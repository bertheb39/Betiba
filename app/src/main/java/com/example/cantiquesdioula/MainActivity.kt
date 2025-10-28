package com.example.cantiquesdioula

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigationrail.NavigationRailView

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private var searchView: SearchView? = null
    private var bottomNav: BottomNavigationView? = null
    private var navRail: NavigationRailView? = null

    private var currentToolbarTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        // Appliquer le thème sauvegardé
        val savedTheme = SettingsManager.getTheme(this)
        SettingsManager.applyTheme(savedTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        searchView = findViewById(R.id.search_view)
        bottomNav = findViewById(R.id.bottom_navigation)
        navRail = findViewById(R.id.navigation_rail)

        // Connexion de la barre de recherche
        searchView?.let { setupSearch(it) }

        val navigationListener = NavigationBarView.OnItemSelectedListener { item ->
            handleNavigation(item.itemId)
            true
        }

        bottomNav?.setOnItemSelectedListener(navigationListener)
        navRail?.setOnItemSelectedListener(navigationListener)

        // Charger les données utilisateur (Favoris, Maîtrisés) de manière asynchrone
        FavoritesManager.loadFavorites(this) {
            Log.d("MainActivity", "Chargement des favoris terminé.")
            refreshChildLists()
        }
        MasteredManager.loadMastered(this) {
            Log.d("MainActivity", "Chargement des 'maîtrisés' terminé.")
            refreshChildLists()
        }

        if (savedInstanceState == null) {
            handleNavigation(R.id.nav_home)
        }
    }

    override fun onResume() {
        super.onResume()
        val selectedItemId = bottomNav?.selectedItemId ?: navRail?.selectedItemId ?: R.id.nav_home

        handleNavigationVisibility(selectedItemId)
        updateToolbarTitle(selectedItemId)

        // Rafraîchit les listes (pour mettre à jour les icônes) et réapplique le filtre si nécessaire
        refreshChildLists()
    }

    // Fonction centralisée pour rafraîchir les listes dans les fragments enfants
    private fun refreshChildLists() {
        // runOnUiThread est crucial car nous sommes probablement appelés depuis un thread de Firestore
        runOnUiThread {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (fragment is HomeFragment) {
                fragment.refreshLists()

                // !! SOLUTION PROBABLE POUR LE BUG DE RECHERCHE PERSISTANT !!
                // Réapplique le filtre en cours quand la liste est rafraîchie
                searchView?.let { search ->
                    if (!search.isIconified) {
                        fragment.filterList(search.query.toString())
                    }
                }
            }
        }
    }

    private fun handleNavigation(itemId: Int) {
        handleNavigationVisibility(itemId)
        updateToolbarTitle(itemId)

        val fragment = when (itemId) {
            R.id.nav_home -> HomeFragment()
            R.id.nav_categories -> CategoriesFragment()
            R.id.nav_menu -> MenuFragment()
            else -> HomeFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateToolbarTitle(itemId: Int) {
        val title = when (itemId) {
            R.id.nav_home -> getString(R.string.app_name)
            R.id.nav_categories -> getString(R.string.nav_categories)
            R.id.nav_menu -> getString(R.string.nav_menu)
            else -> getString(R.string.app_name)
        }
        currentToolbarTitle = title
        findViewById<TextView>(R.id.toolbar_title).text = title
    }

    private fun handleNavigationVisibility(itemId: Int) {
        val showSearch = itemId == R.id.nav_home
        toolbar.visibility = View.VISIBLE
        searchView?.visibility = if (showSearch) View.VISIBLE else View.GONE
    }

    // Fonction de connexion de la barre de recherche (le cœur du filtrage)
    private fun setupSearch(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                // Cette logique est la clé pour que le filtrage démarre
                val homeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

                if (homeFragment is HomeFragment) {
                    homeFragment.filterList(newText)
                }
                return true
            }
        })
    }
}