package com.example.cantiquesdioula

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Appliquer le thème sauvegardé
        val savedTheme = SettingsManager.getTheme(this)
        SettingsManager.applyTheme(savedTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val searchView: SearchView = findViewById(R.id.search_view)

        // Afficher le fragment d'accueil au démarrage
        if (savedInstanceState == null) {
            // --- CORRECTION ICI ---
            // On utilise la ressource string au lieu d'un texte fixe
            loadFragment(HomeFragment(), getString(R.string.app_name))
        }

        // Gérer les clics de la barre de navigation
        bottomNav.setOnItemSelectedListener { item ->
            val showSearch = item.itemId == R.id.nav_home
            searchView.visibility = if (showSearch) View.VISIBLE else View.GONE

            when (item.itemId) {
                R.id.nav_home -> {
                    // --- CORRECTION ICI ---
                    loadFragment(HomeFragment(), getString(R.string.app_name))
                }
                R.id.nav_categories -> {
                    loadFragment(CategoriesFragment(), "Catégories")
                }
                R.id.nav_menu -> {
                    loadFragment(MenuFragment(), "Menu")
                }
            }
            true
        }

        setupSearch(searchView)
    }

    private fun setupSearch(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (fragment is HomeFragment) {
                    fragment.filterList(newText)
                }
                return true
            }
        })
    }

    private fun loadFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        findViewById<TextView>(R.id.toolbar_title).text = title
    }
}