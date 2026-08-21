package com.example.yoursoundtrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.dataModel.Album
import com.example.yoursoundtrack.managers.loadAlbumCover

class AlbumAdapter(
    private val onAlbumClick: (Album) -> Unit
) : ListAdapter<Album, AlbumAdapter.AlbumViewHolder>(AlbumDiffCallback()) {

    //album layout for each row
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }
    //binds the album to position
    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    //class to cache view ref
    inner class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover = itemView.findViewById<ImageView>(R.id.iv_album_cover)

        fun bind(album: Album) {
            ivCover.loadAlbumCover(album.coverUrl)

            itemView.setOnClickListener {
                onAlbumClick(album)
            }
        }
    }
    //diffutil callback to opt list
    class AlbumDiffCallback : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean = oldItem == newItem
    }
}