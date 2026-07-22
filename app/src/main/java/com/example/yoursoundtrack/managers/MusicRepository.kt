package com.example.yoursoundtrack.managers

import com.example.yoursoundtrack.dataModel.Album
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MusicRepository {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Listens live to the "albums" collection in Firestore.
     */
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

        // Remove Firestore listener when screen/flow is destroyed
        awaitClose { listenerRegistration.remove() }
    }
}