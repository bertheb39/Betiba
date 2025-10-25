package com.example.cantiquesdioula

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.InputStreamReader

class SongListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private var listType: String? = null
    private var allSongs: List<Song> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_list)

        // --- CORRIGÉ : Utilisation des bons IDs ---
        val toolbar: Toolbar = findViewById(R.id.song_list_toolbar)
        recyclerView = findViewById(R.id.song_list_recycler_view)
        // --- FIN CORRECTION ---

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Affiche la flèche de retour

        listType = intent.getStringExtra("LIST_TYPE")
        recyclerView.layoutManager = LinearLayoutManager(this)

        allSongs = loadSongsFromAssets()

        // --- CORRIGÉ : Initialisation de votre adaptateur ---
        // On l'initialise vide, car on le met à jour dans onResume
        songAdapter = SongAdapter(listOf())
        recyclerView.adapter = songAdapter
        // --- FIN CORRECTION ---
    }

    // Cette fonction est appelée chaque fois que l'écran redevient visible
    override fun onResume() {
        super.onResume()
        // On met à jour la liste ici pour qu'elle soit toujours correcte
        updateContent()
    }

    private fun updateContent() {
        val songsToShow: List<Song>
        if (listType == "FAVORITES") {
            supportActionBar?.title = getString(R.string.menu_favorites)
            // --- CORRECTION : Nouvelle logique de filtrage Firebase ---
            songsToShow = allSongs.filter { FavoritesManager.isFavorite(it.id) }
            // --- FIN CORRECTION ---
        } else if (listType == "MASTERED") {
            supportActionBar?.title = getString(R.string.menu_mastered)
            // --- CORRECTION : Nouvelle logique de filtrage Firebase ---
            songsToShow = allSongs.filter { MasteredManager.isMastered(it.id) }
            // --- FIN CORRECTION ---
        } else {
            songsToShow = allSongs // Par sécurité
        }

        // --- CORRECTION : Utilisation de votre méthode setFullList ---
        // On passe la liste filtrée pour l'affichage
        // et on dit à l'adaptateur quelle est la "vraie" liste complète
        // pour que le clic vers le Pager fonctionne.
        songAdapter.setFullList(allSongs) // Important pour le Pager
        songAdapter.updateList(songsToShow) // Affiche la liste filtrée
    }

    // Gère le clic sur la flèche de retour
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadSongsFromAssets(): List<Song> {
        return try {
            val inputStream = assets.open("CAD.json")
            val reader = InputStreamReader(inputStream)
            val songData = Gson().fromJson(reader, SongData::class.java)
            songData.songs.sortedBy { it.id }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}