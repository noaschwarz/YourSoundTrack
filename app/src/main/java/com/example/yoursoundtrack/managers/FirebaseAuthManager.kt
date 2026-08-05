package com.example.yoursoundtrack.managers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.example.yoursoundtrack.dataModel.UserProfile
import com.google.firebase.firestore.FieldValue

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

    fun createAccount( //creat a user in db
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
                        val userMap = hashMapOf(
                            "uid" to user.uid,
                            "username" to username,
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("users").document(user.uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                onResult(user, null)
                            }
                            .addOnFailureListener { e ->
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

     //if user exist login w it
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

    fun followUser(targetUid: String, onComplete: (Boolean) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return
        db.collection("users").document(currentUid)
            .update("friendIds", FieldValue.arrayUnion(targetUid))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun unfollowUser(targetUid: String, onComplete: (Boolean) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return
        db.collection("users").document(currentUid)
            .update("friendIds", FieldValue.arrayRemove(targetUid))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun searchUsers(query: String, onResult: (List<UserProfile>) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: ""
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) }
                val filtered = users.filter { user ->
                    user.uid != currentUid &&
                            (user.username.contains(query, ignoreCase = true) || user.email.contains(query, ignoreCase = true))
                }
                onResult(filtered)
            }
    }

    fun fetchFriends(onResult: (List<UserProfile>) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return
        db.collection("users").document(currentUid).get().addOnSuccessListener { doc ->
            val user = doc.toObject(UserProfile::class.java)
            val friendIds = user?.friendIds ?: emptyList()
            if (friendIds.isEmpty()) {
                onResult(emptyList())
                return@addOnSuccessListener
            }
            db.collection("users").whereIn("uid", friendIds).get().addOnSuccessListener { snapshot ->
                onResult(snapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) })
            }
        }
    }

    fun logout() {
        auth.signOut()
    }
}