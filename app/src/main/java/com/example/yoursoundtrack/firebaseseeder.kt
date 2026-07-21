package com.example.yoursoundtrack

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseSeeder {

    fun seedInitialData(onComplete: (Boolean) -> Unit = {}) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            Log.e("FirebaseSeeder", "Cannot seed data: User is not authenticated.")
            onComplete(false)
            return
        }

        val userId = currentUser.uid

        // 1. Seed User Document
        val userData = hashMapOf(
            "email" to currentUser.email,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("users").document(userId).set(userData)

        // 2. Seed Sample Albums (into top-level 'albums' collection)
        val album1 = hashMapOf(
            "title" to "Abbey Road",
            "artist" to "The Beatles",
            "year" to 1969,
            "genre" to "Rock"
        )
        val album2 = hashMapOf(
            "title" to "Dark Side of the Moon",
            "artist" to "Pink Floyd",
            "year" to 1973,
            "genre" to "Progressive Rock"
        )

        val albumsRef = db.collection("albums")
        albumsRef.add(album1)
        albumsRef.add(album2)

        // 3. Seed Sample Track Listen (into 'user_listens' subcollection)
        val sampleListen = hashMapOf(
            "songTitle" to "Come Together",
            "artist" to "The Beatles",
            "albumName" to "Abbey Road",
            "rating" to 5.0,
            "review" to "Timeless track!",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .collection("user_listens")
            .add(sampleListen)
            .addOnSuccessListener {
                Log.d("FirebaseSeeder", "Sample listen seeded successfully!")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseSeeder", "Failed to seed listen", e)
                onComplete(false)
            }
    }
}