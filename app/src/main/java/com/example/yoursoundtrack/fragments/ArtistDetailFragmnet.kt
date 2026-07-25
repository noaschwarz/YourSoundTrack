package com.example.yoursoundtrack.fragments

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.adapters.AlbumAdapter
import com.example.yoursoundtrack.dataModel.Artist
import com.example.yoursoundtrack.managers.MusicViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArtistDetailFragment : Fragment(R.layout.fragment_artist_detail) {

    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var albumAdapter: AlbumAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btn_back_artist_detail)?.setOnClickListener {
            findNavController().navigateUp()
        }

        setupAlbumRecyclerView(view)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.loadFavoriteArtists(userId)
        }

        val artistId = arguments?.getString("artistId") ?: return

        val cachedArtist = viewModel.favoriteArtists.value?.find { it.id == artistId }
        if (cachedArtist != null) {
            bindArtistData(view, cachedArtist)
        } else {
            FirebaseFirestore.getInstance().collection("artists").document(artistId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val artist = snapshot.toObject(Artist::class.java)
                    if (artist != null) {
                        bindArtistData(view, artist)
                    }
                }
        }
    }

    private fun setupAlbumRecyclerView(view: View) {
        albumAdapter = AlbumAdapter { album ->
            val bundle = Bundle().apply { putString("albumId", album.id) }
            findNavController().navigate(R.id.navigation_album_detail, bundle)
        }

        view.findViewById<RecyclerView>(R.id.rv_artist_albums)?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = albumAdapter
        }
    }

    private fun bindArtistData(view: View, artist: Artist) {
        view.findViewById<TextView>(R.id.tv_artist_detail_name)?.text = artist.name
        view.findViewById<TextView>(R.id.tv_artist_detail_genre)?.text = artist.genre

        val ivImage = view.findViewById<ImageView>(R.id.iv_artist_detail_image)
        if (ivImage != null) {
            Glide.with(this)
                .load(artist.imageUrl)
                .placeholder(R.drawable.singer_icon)
                .into(ivImage)
        }

        val cbFavorite = view.findViewById<CheckBox>(R.id.btn_favorite_artist)
        val targetArtistKey = artist.id.ifEmpty { artist.name }

        viewModel.favoriteArtists.observe(viewLifecycleOwner) { favorites ->
            val isFav = favorites.any { it.id == targetArtistKey || it.name == targetArtistKey }
            cbFavorite?.isChecked = isFav
        }

        cbFavorite?.setOnClickListener {
            viewModel.toggleFavoriteArtist(targetArtistKey) { success ->
                if (!success) {
                    cbFavorite.isChecked = !cbFavorite.isChecked
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allAlbumsState.collectLatest { allAlbums ->
                val artistAlbums = allAlbums.filter { album ->
                    album.artist.equals(artist.name, ignoreCase = true) || artist.albumIds.contains(album.id)
                }
                albumAdapter.submitList(artistAlbums)
            }
        }
    }
}