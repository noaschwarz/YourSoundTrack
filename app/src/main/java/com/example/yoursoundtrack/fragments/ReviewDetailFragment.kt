package com.example.yoursoundtrack.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.managers.MusicViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewDetailFragment : Fragment(R.layout.fragment_review_detail) {

    private val viewModel: MusicViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val reviewId = arguments?.getString("reviewId") ?: return

        val review = viewModel.friendsAllActivityState.value.find { it.id == reviewId }
            ?: viewModel.userReviews.value?.find { it.id == reviewId }
            ?: viewModel.lastListensState.value.find { it.id == reviewId }

        review?.let { item ->
            view.findViewById<TextView>(R.id.tv_detail_title).text = item.albumTitle
            view.findViewById<TextView>(R.id.tv_detail_artist).text = item.albumArtist
            view.findViewById<TextView>(R.id.tv_detail_review_text).text = item.textReview.ifEmpty { "No text review provided." }
            view.findViewById<RatingBar>(R.id.rating_detail).rating = item.rating
            view.findViewById<View>(R.id.btn_back_review_detail)?.setOnClickListener {
                findNavController().navigateUp()
            }

            val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
            val formattedDate = sdf.format(Date(item.timestamp))
            view.findViewById<TextView>(R.id.tv_detail_timestamp).text = "Logged on: $formattedDate"

            val ivCover = view.findViewById<ImageView>(R.id.iv_detail_cover)
            Glide.with(this)
                .load(item.albumCoverUrl)
                .placeholder(R.drawable.singer_icon)
                .into(ivCover)
        }
    }
}