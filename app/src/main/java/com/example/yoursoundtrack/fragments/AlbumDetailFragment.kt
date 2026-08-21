package com.example.yoursoundtrack.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.adapters.TrackAdapter
import com.example.yoursoundtrack.dataModel.Album
import com.example.yoursoundtrack.managers.loadAlbumCover
import com.example.yoursoundtrack.managers.MusicViewModel
import kotlinx.coroutines.launch

class AlbumDetailFragment : Fragment(R.layout.fragment_album_detail) {

    private val viewModel: MusicViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val albumId = arguments?.getString("albumId")

        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        btnBack?.setOnClickListener { findNavController().navigateUp() }

        val btnRateAlbum = view.findViewById<ImageButton>(R.id.btn_rate_album)

        if (!albumId.isNullOrEmpty()) {
            setupWantToListenButton(view, albumId)
            observeAlbumDetails(view, albumId)
            observeUserSavedState(view, albumId)
            observeUserRating(view, albumId)

            // go to rate screen and not search
            btnRateAlbum?.setOnClickListener {
                val bundle = bundleOf("albumId" to albumId)
                findNavController().navigate(R.id.navigation_new, bundle)
            }
        }
    }

    //toggle the album in or out of WTL
    private fun setupWantToListenButton(view: View, albumId: String) {
        val btnWantToListen = view.findViewById<ImageButton>(R.id.btn_want_to_listen)
        btnWantToListen?.setOnClickListener {
            viewModel.toggleWantToListen(albumId) { newlyAdded ->
                context?.let { ctx ->
                    val msg = if (newlyAdded) "Added to Want to Listen!" else "Removed from Want to Listen"
                    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //get the curr album details
    private fun observeAlbumDetails(view: View, albumId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allAlbumsState.collect { albums ->
                    val selectedAlbum = albums.find { it.id == albumId }
                    selectedAlbum?.let { bindAlbumData(view, it) }
                }
            }
        }
    }

    // update the bookmark icon base on state
    private fun observeUserSavedState(view: View, albumId: String) {
        val btnWantToListen = view.findViewById<ImageButton>(R.id.btn_want_to_listen)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.wantToListenIdsState.collect { savedIds ->
                    val isSaved = savedIds.contains(albumId)
                    if (isSaved) {
                        btnWantToListen?.setImageResource(R.drawable.minus_symble)
                    } else {
                        btnWantToListen?.setImageResource(R.drawable.plus_symble)
                    }
                }
            }
        }
    }

    //display curr users rating
    private fun observeUserRating(view: View, albumId: String) {
        val tvUserRating = view.findViewById<TextView>(R.id.tv_user_personal_rating)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userRatingsState.collect { ratingsMap ->
                    val personalRating = ratingsMap[albumId]
                    if (personalRating != null && personalRating > 0f) {
                        tvUserRating?.text = String.format("Your rating: ★ %.1f", personalRating)
                    } else {
                        tvUserRating?.text = "Your rating: Not rated"
                    }
                }
            }
        }
    }

    //bind the album info to UI
    private fun bindAlbumData(view: View, album: Album) {
        view.findViewById<TextView>(R.id.tv_detail_title)?.text = album.title
        view.findViewById<TextView>(R.id.tv_detail_artist)?.text = album.artist
        view.findViewById<TextView>(R.id.tv_detail_genre_year)?.text = "${album.genre} • ${album.releaseYear}"
        view.findViewById<RatingBar>(R.id.rating_bar_avg)?.rating = album.avgRating.toFloat()
        view.findViewById<TextView>(R.id.tv_avg_rating_text)?.text = String.format("%.1f / 5.0", album.avgRating)

        val ivCover = view.findViewById<ImageView>(R.id.iv_detail_cover)
        if (album.coverUrl.isNotEmpty()) {
            ivCover?.loadAlbumCover(album.coverUrl)
        }

        val rvTracks = view.findViewById<RecyclerView>(R.id.rv_tracklist)
        rvTracks?.let { rv ->
            if (rv.adapter == null) {
                rv.layoutManager = LinearLayoutManager(requireContext())
                rv.adapter = TrackAdapter(album.tracks)
            }
        }
    }
}