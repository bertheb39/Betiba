package com.example.cantiquesdioula

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // 1. Configuration de la barre d'outils (Toolbar)
        val toolbar: Toolbar = findViewById(R.id.menu_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Menu"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Ajoute la flèche de retour

        // 2. Préparation de la liste des options avec titres ET icônes
        // C'est cette liste qui est maintenant correcte pour l'adaptateur
        val menuItems = listOf(
            MenuItemData("Paramètres", R.drawable.ic_settings),
            MenuItemData("Favoris", R.drawable.ic_favorite_filled),
            MenuItemData("Cantiques maîtrisés", R.drawable.ic_check_circle),
            MenuItemData("Partager l'application", R.drawable.ic_share),
            MenuItemData("Commentaires et avis", R.drawable.ic_rate_review),
            MenuItemData("Faire un don", R.drawable.ic_donate),
            MenuItemData("À propos", R.drawable.ic_info),
            MenuItemData("Contactez-nous", R.drawable.ic_contact)
        )

        // 3. Configuration du RecyclerView pour afficher la liste
        val recyclerView: RecyclerView = findViewById(R.id.menu_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // On crée l'adaptateur avec la bonne liste (menuItems) et la logique de clic
        recyclerView.adapter = MenuAdapter(menuItems) { selectedItem ->
            // Gérer le clic sur un élément du menu
            when (selectedItem.title) {
                "Favoris" -> {
                    val intent = Intent(this, SongListActivity::class.java).apply {
                        putExtra("LIST_TYPE", "FAVORITES")
                    }
                    startActivity(intent)
                }
                "Cantiques maîtrisés" -> {
                    val intent = Intent(this, SongListActivity::class.java).apply {
                        putExtra("LIST_TYPE", "MASTERED")
                    }
                    startActivity(intent)
                }
                // Ajoutez d'autres 'when' pour les autres options...
            }
        }
    }

    // 4. Gérer le clic sur la flèche de retour dans la barre d'outils
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed() // Revient à l'écran précédent
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}