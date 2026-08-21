package com.example.yoursoundtrack.managers

import com.example.yoursoundtrack.dataModel.Album
import com.example.yoursoundtrack.dataModel.Artist
import com.example.yoursoundtrack.dataModel.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.util.Locale

class MusicRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId: String?
        get() = auth.currentUser?.uid

     // see what albums we have in db
    fun getAlbumsFlow(): Flow<List<Album>> = callbackFlow {
        val listenerRegistration = db.collection("albums")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val albums = snapshot.toObjects(Album::class.java)
                    trySend(albums)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

     // func for the WTL list
    fun toggleWantToListen(albumId: String, onResult: (isAdded: Boolean) -> Unit) {
        val userId = currentUserId ?: run {
            onResult(false)
            return
        }
        val docRef = db.collection("users")
            .document(userId)
            .collection("wantToListen")
            .document(albumId)
        docRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                docRef.delete().addOnSuccessListener { onResult(false) }
            } else {
                val data = mapOf(
                    "albumId" to albumId,
                    "addedAt" to System.currentTimeMillis()
                )
                docRef.set(data).addOnSuccessListener { onResult(true) }
            }
        }.addOnFailureListener {
            onResult(false)
        }
    }

     // live updates of the album IDs in the WTL list
    fun getWantToListenAlbumIdsFlow(): Flow<List<String>> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = db.collection("users")
            .document(userId)
            .collection("wantToListen")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val albumIds = snapshot.documents.map { it.id }
                    trySend(albumIds)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

     // updates for the ratings given by the current user [AlbumID -> RatingFloat]
    fun getUserRatingsFlow(): Flow<Map<String, Float>> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }

        val listenerRegistration = db.collection("users")
            .document(userId)
            .collection("listens")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val ratingsMap = mutableMapOf<String, Float>()
                    for (doc in snapshot.documents) {
                        val albumId = doc.getString("albumId") ?: doc.id
                        val rating = doc.getDouble("rating")?.toFloat() ?: 0f
                        ratingsMap[albumId] = rating
                    }
                    trySend(ratingsMap)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    //reviews section
    fun saveReview(
        album: Album,
        rating: Float,
        textReview: String,
        isFavorited: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        val userId = currentUserId ?: run {
            onComplete(false)
            return
        }
        val reviewRef = db.collection("users").document(userId).collection("listens").document()
        val reviewId = reviewRef.id
        val albumRef = db.collection("albums").document(album.id)

        val review = Review(
            id = reviewId,
            userId = userId,
            albumId = album.id,
            albumTitle = album.title,
            albumArtist = album.artist,
            albumCoverUrl = album.coverUrl,
            rating = rating,
            textReview = textReview,
            isFavorited = isFavorited,
            timestamp = System.currentTimeMillis()
        )

        db.runTransaction { transaction ->
            transaction.set(reviewRef, review)
            val globalReviewRef = db.collection("reviews").document(reviewId)
            transaction.set(globalReviewRef, review)

            val albumDoc = transaction.get(albumRef)
            val currentRating = albumDoc.getDouble("avgRating") ?: 0.0
            val currentCount = albumDoc.getLong("ratingCount") ?: 0L

            val newCount = currentCount + 1
            val newAvg = ((currentRating * currentCount) + rating) / newCount

            transaction.update(
                albumRef, mapOf(
                    "avgRating" to newAvg,
                    "ratingCount" to newCount
                )
            )
        }.addOnSuccessListener {
            onComplete(true)
        }.addOnFailureListener {
            onComplete(false)
        }
    }

    fun getUserReviewsFlow(): Flow<List<Review>> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = db.collection("users")
            .document(userId)
            .collection("listens")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reviews = snapshot.toObjects(Review::class.java)
                    trySend(reviews)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun getUserReviewsFlowByUserId(userId: String): Flow<List<Review>> = callbackFlow {
        val listenerRegistration = db.collection("users")
            .document(userId)
            .collection("listens")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reviews = snapshot.toObjects(Review::class.java)
                    trySend(reviews)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun getFavoriteReviewsFlow(): Flow<List<Review>> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = db.collection("users")
            .document(userId)
            .collection("listens")
            .whereEqualTo("isFavorited", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reviews = snapshot.toObjects(Review::class.java)
                    trySend(reviews)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun getFavoriteArtistsFlow(userId: String): Flow<List<Artist>> = callbackFlow {
        val listenerRegistration = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val artistNamesOrIds = snapshot.get("favoriteArtists") as? List<String> ?: emptyList()
                if (artistNamesOrIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val queryIds = artistNamesOrIds.take(30)
                db.collection("artists")
                    .whereIn(FieldPath.documentId(), queryIds)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val foundArtists = querySnapshot.documents.mapNotNull { doc ->
                            doc.toObject(Artist::class.java)
                        }

                        if (foundArtists.isEmpty()) {
                            trySend(artistNamesOrIds.map { name -> Artist(id = name, name = name) })
                        } else {
                            trySend(foundArtists)
                        }
                    }
                    .addOnFailureListener {
                        trySend(artistNamesOrIds.map { name -> Artist(id = name, name = name) })
                    }
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun getTopAlbumIdsFlow(userId: String): Flow<List<String>> = callbackFlow {
        val listenerRegistration = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val savedIdsOrTitles = snapshot.get("topAlbumIds") as? List<String> ?: emptyList()
                trySend(savedIdsOrTitles)
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun toggleFavoriteArtist(artistNameOrId: String, onResult: (Boolean) -> Unit) {
        val userId = currentUserId ?: return
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val currentFavs = snapshot.get("favoriteArtists") as? List<String> ?: emptyList()
            val isFav = currentFavs.contains(artistNameOrId)

            val update = if (isFav) {
                FieldValue.arrayRemove(artistNameOrId)
            } else {
                FieldValue.arrayUnion(artistNameOrId)
            }

            userRef.update("favoriteArtists", update)
                .addOnSuccessListener { onResult(!isFav) }
                .addOnFailureListener {
                    val data = mapOf("favoriteArtists" to listOf(artistNameOrId))
                    userRef.set(data, SetOptions.merge())
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                }
        }
    }

    fun getFriendsActivityFlow(): Flow<List<Review>> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var reviewsListener: ListenerRegistration? = null

        val userListener = db.collection("users").document(userId)
            .addSnapshotListener { userDoc, userError ->
                if (userError != null) {
                    close(userError)
                    return@addSnapshotListener
                }

                val friendIds = (userDoc?.get("friendIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                if (friendIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                // Remove prior reviews listener if friend list updates
                reviewsListener?.remove()

                // Query by friendIds without orderBy to avoid missing index failures, then sort in memory
                reviewsListener = db.collection("reviews")
                    .whereIn("userId", friendIds.take(30))
                    .addSnapshotListener { snapshot, reviewsError ->
                        if (reviewsError != null) {
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val reviews = snapshot.toObjects(Review::class.java)
                                .sortedByDescending { it.timestamp }
                                .take(20)
                            trySend(reviews)
                        }
                    }
            }

        awaitClose {
            userListener.remove()
            reviewsListener?.remove()
        }
    }

    fun getArtistsFlow(): Flow<List<Artist>> = callbackFlow {
        val listenerRegistration = db.collection("artists")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val artists = snapshot.toObjects(Artist::class.java)
                    trySend(artists)
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    fun getAlbumsByArtistFlow(
        artistId: String,
        artistName: String,
        albumIds: List<String> = emptyList()
    ): Flow<List<Album>> = callbackFlow {
        val targetDocId = artistId.lowercase(Locale.ROOT).replace(" ", "_")
        var albumsRegistration: ListenerRegistration? = null
        val artistRegistration = db.collection("artists").document(targetDocId)
            .addSnapshotListener { artistSnapshot, artistError ->
                if (artistError != null || artistSnapshot == null || !artistSnapshot.exists()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val fetchedArtist = artistSnapshot.toObject(Artist::class.java)
                val authoritativeAlbumIds = (fetchedArtist?.albumIds.orEmpty() + albumIds)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                val authoritativeArtistId = fetchedArtist?.id?.takeIf { it.isNotBlank() } ?: artistId
                albumsRegistration?.remove()
                albumsRegistration = db.collection("albums")
                    .addSnapshotListener { albumSnapshot, albumError ->
                        if (albumError != null || albumSnapshot == null) {
                            return@addSnapshotListener // Don't wipe out existing list on minor error
                        }
                        val allAlbums = albumSnapshot.toObjects(Album::class.java)
                        val matchedAlbums = allAlbums.filter { album ->
                            val albumId = album.id.trim()
                            val albumArtistId = album.artistId.trim()

                            val matchesIdArray = authoritativeAlbumIds.any { it.equals(albumId, ignoreCase = true) }
                            val matchesArtistField = albumArtistId.equals(authoritativeArtistId, ignoreCase = true) ||
                                    albumArtistId.equals(targetDocId, ignoreCase = true) ||
                                    albumArtistId.equals(artistName, ignoreCase = true)
                            matchesIdArray || matchesArtistField
                        }
                        // Only send if we found albums, or if authoritativeAlbumIds is truly empty
                        if (matchedAlbums.isNotEmpty() || authoritativeAlbumIds.isEmpty()) {
                            trySend(matchedAlbums.distinctBy { it.id })
                        }
                    }
            }
        awaitClose {
            artistRegistration.remove()
            albumsRegistration?.remove()
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)
}