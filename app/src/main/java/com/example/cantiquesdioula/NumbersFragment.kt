package com.example.cantiquesdioula

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope // <-- AJOUT
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers // <-- AJOUT
import kotlinx.coroutines.launch // <-- AJOUT
import kotlinx.coroutines.withContext // <-- AJOUT
import java.io.InputStreamReader

class NumbersFragment : Fragment(), FilterableFragment {

    private lateinit var numberAdapter: NumberAdapter
    // On garde la liste en mémoire ici
    private var allSongsList: List<Song> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_numbers, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_numbers)

        // --- DÉBUT DE LA MODIFICATION ---

        // 1. Initialiser l'adaptateur avec une liste VIDE
        // L'affichage est instantané.
        numberAdapter = NumberAdapter(emptyList())
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 5)
        recyclerView.adapter = numberAdapter

        // 2. Lancer le chargement des données en arrière-plan
        loadSongsData()

        // --- FIN DE LA MODIFICATION ---

        return view
    }

    // --- DÉBUT DES AJOUTS ---
    private fun loadSongsData() {
        // Lance une coroutine liée au cycle de vie du fragment
        lifecycleScope.launch {
            // Si la liste est déjà chargée, on l'affiche
            if (allSongsList.isNotEmpty()) {
                numberAdapter.updateSongs(allSongsList) // (Nous supposons que cette méthode existe)
                return@launch
            }

            // 1. (TRAVAIL EN ARRIÈRE-PLAN)
            // On bascule sur le thread IO (Input/Output) pour lire le fichier
            val songs = withContext(Dispatchers.IO) {
                loadSongsFromAssets() // Appel en arrière-plan
            }

            // 2. (RETOUR SUR LE THREAD PRINCIPAL)
            // On affiche la liste
            allSongsList = songs
            numberAdapter.updateSongs(allSongsList)
        }
    }
    // --- FIN DES AJOUTS ---


    override fun filter(query: String?) {
        if (::numberAdapter.isInitialized) {
            numberAdapter.filter.filter(query)
        }
    }

    fun refreshList() {
        // Cette fonction n'a rien besoin de faire pour les numéros,
        // car ils n'affichent pas les favoris.
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