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

        songAdapter = SongAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = songAdapter

        loadSongsData()

        return view
    }

    private fun loadSongsData() {
        lifecycleScope.launch {
            if (allSongsList.isNotEmpty()) {
                songAdapter.setFullList(allSongsList)
                songAdapter.updateList(allSongsList)

                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                return@launch
            }

            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE

            val songs = withContext(Dispatchers.IO) {
                loadSongsFromAssets()
            }

            allSongsList = songs
            songAdapter.setFullList(allSongsList)
            songAdapter.updateList(allSongsList)

            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    // Cette fonction est appelée par HomeFragment pour lancer le filtre
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