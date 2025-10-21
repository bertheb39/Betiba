package com.example.cantiquesdioula

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.InputStreamReader

class NumbersFragment : Fragment(), FilterableFragment {
    private lateinit var numberAdapter: NumberAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_numbers, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_numbers)

        val allSongs = loadSongsFromAssets()
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 5)
        numberAdapter = NumberAdapter(allSongs)
        recyclerView.adapter = numberAdapter

        return view
    }

    override fun filter(query: String?) {
        if (::numberAdapter.isInitialized) {
            numberAdapter.filter.filter(query)
        }
    }

    // --- AJOUT DE LA FONCTION MANQUANTE ---
    fun refreshList() {
        // Pour l'instant, cette fonction ne fait rien ici, mais elle est nécessaire pour éviter l'erreur.
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