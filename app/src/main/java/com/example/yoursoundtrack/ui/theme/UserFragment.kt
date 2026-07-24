package com.example.yoursoundtrack.ui.theme

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.yoursoundtrack.R
import com.example.yoursoundtrack.adapters.UserPageAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class UserFragment : Fragment(R.layout.fragment_user) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout_user)
        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager_user)

        viewPager.adapter = UserPageAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.profile)
                1 -> getString(R.string.lists)
                else -> ""
            }
        }.attach()
    }
}