package com.example.yoursoundtrack.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.adapters.AlbumAdapter
import com.example.yoursoundtrack.adapters.ArtistAdapter
import com.example.yoursoundtrack.adapters.LastListensAdapter
import com.example.yoursoundtrack.managers.MusicViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.recyclerview.widget.GridLayoutManager

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var topFiveAdapter: AlbumAdapter
    private lateinit var favArtistsAdapter: ArtistAdapter
    private lateinit var lastListensAdapter: LastListensAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        view.findViewById<TextView>(R.id.text_username_label)?.text =
            currentUser?.displayName ?: currentUser?.email ?: "Music Lover"

        setupRecyclerViews(view)
        observeData()

        view.findViewById<Button>(R.id.btn_edit_profile)?.setOnClickListener {
            findNavController().navigate(R.id.navigation_edit_profile)
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

    private fun observeData() {
        viewModel.userReviews.observe(viewLifecycleOwner) { reviews ->
            lastListensAdapter.submitList(reviews)
        }

        viewModel.favoriteArtists.observe(viewLifecycleOwner) { artists ->
            favArtistsAdapter.submitList(artists)
        }

        viewModel.topFiveAlbums.observe(viewLifecycleOwner) { albums ->
            topFiveAdapter.submitList(albums)
        }

        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            viewModel.loadUserReviews(uid)
            viewModel.loadFavoriteArtists(uid)
            viewModel.loadTopFiveAlbums(uid)
        }
    }
}