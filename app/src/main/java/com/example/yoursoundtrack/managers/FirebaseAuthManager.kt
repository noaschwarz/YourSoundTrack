package com.example.yoursoundtrack.managers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseAuthManager {
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Creates a new user account with email and password,
     * and saves their initial user document into Firestore.
     */
    fun createAccount(
        username: String,
        email: String,
        password: String,
        onResult: (FirebaseUser?, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        // Create user document mapping
                        val userMap = hashMapOf(
                            "uid" to user.uid,
                            "username" to username,
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )

                        // Save user profile in Firestore under 'users/{uid}'
                        db.collection("users").document(user.uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                onResult(user, null)
                            }
                            .addOnFailureListener { e ->
                                // Firestore write failed
                                val errorMsg = e.localizedMessage ?: "Failed to save user data."
                                onResult(null, errorMsg)
                            }
                    } else {
                        onResult(null, "User creation failed.")
                    }
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Registration failed."
                    onResult(null, errorMessage)
                }
            }
    }

    /**
     * Signs in an existing user with email and password.
     */
    fun loginUser(email: String, password: String, onResult: (FirebaseUser?, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(task.result?.user, null)
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Login failed."
                    onResult(null, errorMessage)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }
}