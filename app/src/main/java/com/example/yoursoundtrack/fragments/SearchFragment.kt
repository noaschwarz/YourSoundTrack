package com.example.yoursoundtrack.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
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
import com.example.yoursoundtrack.adapters.ArtistAdapter
import com.example.yoursoundtrack.adapters.SearchAlbumAdapter
import com.example.yoursoundtrack.adapters.UserAdapter
import com.example.yoursoundtrack.dataModel.Album
import com.example.yoursoundtrack.dataModel.Artist
import com.example.yoursoundtrack.managers.FirebaseAuthManager
import com.example.yoursoundtrack.managers.MusicViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Locale

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var searchAdapter: SearchAlbumAdapter
    private lateinit var userAdapter: UserAdapter
    private lateinit var artistAdapter: ArtistAdapter

    private var allAlbums: List<Album> = emptyList()
    private var allArtists: List<Artist> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchBar = view.findViewById<SearchView>(R.id.search_bar)
        val layoutCategories = view.findViewById<LinearLayout>(R.id.layout_browse_categories)
        val rvResults = view.findViewById<RecyclerView>(R.id.rv_search_results)
        val rvUserResults = view.findViewById<RecyclerView>(R.id.rv_user_search_results)
        val rvArtistResults = view.findViewById<RecyclerView>(R.id.rv_artist_search_results)

        searchAdapter = SearchAlbumAdapter { selectedAlbum ->
            val bundle = bundleOf("albumId" to selectedAlbum.id)
            findNavController().navigate(
                R.id.action_navigation_search_to_navigation_album_detail,
                bundle
            )
        }

        userAdapter = UserAdapter { selectedUser ->
            val bundle = bundleOf("userId" to selectedUser.uid)
            findNavController().navigate(R.id.navigation_you, bundle)
        }

        artistAdapter = ArtistAdapter { selectedArtist ->
            val bundle = bundleOf("artistId" to selectedArtist.id)
            findNavController().navigate(R.id.navigation_artist_detail, bundle)
        }

        rvResults?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }

        // live flow of our albums
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allAlbumsState.collect { albums ->
                    allAlbums = albums
                    if (allArtists.isEmpty() && albums.isNotEmpty()) {
                        allArtists = extractArtistsFromAlbums(albums)
                    }
                    filterList(searchBar.query.toString(), layoutCategories, rvResults, rvUserResults, rvArtistResults)
                }
            }
        }

        loadArtists { artists ->
            if (artists.isNotEmpty()) {
                allArtists = artists
            } else if (allAlbums.isNotEmpty()) {
                allArtists = extractArtistsFromAlbums(allAlbums)
            }
            filterList(searchBar.query.toString(), layoutCategories, rvResults, rvUserResults, rvArtistResults)
        }

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty().trim()
                filterList(query, layoutCategories, rvResults, rvUserResults, rvArtistResults)
                return true
            }
        })
    }

    private fun extractArtistsFromAlbums(albums: List<Album>): List<Artist> {
        return albums
            .mapNotNull { album ->
                val name = album.artist?.trim()
                if (name.isNullOrEmpty()) null else name to album.coverUrl
            }
            .distinctBy { it.first.lowercase(Locale.ROOT) }
            .map { (name, coverUrl) ->
                val id = name.lowercase(Locale.ROOT).replace("[^a-z0-9_]".toRegex(), "_")
                Artist(id = id, name = name, imageUrl = coverUrl)
            }
    }

    private fun loadArtists(onComplete: (List<Artist>) -> Unit) {
        FirebaseFirestore.getInstance().collection("artists")
            .get()
            .addOnSuccessListener { query ->
                val artists = query.documents.mapNotNull { it.toObject(Artist::class.java) }
                onComplete(artists)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }

    private fun filterList(
        query: String,
        categoriesLayout: LinearLayout?,
        rvAlbums: RecyclerView?,
        rvUsers: RecyclerView?,
        rvArtists: RecyclerView?
    ) {
        if (query.isEmpty()) {
            categoriesLayout?.visibility = View.VISIBLE
            rvAlbums?.visibility = View.GONE
            rvUsers?.visibility = View.GONE
            rvArtists?.visibility = View.GONE
            return
        }

        categoriesLayout?.visibility = View.GONE

        // 1. Filter Albums
        val filteredAlbums = allAlbums.filter { album ->
            album.title.contains(query, ignoreCase = true) ||
                    album.artist.contains(query, ignoreCase = true)
        }
        searchAdapter.submitList(filteredAlbums)
        rvAlbums?.visibility = if (filteredAlbums.isNotEmpty()) View.VISIBLE else View.GONE

        // 2. Filter Artists
        val filteredArtists = allArtists.filter { artist ->
            artist.name.contains(query, ignoreCase = true) ||
                    artist.id.replace("_", " ").contains(query, ignoreCase = true)
        }
        artistAdapter.submitList(filteredArtists)
        rvArtists?.visibility = if (filteredArtists.isNotEmpty()) View.VISIBLE else View.GONE

        // 3. Filter Users with Debug Logging & Strict Visibility Setup
        FirebaseAuthManager.searchUsers(query) { users ->
            activity?.runOnUiThread {
                Log.d("SearchUsers", "Query: '$query' | Found users count: ${users.size}")
                userAdapter.submitList(users)
                rvUsers?.visibility = if (users.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}