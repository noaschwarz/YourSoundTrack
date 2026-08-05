package com.example.yoursoundtrack.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.yoursoundtrack.fragments.UserListsFragment
import com.example.yoursoundtrack.fragments.UserProfileFragment

class UserPageAdapter(fragment: Fragment, private val userId: String? = null) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val bundle = Bundle().apply { putString("userId", userId) }
        return when (position) {
            0 -> UserProfileFragment().apply { arguments = bundle }
            1 -> UserListsFragment().apply { arguments = bundle }
            else -> UserProfileFragment().apply { arguments = bundle }
        }
    }
}
