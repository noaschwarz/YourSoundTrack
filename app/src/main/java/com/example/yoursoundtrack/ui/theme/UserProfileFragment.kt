package com.example.yoursoundtrack.ui.theme

import android.os.Bundle
import android.view.View
import android.widget.TextView
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var topFiveAdapter: AlbumAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        view.findViewById<TextView>(R.id.text_username_label)?.text =
            currentUser?.displayName ?: currentUser?.email ?: "Music Lover"

        setupRecyclerView(view)
    }

    private fun setupRecyclerView(view: View) {
        topFiveAdapter = AlbumAdapter { album ->
            val bundle = Bundle().apply { putString("albumId", album.id) }
            findNavController().navigate(R.id.navigation_album_detail, bundle)
        }

        view.findViewById<RecyclerView>(R.id.rv_top_five)?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = topFiveAdapter
        }
    }
}