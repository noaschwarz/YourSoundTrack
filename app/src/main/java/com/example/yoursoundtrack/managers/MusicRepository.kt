package com.example.yoursoundtrack.managers

import com.example.yoursoundtrack.dataModel.Album
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.example.yoursoundtrack.dataModel.Review
import com.google.firebase.firestore.Query

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
        }.addOnSuccessListener {
            recalculateAlbumAverage(album.id) //update the avg
            onComplete(true)
        }.addOnFailureListener {
            onComplete(false)
        }
    }

    private fun recalculateAlbumAverage(albumId: String) {
        db.collection("reviews")
            .whereEqualTo("albumId", albumId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val ratings = querySnapshot.documents.mapNotNull { it.getDouble("rating")?.toFloat() }
                    val avg = ratings.average().toFloat()
                    db.collection("albums").document(albumId).update("avgRating", avg)
                }
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

    fun getUserFavoriteReviewsFlow(): Flow<List<Review>> = callbackFlow {
        val userId = currentUserId
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = db.collection("users")
            .document(userId)
            .collection("listens")
            .whereEqualTo("favorited", true)
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
}