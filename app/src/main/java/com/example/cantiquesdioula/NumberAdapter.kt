package com.example.cantiquesdioula

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList // Added import
import java.util.Locale // Keep existing import

class NumberAdapter(initialSongs: List<Song>) : RecyclerView.Adapter<NumberAdapter.NumberViewHolder>(), Filterable {
    // Keep a mutable list for the currently displayed songs
    private var songs: MutableList<Song> = ArrayList(initialSongs)
    // Keep an immutable copy of the full, sorted list for reference and filtering
    private var songListFull: List<Song> = ArrayList(initialSongs).sortedBy { it.id } // Ensure it's sorted

    class NumberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numberText: TextView = itemView.findViewById(R.id.number_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item_number, parent, false)
        return NumberViewHolder(view)
    }

    override fun onBindViewHolder(holder: NumberViewHolder, position: Int) {
        val song = songs[position] // Get song from the currently displayed (possibly filtered) list
        holder.numberText.text = song.id.toString()

        // --- START OF CLICK LISTENER CORRECTION ---
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            // Target the new SongPagerActivity
            val intent = Intent(context, SongPagerActivity::class.java)

            // Pass the FULL list of songs (converted to ArrayList)
            val listToPass = ArrayList(songListFull)

            // Find the actual position of the clicked song in the FULL list
            val originalPosition = songListFull.indexOf(song)
            // Use the found position, or default to 0 if not found (safety check)
            val positionToPass = if (originalPosition != -1) originalPosition else 0

            // Add the full list and the correct starting position to the Intent
            intent.putParcelableArrayListExtra(EXTRA_SONGS_LIST, listToPass)
            intent.putExtra(EXTRA_CURRENT_SONG_POSITION, positionToPass)

            // Start the SongPagerActivity
            context.startActivity(intent)
        }
        // --- END OF CLICK LISTENER CORRECTION ---
    }

    override fun getItemCount(): Int = songs.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<Song>() // Start with an empty list
                if (constraint.isNullOrEmpty()) {
                    // If no filter, add all songs from the full list
                    filteredList.addAll(songListFull)
                } else {
                    val filterPattern = constraint.toString().trim()
                    // Filter the full list based on the ID containing the pattern
                    for (song in songListFull) {
                        if (song.id.toString().contains(filterPattern)) {
                            filteredList.add(song)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList // The result is the filtered list
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                songs.clear() // Clear the currently displayed list
                val resultList = results?.values as? List<Song>
                if (resultList != null) {
                    songs.addAll(resultList) // Add the filtered results
                }
                notifyDataSetChanged() // Update the RecyclerView
            }
        }
    }

    // Optional: Function to update the adapter if the underlying data source changes
    fun updateFullList(newFullList: List<Song>) {
        songListFull = ArrayList(newFullList).sortedBy { it.id } // Store the new sorted full list
        songs.clear() // Clear the displayed list
        songs.addAll(songListFull) // Reset displayed list to the full list
        notifyDataSetChanged()
    }
}