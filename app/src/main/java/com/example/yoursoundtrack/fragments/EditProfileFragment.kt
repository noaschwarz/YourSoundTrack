package com.example.yoursoundtrack.fragments

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private var selectedImageUri: Uri? = null
    private lateinit var ivProfilePreview: ImageView
    private lateinit var etTopAlbums: TextInputEditText
    private lateinit var etFavoriteArtists: TextInputEditText

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .into(ivProfilePreview)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivProfilePreview = view.findViewById(R.id.iv_profile_preview)
        etTopAlbums = view.findViewById(R.id.et_top_albums)
        etFavoriteArtists = view.findViewById(R.id.et_favorite_artists)

        loadExistingProfile()

        view.findViewById<Button>(R.id.btn_select_photo)?.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        view.findViewById<Button>(R.id.btn_save_profile)?.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun loadExistingProfile() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val topAlbums = snapshot.get("topAlbumIds") as? List<String> ?: emptyList()
                    val favoriteArtists = snapshot.get("favoriteArtists") as? List<String> ?: emptyList()
                    val profilePicUrl = snapshot.getString("profilePictureUrl")

                    etTopAlbums.setText(topAlbums.joinToString(", "))
                    etFavoriteArtists.setText(favoriteArtists.joinToString(", "))

                    if (!profilePicUrl.isNullOrEmpty() && selectedImageUri == null) {
                        Glide.with(this).load(profilePicUrl).into(ivProfilePreview)
                    }
                }
            }
    }

    private fun saveProfileChanges() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        val rawAlbumsText = etTopAlbums.text?.toString().orEmpty()
        val topAlbumList = rawAlbumsText
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .take(5) // Limit to top 5

        val rawArtistsText = etFavoriteArtists.text?.toString().orEmpty()
        val favoriteArtistList = rawArtistsText
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val userUpdate = mutableMapOf<String, Any>(
            "topAlbumIds" to topAlbumList,
            "favoriteArtists" to favoriteArtistList
        )

        val uriToUpload = selectedImageUri
        if (uriToUpload != null) {
            val storageRef = FirebaseStorage.getInstance().reference
                .child("profile_pictures/${user.uid}.jpg")

            storageRef.putFile(uriToUpload)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri: Uri ->
                        userUpdate["profilePictureUrl"] = downloadUri.toString()
                        updateFirestoreAndExit(user.uid, userUpdate)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        } else {
            updateFirestoreAndExit(user.uid, userUpdate)
        }
    }

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