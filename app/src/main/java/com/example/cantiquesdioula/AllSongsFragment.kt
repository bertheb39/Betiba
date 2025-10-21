package com.example.cantiquesdioula

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.InputStreamReader

class AllSongsFragment : Fragment(), FilterableFragment {
    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_all_songs, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_all_songs)

        val allSongs = loadSongsFromAssets()
        songAdapter = SongAdapter(allSongs)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = songAdapter

        return view
    }

    override fun filter(query: String?) {
        if (::songAdapter.isInitialized) {
            songAdapter.filter.filter(query)
        }
    }

    // --- AJOUT DE LA FONCTION DE RAFRAÎCHISSEMENT ---
    fun refreshList() {
        if (::songAdapter.isInitialized && isAdded) {
            val allSongs = loadSongsFromAssets()
            songAdapter.updateList(allSongs)
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