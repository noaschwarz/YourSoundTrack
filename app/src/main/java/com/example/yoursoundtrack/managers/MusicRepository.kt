package com.example.yoursoundtrack.managers

import com.example.yoursoundtrack.dataModel.Album
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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
}