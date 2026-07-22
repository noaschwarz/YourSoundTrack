package com.example.yoursoundtrack.ui.theme

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.adapters.AlbumAdapter
import com.example.yoursoundtrack.dataModel.Album
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: MusicViewModel by viewModels()

    private lateinit var upcomingAdapter: AlbumAdapter
    private lateinit var popularAdapter: AlbumAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews(view)
        observeViewModel()
    }

    private fun setupRecyclerViews(view: View) {
        val onAlbumClick: (Album) -> Unit = { album ->
            val bundle = Bundle().apply {
                putString("albumId", album.id)
            }
            findNavController().navigate(R.id.navigation_album_detail, bundle)
        }

        upcomingAdapter = AlbumAdapter(onAlbumClick)
        popularAdapter = AlbumAdapter(onAlbumClick)

        // Switch to Grid (3 columns) and disable nested scrolling inside ScrollView
        view.findViewById<RecyclerView>(R.id.rv_upcoming_releases)?.apply {
            layoutManager = GridLayoutManager(context, 3)
            isNestedScrollingEnabled = false
            adapter = upcomingAdapter
        }

        view.findViewById<RecyclerView>(R.id.rv_popular_this_week)?.apply {
            layoutManager = GridLayoutManager(context, 3)
            isNestedScrollingEnabled = false
            adapter = popularAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Observe 2026 Upcoming Releases
                launch {
                    viewModel.upcomingReleasesState.collect { upcomingAlbums ->
                        upcomingAdapter.submitList(upcomingAlbums)
                    }
                }

                // Observe Popular This Week
                launch {
                    viewModel.popularThisWeekState.collect { popularAlbums ->
                        popularAdapter.submitList(popularAlbums)
                    }
                }

            }
        }
    }
}