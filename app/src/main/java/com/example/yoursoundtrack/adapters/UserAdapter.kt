package com.example.yoursoundtrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.dataModel.UserProfile

class UserAdapter(
    private var users: List<UserProfile> = emptyList(),
    private val onUserClick: (UserProfile) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    fun submitList(newList: List<UserProfile>) {
        users = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.text_username)
        private val tvEmail: TextView = itemView.findViewById(R.id.text_email)

        fun bind(user: UserProfile) {
            tvUsername.text = user.username.ifEmpty { "Music Lover" }
            tvEmail.text = user.email
            itemView.setOnClickListener { onUserClick(user) }
        }
    }
}