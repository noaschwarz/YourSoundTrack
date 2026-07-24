package com.example.yoursoundtrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yoursoundtrack.R

class TrackAdapter(private val tracks: List<String>) :
    RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTrackTitle: TextView = view.findViewById(R.id.tv_track_title)
        val tvTrackNumber: TextView? = view.findViewById(R.id.tv_track_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val trackTitle = tracks[position]
        holder.tvTrackTitle.text = trackTitle
        holder.tvTrackNumber?.text = "${position + 1}."
    }

    override fun getItemCount(): Int = tracks.size
}