package com.example.cantiquesdioula

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_numbers, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_numbers)
        progressBar = view.findViewById(R.id.progress_bar_numbers)

        numberAdapter = NumberAdapter(emptyList())
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 5)
        recyclerView.adapter = numberAdapter

        loadSongsData()

        return view
    }

    private fun loadSongsData() {
        lifecycleScope.launch {
            if (allSongsList.isNotEmpty()) {
                numberAdapter.updateSongs(allSongsList) // Utilise la fonction de NumberAdapter
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
            numberAdapter.updateSongs(allSongsList) // Utilise la fonction de NumberAdapter

            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
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