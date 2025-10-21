package com.example.cantiquesdioula

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: HomeViewPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout_home)
        viewPager = view.findViewById(R.id.view_pager_home)

        // On utilise childFragmentManager car ce fragment gère d'autres fragments
        adapter = HomeViewPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "TOUS" else "NUMÉROS"
        }.attach()

        return view
    }

    // Fonction qui reçoit le texte de la recherche et le transmet
    fun filterList(query: String?) {
        val fragment = childFragmentManager.fragments.getOrNull(viewPager.currentItem)
        (fragment as? FilterableFragment)?.filter(query)
    }

    // --- AJOUT DE LA FONCTION MANQUANTE ---
    // Fonction qui rafraîchit les listes dans les onglets (pour les favoris/maîtrisés)
    fun refreshLists() {
        // On demande à chaque fragment enfant de se rafraîchir
        (childFragmentManager.fragments.find { it is AllSongsFragment } as? AllSongsFragment)?.refreshList()
        (childFragmentManager.fragments.find { it is NumbersFragment } as? NumbersFragment)?.refreshList()
    }
}