package com.example.cantiquesdioula

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

        val toolbar: Toolbar = findViewById(R.id.song_list_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Affiche la flèche de retour

        listType = intent.getStringExtra("LIST_TYPE")
        recyclerView = findViewById(R.id.song_list_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        allSongs = loadSongsFromAssets()

        // Initialiser l'adaptateur avec une liste vide pour l'instant
        songAdapter = SongAdapter(listOf())
        recyclerView.adapter = songAdapter
    }

    // Cette fonction est appelée chaque fois que l'écran redevient visible
    override fun onResume() {
        super.onResume()
        // On met à jour la liste ici pour qu'elle soit toujours correcte
        updateContent()
    }

    private fun updateContent() {
        var songsToShow: List<Song> = listOf()
        if (listType == "FAVORITES") {
            supportActionBar?.title = "Favoris"
            songsToShow = FavoritesManager.getFavoriteSongs(this, allSongs)
        } else if (listType == "MASTERED") {
            supportActionBar?.title = "Cantiques maîtrisés"
            songsToShow = MasteredManager.getMasteredSongs(this, allSongs)
        }
        songAdapter.updateList(songsToShow)
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