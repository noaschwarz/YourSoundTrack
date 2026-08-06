package com.example.yoursoundtrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.dataModel.Artist

class ArtistAdapter(
    private val onItemClick: (Artist) -> Unit
) : ListAdapter<Artist, ArtistAdapter.ArtistViewHolder>(ArtistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivArtistImage: ImageView = itemView.findViewById(R.id.iv_artist_image)
        private val tvArtistName: TextView = itemView.findViewById(R.id.tv_artist_name)

        fun bind(artist: Artist) {
            tvArtistName.text = artist.name.ifBlank { artist.id }

            if (artist.imageUrl.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(artist.imageUrl)
                    .placeholder(R.drawable.singer_icon)
                    .error(R.drawable.singer_icon)
                    .into(ivArtistImage)
            } else {
                ivArtistImage.setImageResource(R.drawable.singer_icon)
            }

            itemView.setOnClickListener { onItemClick(artist) }
        }
    }

    class ArtistDiffCallback : DiffUtil.ItemCallback<Artist>() {
        override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean =
            oldItem == newItem
    }
}