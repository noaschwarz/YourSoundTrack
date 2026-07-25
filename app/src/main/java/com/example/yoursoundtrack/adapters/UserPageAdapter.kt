package com.example.yoursoundtrack.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.yoursoundtrack.fragments.UserListsFragment
import com.example.yoursoundtrack.fragments.UserProfileFragment

class UserPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserProfileFragment()
            1 -> UserListsFragment()
            else -> UserProfileFragment()
        }
    }
}