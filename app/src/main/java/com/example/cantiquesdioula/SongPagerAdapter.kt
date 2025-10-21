package com.example.cantiquesdioula

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

// L'adaptateur a besoin de savoir dans quelle Activity il se trouve
// et quelle est la liste complète des cantiques à afficher.
class SongPagerAdapter(
    fragmentActivity: FragmentActivity, // L'Activity qui contient le ViewPager2
    private val songs: List<Song>       // La liste des cantiques
) : FragmentStateAdapter(fragmentActivity) {

    // Cette fonction dit combien de pages (cantiques) il y a au total.
    override fun getItemCount(): Int {
        return songs.size // Le nombre total de pages est égal au nombre de cantiques
    }

    // Cette fonction est appelée par le ViewPager2 quand il a besoin
    // d'afficher une page spécifique (à une certaine position).
    override fun createFragment(position: Int): Fragment {
        // 1. Récupérer le cantique correspondant à cette position dans la liste.
        val song = songs[position]

        // 2. Créer une nouvelle instance de SongDetailFragment pour ce cantique.
        //    C'est ici qu'on utilise la fonction `newInstance` qu'on avait créée ! ✨
        return SongDetailFragment.newInstance(song)
    }
}