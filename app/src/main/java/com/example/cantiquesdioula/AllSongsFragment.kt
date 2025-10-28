package com.example.cantiquesdioula

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class AllSongsFragment : Fragment(), FilterableFragment {

    private lateinit var songAdapter: SongAdapter
    private var allSongsList: List<Song> = emptyList()

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_songs, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_all_songs)
        progressBar = view.findViewById(R.id.progress_bar_all)

        // Initialiser l'adaptateur avec une liste vide
        songAdapter = SongAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = songAdapter

        // Lancer le chargement des données
        loadSongsData()

        return view
    }

    private fun loadSongsData() {
        // Utiliser 'lifecycleScope.launch' pour un travail asynchrone
        lifecycleScope.launch {
            // Si les données sont déjà chargées (ex: rotation), on les affiche juste
            if (allSongsList.isNotEmpty()) {
                songAdapter.setFullList(allSongsList) // Met à jour le filtre ET la liste
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                return@launch
            }

            // Afficher le chargement
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            // Aller sur le Thread IO (Input/Output) pour lire le gros fichier JSON
            val songs = withContext(Dispatchers.IO) {
                loadSongsFromAssets()
            }

            // Revenir sur le Thread Principal (Main) pour afficher l'interface
            allSongsList = songs
            songAdapter.setFullList(allSongsList) // Met à jour le filtre ET la liste

            // Cacher le chargement et afficher la liste
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    // Fonction appelée par MainActivity pour lancer le filtre
    override fun filter(query: String?) {
        if (::songAdapter.isInitialized) {
            songAdapter.filter.filter(query)
        }
    }

    // Fonction appelée par MainActivity pour rafraîchir les favoris/maîtrisés
    fun refreshList() {
        if (::songAdapter.isInitialized && isAdded) {
            songAdapter.notifyDataSetChanged() // Force la mise à jour des icônes
        }
    }

    private fun loadSongsFromAssets(): List<Song> {
        return try {
            val inputStream = requireContext().assets.open("CAD.json")
            val reader = InputStreamReader(inputStream)
            // Utilise la structure SongData pour parser le JSON
            val songData = Gson().fromJson(reader, SongData::class.java)
            songData.songs.sortedBy { it.id } // S'assurer qu'ils sont triés
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}