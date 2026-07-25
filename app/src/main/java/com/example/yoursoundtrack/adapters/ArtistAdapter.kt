package com.example.yoursoundtrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.dataModel.Artist

class ArtistAdapter(
    private var artists: List<Artist> = emptyList(),
    private val onItemClick: (Artist) -> Unit
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    fun submitList(newList: List<Artist>) {
        artists = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]
        holder.bind(artist)
    }

    override fun getItemCount(): Int = artists.size

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivArtistImage: ImageView = itemView.findViewById(R.id.iv_artist_image)
        private val tvArtistName: TextView = itemView.findViewById(R.id.tv_artist_name)

        fun bind(artist: Artist) {
            tvArtistName.text = artist.name

            Glide.with(itemView.context)
                .load(artist.imageUrl)
                .placeholder(R.drawable.singer_icon)
                .into(ivArtistImage)

            itemView.setOnClickListener { onItemClick(artist) }
        }
    }
}