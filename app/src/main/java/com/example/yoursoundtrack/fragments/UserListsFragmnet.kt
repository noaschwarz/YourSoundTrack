package com.example.yoursoundtrack.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.adapters.AlbumAdapter
import com.example.yoursoundtrack.managers.MusicViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class UserListsFragment : Fragment(R.layout.fragment_user_lists) {

    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var catalogAdapter: AlbumAdapter
    private var observeJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        catalogAdapter = AlbumAdapter { album ->
            val bundle = Bundle().apply { putString("albumId", album.id) }
            findNavController().navigate(R.id.navigation_album_detail, bundle)
        }

        val rvCatalog = view.findViewById<RecyclerView>(R.id.rv_vertical_catalog_list)
        rvCatalog?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = catalogAdapter
        }

        setupFilterChips(view)
        observeLists("all")
    }

    private fun setupFilterChips(view: View) {
        view.findViewById<Button>(R.id.chip_all)?.setOnClickListener { observeLists("all") }
        view.findViewById<Button>(R.id.chip_logged)?.setOnClickListener { observeLists("logged") }
        view.findViewById<Button>(R.id.chip_plan)?.setOnClickListener { observeLists("plan") }
        view.findViewById<Button>(R.id.chip_fav)?.setOnClickListener { observeLists("fav") }
    }

    //combine flow of lists
    private fun observeLists(filterMode: String) {
        observeJob?.cancel()

        observeJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                when (filterMode) {
                    "all" -> {
                        combine(
                            viewModel.allAlbumsState,
                            viewModel.lastListensState,
                            viewModel.wantToListenIdsState
                        ) { allAlbums, userReviews, wantToListenIds ->
                            val reviewedIds = userReviews.map { it.albumId }.toSet()
                            val plannedIds = wantToListenIds.toSet()

                            val combinedTargetIds = reviewedIds + plannedIds

                            allAlbums.filter { combinedTargetIds.contains(it.id) }
                        }.collect { combinedList ->
                            catalogAdapter.submitList(combinedList)
                        }
                    }
                    "fav" -> {
                        combine(
                            viewModel.allAlbumsState,
                            viewModel.favoriteReviewsState
                        ) { allAlbums, favoriteReviews ->
                            val favoriteAlbumIds = favoriteReviews.map { it.albumId }.toSet()
                            allAlbums.filter { favoriteAlbumIds.contains(it.id) }
                        }.collect { favoritedAlbums ->
                            catalogAdapter.submitList(favoritedAlbums)
                        }
                    }
                    "plan" -> {
                        viewModel.wantToListenAlbumsState.collect { catalogAdapter.submitList(it) }
                    }
                    "logged" -> {
                        combine(
                            viewModel.allAlbumsState,
                            viewModel.lastListensState
                        ) { allAlbums, userReviews ->
                            val reviewedAlbumIds = userReviews.map { it.albumId }.toSet()
                            allAlbums.filter { reviewedAlbumIds.contains(it.id) }
                        }.collect { loggedAlbums ->
                            catalogAdapter.submitList(loggedAlbums)
                        }
                    }
                }
            }
        }
    }
}