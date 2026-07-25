package com.example.yoursoundtrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.dataModel.Review

class LastListensAdapter(
    private var reviews: List<Review> = emptyList(),
    private val onItemClick: (Review) -> Unit
) : RecyclerView.Adapter<LastListensAdapter.ReviewViewHolder>() {

    fun submitList(newList: List<Review>) {
        reviews = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_last_listen, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.bind(review)
    }

    override fun getItemCount(): Int = reviews.size

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCover: ImageView = itemView.findViewById(R.id.iv_album_cover)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_album_title)
        private val tvArtist: TextView = itemView.findViewById(R.id.tv_album_artist)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.rating_bar)
        private val tvReviewText: TextView = itemView.findViewById(R.id.tv_review_text)

        fun bind(review: Review) {
            tvTitle.text = review.albumTitle
            tvArtist.text = review.albumArtist
            ratingBar.rating = review.rating
            tvReviewText.text = review.textReview

            Glide.with(itemView.context)
                .load(review.albumCoverUrl)
                .placeholder(R.drawable.singer_icon)
                .into(ivCover)

            itemView.setOnClickListener { onItemClick(review) }
        }
    }
}