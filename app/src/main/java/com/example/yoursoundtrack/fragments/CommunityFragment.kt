package com.example.yoursoundtrack.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.adapters.FriendActivityAdapter
import com.example.yoursoundtrack.dataModel.Review
import com.example.yoursoundtrack.managers.MusicViewModel
import kotlinx.coroutines.launch

class CommunityFragment : Fragment() {

    private lateinit var listensAdapter: FriendActivityAdapter
    private lateinit var reviewsAdapter: FriendActivityAdapter

    private val viewModel: MusicViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)
        setupRecyclerViews(view)
        setupStaticButtons(view)
        observeViewModel()
        return view
    }

    private fun setupRecyclerViews(view: View) {
        val onItemClick: (Review) -> Unit = { review ->
            val bundle = Bundle().apply {
                putString("reviewId", review.id)
                putString("albumId", review.albumId)
            }
            findNavController().navigate(R.id.action_navigation_community_to_navigation_review_detail, bundle)
        }

        listensAdapter = FriendActivityAdapter(onItemClick = onItemClick)
        reviewsAdapter = FriendActivityAdapter(onItemClick = onItemClick)

        view.findViewById<RecyclerView>(R.id.rv_friends_listens)?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = listensAdapter
        }

        view.findViewById<RecyclerView>(R.id.rv_friends_reviews)?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = reviewsAdapter
        }
    }

    private fun setupStaticButtons(view: View) {
        view.findViewById<View>(R.id.btn_popular_new_reviews)?.setOnClickListener {
            Toast.makeText(context, "Popular Reviews feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btn_popular_community_lists)?.setOnClickListener {
            Toast.makeText(context, "Community Lists feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.friendsListensState.collect { plainListens ->
                        listensAdapter.submitList(plainListens)
                    }
                }

                launch {
                    viewModel.friendsReviewsState.collect { textReviews ->
                        reviewsAdapter.submitList(textReviews)
                    }
                }

            }
        }
    }
}