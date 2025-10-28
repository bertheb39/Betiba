package com.example.cantiquesdioula

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar // <-- AJOUT
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

    // --- AJOUT DES RÉFÉRENCES ---
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    // --- FIN AJOUT ---

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_songs, container, false)

        // --- DÉBUT MODIFICATION ---
        // On récupère les références
        recyclerView = view.findViewById(R.id.recycler_view_all_songs)
        progressBar = view.findViewById(R.id.progress_bar_all)
        // --- FIN MODIFICATION ---

        // 1. Initialiser l'adaptateur avec une liste VIDE
        songAdapter = SongAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = songAdapter

        // 2. Lancer le chargement des données
        loadSongsData()

        return view
    }

    private fun loadSongsData() {
        lifecycleScope.launch {
            if (allSongsList.isNotEmpty()) {
                songAdapter.setFullList(allSongsList)
                songAdapter.updateList(allSongsList)

                // --- AJOUT ---
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                // --- FIN AJOUT ---
                return@launch
            }

            // --- AJOUT (Pendant le chargement) ---
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            // --- FIN AJOUT ---

            val songs = withContext(Dispatchers.IO) {
                loadSongsFromAssets()
            }

            allSongsList = songs
            songAdapter.setFullList(allSongsList)
            songAdapter.updateList(allSongsList)

            // --- AJOUT (Quand le chargement est fini) ---
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            // --- FIN AJOUT ---
        }
    }

    override fun filter(query: String?) {
        if (::songAdapter.isInitialized) {
            songAdapter.filter.filter(query)
        }
    }

    fun refreshList() {
        if (::songAdapter.isInitialized && isAdded) {
            songAdapter.notifyDataSetChanged()
        }
    }

    private fun loadSongsFromAssets(): List<Song> {
        return try {
            val inputStream = requireContext().assets.open("CAD.json")
            val reader = InputStreamReader(inputStream)
            val songData = Gson().fromJson(reader, SongData::class.java)
            songData.songs.sortedBy { it.id }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}