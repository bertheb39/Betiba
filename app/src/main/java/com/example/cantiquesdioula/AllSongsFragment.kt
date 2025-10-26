package com.example.cantiquesdioula

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope // <-- Import important
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers // <-- Import important
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class AllSongsFragment : Fragment(), FilterableFragment {

    private lateinit var songAdapter: SongAdapter
    private var allSongsList: List<Song> = emptyList() // Garde la liste en mémoire ici

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_songs, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_all_songs)

        // 1. Initialiser l'adaptateur avec une liste VIDE
        // L'affichage est instantané.
        songAdapter = SongAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = songAdapter

        // 2. Lancer le chargement des données en arrière-plan
        loadSongsData()

        return view
    }

    private fun loadSongsData() {
        // Lance une coroutine liée au cycle de vie du fragment
        lifecycleScope.launch {
            // Si la liste est déjà chargée (ex: changement d'orientation), on ne fait rien
            if (allSongsList.isNotEmpty()) {
                songAdapter.setFullList(allSongsList)
                songAdapter.updateList(allSongsList)
                return@launch
            }

            // 1. (TRAVAIL EN ARRIÈRE-PLAN)
            // On bascule sur le thread IO (Input/Output) pour lire le fichier et l'analyser
            val songs = withContext(Dispatchers.IO) {
                loadSongsFromAssets() // Cette fonction est maintenant appelée en arrière-plan
            }

            // 2. (RETOUR SUR LE THREAD PRINCIPAL)
            // withContext est terminé, nous sommes de retour sur le thread principal (UI)
            allSongsList = songs // On sauvegarde la liste
            songAdapter.setFullList(allSongsList) // On dit à l'adaptateur quelle est la liste complète (pour les clics)
            songAdapter.updateList(allSongsList) // On affiche la liste
        }
    }

    override fun filter(query: String?) {
        if (::songAdapter.isInitialized) {
            songAdapter.filter.filter(query)
        }
    }

    fun refreshList() {
        if (::songAdapter.isInitialized && isAdded) {
            // Il suffit de notifier l'adaptateur que les données (favoris) ont changé
            songAdapter.notifyDataSetChanged()
        }
    }

    // Cette fonction ne change pas, mais elle est maintenant
    // appelée depuis un thread d'arrière-plan
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