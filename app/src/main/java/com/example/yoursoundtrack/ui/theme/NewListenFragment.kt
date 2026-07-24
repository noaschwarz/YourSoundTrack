package com.example.yoursoundtrack.ui.theme

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.adapters.SearchAlbumAdapter
import com.example.yoursoundtrack.dataModel.Album
import com.example.yoursoundtrack.managers.loadAlbumCover
import kotlinx.coroutines.launch

class NewListenFragment : Fragment(R.layout.fragment_new_listen) {

    private val viewModel: MusicViewModel by activityViewModels()

    private lateinit var overlaySearch: LinearLayout
    private lateinit var layoutReviewForm: LinearLayout

    private lateinit var adapter: SearchAlbumAdapter
    private var allAlbums: List<Album> = emptyList()
    private var selectedAlbum: Album? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        overlaySearch = view.findViewById(R.id.layout_album_search_overlay)
        layoutReviewForm = view.findViewById(R.id.layout_main_review_form)

        overlaySearch.visibility = View.VISIBLE //first show search

        setupSearchOverlay(view)
        setupReviewForm(view)
    }

    private fun setupSearchOverlay(view: View) {
        val searchView = view.findViewById<SearchView>(R.id.search_view_album)
        val rvSearch = view.findViewById<RecyclerView>(R.id.rv_search_results)

        adapter = SearchAlbumAdapter { album ->
            selectedAlbum = album
            bindReviewForm(view, album)

            overlaySearch.visibility = View.GONE
            layoutReviewForm.visibility = View.VISIBLE // move on to rate
        }

        rvSearch.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NewListenFragment.adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allAlbumsState.collect { albums ->
                    allAlbums = albums
                    adapter.submitList(albums)
                }
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty().trim()
                val filtered = if (query.isEmpty()) {
                    allAlbums
                } else {
                    allAlbums.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.artist.contains(query, ignoreCase = true)
                    }
                }
                adapter.submitList(filtered)
                return true
            }
        })
    }

    private fun bindReviewForm(view: View, album: Album) {
        val tvName = view.findViewById<TextView>(R.id.text_selected_name)
        val ivCover = view.findViewById<ImageView>(R.id.iv_review_album_cover)

        tvName.text = "${album.title} - ${album.artist}"
        ivCover.loadAlbumCover(album.coverUrl)
    }

    private fun setupReviewForm(view: View) {
        val btnSave = view.findViewById<Button>(R.id.btn_save_entry)
        val ratingBar = view.findViewById<RatingBar>(R.id.rating_bar)
        val etReview = view.findViewById<EditText>(R.id.et_review_input)

        btnSave.setOnClickListener {
            val rating = ratingBar.rating
            val reviewText = etReview.text.toString()

            if (selectedAlbum != null) {
                Toast.makeText(requireContext(), "Saved rating for ${selectedAlbum?.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}