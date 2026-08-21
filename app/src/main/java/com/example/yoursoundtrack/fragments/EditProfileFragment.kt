package com.example.yoursoundtrack.fragments

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.yoursoundtrack.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlin.io.encoding.Base64

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private var selectedImageUri: Uri? = null
    private lateinit var ivProfilePreview: ImageView
    private lateinit var etUsername: TextInputEditText
    private lateinit var etTopAlbums: TextInputEditText
    private lateinit var etFavoriteArtists: TextInputEditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivProfilePreview = view.findViewById(R.id.iv_profile_preview)
        etUsername = view.findViewById(R.id.et_username)
        etTopAlbums = view.findViewById(R.id.et_top_albums)
        etFavoriteArtists = view.findViewById(R.id.et_favorite_artists)

        loadExistingProfile()

        view.findViewById<Button>(R.id.btn_select_photo)?.setOnClickListener {
            Toast.makeText(context, "Pending development", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<Button>(R.id.btn_cancel)?.setOnClickListener { //go back
            findNavController().navigateUp()
        }
        view.findViewById<Button>(R.id.btn_save_profile)?.setOnClickListener { //save
            saveProfileChanges()
        }
    }

    //load curr user info
    private fun loadExistingProfile() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val username = snapshot.getString("username")
                    val topAlbums = snapshot.get("topAlbumIds") as? List<String> ?: emptyList()
                    val favoriteArtists = snapshot.get("favoriteArtists") as? List<String> ?: emptyList()
                    val profilePicUrl = snapshot.getString("profilePictureUrl")

                    etUsername.setText(username ?: user.displayName.orEmpty())
                    etTopAlbums.setText(topAlbums.joinToString(", "))
                    etFavoriteArtists.setText(favoriteArtists.joinToString(", "))

                    if (!profilePicUrl.isNullOrEmpty()) {
                        Glide.with(this).load(profilePicUrl).into(ivProfilePreview)
                    }
                } else {
                    etUsername.setText(user.displayName.orEmpty())
                }
            }
    }

    //validate the inputs
    private fun saveProfileChanges() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        val newUsername = etUsername.text?.toString()?.trim().orEmpty()
        val rawAlbumsText = etTopAlbums.text?.toString().orEmpty()
        val topAlbumList = rawAlbumsText
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .take(5) // Limit to top 5
        //clean out top 5 albums
        val rawArtistsText = etFavoriteArtists.text?.toString().orEmpty()
        val favoriteArtistList = rawArtistsText
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        //clean out fav artists
        val userUpdate = mutableMapOf<String, Any>(
            "username" to newUsername,
            "topAlbumIds" to topAlbumList,
            "favoriteArtists" to favoriteArtistList
        )
        //update diaply name
        if (newUsername.isNotEmpty()) {
            val profileUpdates = userProfileChangeRequest {
                displayName = newUsername
            }
            user.updateProfile(profileUpdates)
        }
        updateFirestoreAndExit(user.uid, userUpdate)
    }
    //update the data to firebase
    private fun updateFirestoreAndExit(uid: String, data: Map<String, Any>) {
        FirebaseFirestore.getInstance().collection("users")
            .document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to save profile", Toast.LENGTH_SHORT).show()
            }
    }
}