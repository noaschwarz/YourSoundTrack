package com.example.yoursoundtrack.ui.theme

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.yoursoundtrack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         //Thread { seedAlbumsToFirestore(requireContext()) }.start() //meant for adding albums to DB

        // Check if current user is logged in and exists in DB
        checkCurrentUserInDb()
    }

    fun checkCurrentUserInDb() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Log.d("FirestoreCheck", "User Data: ${document.data}")
                    } else {
                        Log.d("FirestoreCheck", "No such document found!")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreCheck", "Error fetching user", exception)
                }
        }
    }

    fun seedAlbumsToFirestore(context: Context) {
        try {
            val jsonString = context.assets.open("albums_100.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val albumsArray = jsonObject.getJSONArray("albums")

            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()

            for (i in 0 until albumsArray.length()) {
                val albumObj = albumsArray.getJSONObject(i)
                val id = albumObj.getString("id")

                val tracksArray = albumObj.getJSONArray("tracks")
                val tracksList = mutableListOf<String>()
                for (j in 0 until tracksArray.length()) {
                    tracksList.add(tracksArray.getString(j))
                }

                val albumMap = hashMapOf(
                    "id" to id,
                    "title" to albumObj.getString("title"),
                    "artist" to albumObj.getString("artist"),
                    "releaseYear" to albumObj.getInt("releaseYear"),
                    "genre" to albumObj.getString("genre"),
                    "coverUrl" to albumObj.getString("coverUrl"),
                    "avgRating" to albumObj.getDouble("avgRating"),
                    "tracks" to tracksList
                )

                val docRef = db.collection("albums").document(id)
                batch.set(docRef, albumMap)
            }

            batch.commit()
                .addOnSuccessListener {
                    Log.d("FirestoreSeed", "Successfully uploaded ${albumsArray.length()} albums!")
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreSeed", "Error committing batch", e)
                }

        } catch (e: Exception) {
            Log.e("FirestoreSeed", "Error parsing JSON or reading asset", e)
        }
    }
}