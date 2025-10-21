package com.example.cantiquesdioula

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomeViewPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {
    // Garde une référence aux fragments pour y accéder plus tard
    private val fragments = mutableMapOf<Int, Fragment>()

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = if (position == 0) AllSongsFragment() else NumbersFragment()
        fragments[position] = fragment
        return fragment
    }

    fun getFragment(position: Int): Fragment? = fragments[position]
}