package com.example.yoursoundtrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.dataModel.Album

class SearchAlbumAdapter(
    private val onAlbumClick: (Album) -> Unit
) : ListAdapter<Album, SearchAlbumAdapter.SearchViewHolder>(SearchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album_search, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_album_title)
        private val tvArtist: TextView = itemView.findViewById(R.id.tv_artist_name)

        fun bind(album: Album) {
            tvTitle.text = album.title
            tvArtist.text = album.artist

            itemView.setOnClickListener {
                onAlbumClick(album)
            }
        }
    }

    class SearchDiffCallback : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean = oldItem == newItem
    }
}