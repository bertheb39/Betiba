package com.example.cantiquesdioula

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SongPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val songs: List<Song>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return songs.size
    }

    override fun createFragment(position: Int): Fragment {
        val song = songs[position]
        // --- MODIFICATION : Passer aussi le nombre total ---
        return SongDetailFragment.newInstance(song, itemCount) // <<< itemCount AJOUTÉ ICI
    }
}