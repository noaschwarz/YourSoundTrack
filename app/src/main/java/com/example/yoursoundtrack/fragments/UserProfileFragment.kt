package com.example.yoursoundtrack.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.adapters.AlbumAdapter
import com.example.yoursoundtrack.adapters.ArtistAdapter
import com.example.yoursoundtrack.adapters.LastListensAdapter
import com.example.yoursoundtrack.dataModel.UserProfile
import com.example.yoursoundtrack.managers.FirebaseAuthManager
import com.example.yoursoundtrack.managers.MusicViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var topFiveAdapter: AlbumAdapter
    private lateinit var favArtistsAdapter: ArtistAdapter
    private lateinit var lastListensAdapter: LastListensAdapter

    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        val passedUid = arguments?.getString("userId")
        val isSelf = passedUid == null || passedUid == currentUid
        val targetUid = passedUid ?: currentUid

        val editBtn = view.findViewById<Button>(R.id.btn_edit_profile)
        val followBtn = view.findViewById<Button>(R.id.btn_follow)
        val tvUsername = view.findViewById<TextView>(R.id.text_username_label)

        if (isSelf) {
            editBtn?.visibility = View.VISIBLE
            followBtn?.visibility = View.GONE
        } else {
            editBtn?.visibility = View.GONE
            followBtn?.visibility = View.VISIBLE
            targetUid?.let { setupFollowButton(it, followBtn) }
        }

        // Fetch user profile from Firestore automatically
        targetUid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                val profile = doc.toObject(UserProfile::class.java)
                val firebaseUser = FirebaseAuth.getInstance().currentUser

                val displayName = when {
                    !profile?.username.isNullOrEmpty() -> profile?.username
                    isSelf && !firebaseUser?.displayName.isNullOrEmpty() -> firebaseUser?.displayName
                    !profile?.email.isNullOrEmpty() -> profile?.email
                    else -> "Music Lover"
                }
                tvUsername?.text = displayName
            }
        }

        setupRecyclerViews(view)
        targetUid?.let { observeData(it) }

        editBtn?.setOnClickListener {
            findNavController().navigate(R.id.navigation_edit_profile)
        }
    }

    private fun setupFollowButton(targetUid: String, followBtn: Button?) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(currentUid).get().addOnSuccessListener { doc ->
            val myProfile = doc.toObject(UserProfile::class.java)
            var isFollowing = myProfile?.friendIds?.contains(targetUid) == true

            followBtn?.text = if (isFollowing) "Unfollow" else "Follow"

            followBtn?.setOnClickListener {
                if (isFollowing) {
                    FirebaseAuthManager.unfollowUser(targetUid) { success ->
                        if (success) {
                            isFollowing = false
                            followBtn.text = "Follow"
                        }
                    }
                } else {
                    FirebaseAuthManager.followUser(targetUid) { success ->
                        if (success) {
                            isFollowing = true
                            followBtn.text = "Unfollow"
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerViews(view: View) {
        topFiveAdapter = AlbumAdapter { album ->
            val bundle = Bundle().apply { putString("albumId", album.id) }
            findNavController().navigate(R.id.navigation_album_detail, bundle)
        }

        view.findViewById<RecyclerView>(R.id.rv_top_five)?.apply {
            layoutManager = GridLayoutManager(context, 5)
            adapter = topFiveAdapter
        }

        favArtistsAdapter = ArtistAdapter { artist ->
            val bundle = Bundle().apply { putString("artistId", artist.id) }
            findNavController().navigate(R.id.navigation_artist_detail, bundle)
        }
        view.findViewById<RecyclerView>(R.id.rv_fav_artists)?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = favArtistsAdapter
        }

        lastListensAdapter = LastListensAdapter { review ->
            val bundle = Bundle().apply {
                putString("reviewId", review.id)
                putString("albumId", review.albumId)
            }
            findNavController().navigate(R.id.navigation_review_detail, bundle)
        }
        view.findViewById<RecyclerView>(R.id.rv_last_listens)?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = lastListensAdapter
        }
    }

    private fun observeData(uid: String) {
        viewModel.userReviews.observe(viewLifecycleOwner) { reviews ->
            lastListensAdapter.submitList(reviews)
        }

        viewModel.favoriteArtists.observe(viewLifecycleOwner) { artists ->
            favArtistsAdapter.submitList(artists)
        }

        viewModel.topFiveAlbums.observe(viewLifecycleOwner) { albums ->
            topFiveAdapter.submitList(albums)
        }

        viewModel.loadUserReviews(uid)
        viewModel.loadFavoriteArtists(uid)
        viewModel.loadTopFiveAlbums(uid)
    }
}