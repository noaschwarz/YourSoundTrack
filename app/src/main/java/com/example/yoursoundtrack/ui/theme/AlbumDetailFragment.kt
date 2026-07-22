package com.example.yoursoundtrack.ui.theme

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.adapters.TrackAdapter
import com.example.yoursoundtrack.dataModel.Album
import kotlinx.coroutines.launch

class AlbumDetailFragment : Fragment(R.layout.fragment_album_detail) {

    private val viewModel: MusicViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val albumId = arguments?.getString("albumId")

        val btnBack = view.findViewById<ImageButton>(R.id.btn_back)
        btnBack?.setOnClickListener { findNavController().navigateUp() }

        if (albumId != null) {
            observeAlbumDetails(view, albumId)
        }
    }

    private fun observeAlbumDetails(view: View, albumId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Updated to reference allAlbumsState
                viewModel.allAlbumsState.collect { albums ->
                    val selectedAlbum = albums.find { it.id == albumId }
                    selectedAlbum?.let { bindAlbumData(view, it) }
                }
            }
        }
    }

    private fun bindAlbumData(view: View, album: Album) {
        view.findViewById<TextView>(R.id.tv_detail_title)?.text = album.title
        view.findViewById<TextView>(R.id.tv_detail_artist)?.text = album.artist
        view.findViewById<TextView>(R.id.tv_detail_genre_year)?.text = "${album.genre} • ${album.releaseYear}"
        view.findViewById<RatingBar>(R.id.rating_bar_avg)?.rating = album.avgRating.toFloat()
        view.findViewById<TextView>(R.id.tv_avg_rating_text)?.text = String.format("%.1f / 5.0", album.avgRating)

        val ivCover = view.findViewById<ImageView>(R.id.iv_detail_cover)
        if (ivCover != null) {
            Glide.with(this)
                .load(album.coverUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivCover)
        }

        val rvTracks = view.findViewById<RecyclerView>(R.id.rv_tracklist)
        rvTracks?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = TrackAdapter(album.tracks)
        }
    }
}