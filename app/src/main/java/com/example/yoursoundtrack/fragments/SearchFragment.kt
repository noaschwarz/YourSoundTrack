package com.example.yoursoundtrack.fragments

import android.os.Bundle
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
import com.example.yoursoundtrack.adapters.SearchAlbumAdapter
import com.example.yoursoundtrack.adapters.UserAdapter
import com.example.yoursoundtrack.dataModel.Album
import com.example.yoursoundtrack.managers.FirebaseAuthManager
import com.example.yoursoundtrack.managers.MusicViewModel
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var searchAdapter: SearchAlbumAdapter
    private lateinit var userAdapter: UserAdapter
    private var allAlbums: List<Album> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchBar = view.findViewById<SearchView>(R.id.search_bar)
        val layoutCategories = view.findViewById<LinearLayout>(R.id.layout_browse_categories)
        val rvResults = view.findViewById<RecyclerView>(R.id.rv_search_results)
        val rvUserResults = view.findViewById<RecyclerView>(R.id.rv_user_search_results)

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

        rvResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }

        // live flow of our albums
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allAlbumsState.collect { albums ->
                    allAlbums = albums
                    filterList(searchBar.query.toString(), layoutCategories, rvResults, rvUserResults)
                }
            }
        }

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty().trim()
                filterList(query, layoutCategories, rvResults, rvUserResults)
                return true
            }
        })
    }

    private fun filterList(
        query: String,
        categoriesLayout: LinearLayout,
        rvAlbums: RecyclerView,
        rvUsers: RecyclerView?
    ) {
        if (query.isEmpty()) {
            categoriesLayout.visibility = View.VISIBLE
            rvAlbums.visibility = View.GONE
            rvUsers?.visibility = View.GONE
            return
        }

        categoriesLayout.visibility = View.GONE
        rvAlbums.visibility = View.VISIBLE
        rvUsers?.visibility = View.VISIBLE

        val filteredAlbums = allAlbums.filter { album ->
            album.title.contains(query, ignoreCase = true) ||
                    album.artist.contains(query, ignoreCase = true)
        }
        searchAdapter.submitList(filteredAlbums)

        FirebaseAuthManager.searchUsers(query) { users ->
            userAdapter.submitList(users)
        }
    }
}