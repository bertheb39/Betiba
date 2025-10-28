package com.example.cantiquesdioula

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar // <-- AJOUT
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class NumbersFragment : Fragment(), FilterableFragment {

    private lateinit var numberAdapter: NumberAdapter
    private var allSongsList: List<Song> = emptyList()

    // --- AJOUT DES RÉFÉRENCES ---
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    // --- FIN AJOUT ---

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_numbers, container, false)

        // --- DÉBUT MODIFICATION ---
        recyclerView = view.findViewById(R.id.recycler_view_numbers)
        progressBar = view.findViewById(R.id.progress_bar_numbers)
        // --- FIN MODIFICATION ---

        // 1. Initialiser l'adaptateur avec une liste VIDE
        numberAdapter = NumberAdapter(emptyList())
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 5)
        recyclerView.adapter = numberAdapter

        // 2. Lancer le chargement des données
        loadSongsData()

        return view
    }

    private fun loadSongsData() {
        lifecycleScope.launch {
            if (allSongsList.isNotEmpty()) {
                numberAdapter.updateSongs(allSongsList)
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
            numberAdapter.updateSongs(allSongsList)

            // --- AJOUT (Quand le chargement est fini) ---
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            // --- FIN AJOUT ---
        }
    }


    override fun filter(query: String?) {
        if (::numberAdapter.isInitialized) {
            numberAdapter.filter.filter(query)
        }
    }

    fun refreshList() {
        // Rien à faire ici
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