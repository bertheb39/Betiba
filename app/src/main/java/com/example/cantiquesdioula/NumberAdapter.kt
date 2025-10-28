package com.example.cantiquesdioula

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList
import java.util.Locale

class NumberAdapter(
    private var songs: List<Song>
) : RecyclerView.Adapter<NumberAdapter.NumberViewHolder>(), Filterable {

    private var songListFull: List<Song> = ArrayList(songs)

    class NumberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val number: TextView = itemView.findViewById(R.id.number_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item_number, parent, false)
        return NumberViewHolder(view)
    }

    override fun onBindViewHolder(holder: NumberViewHolder, position: Int) {
        val song = songs[position]
        val context = holder.itemView.context
        holder.number.text = song.id.toString()

        val savedFontSize = SettingsManager.getFontSize(context)
        holder.number.textSize = savedFontSize - 2f

        holder.itemView.setOnClickListener {
            SongRepository.allSongs = songListFull
            val originalPosition = songListFull.indexOf(song)
            val positionToPass = if (originalPosition != -1) originalPosition else 0
            val intent = Intent(context, SongPagerActivity::class.java)
            intent.putExtra(EXTRA_CURRENT_SONG_POSITION, positionToPass)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = songs.size

    fun setFullList(fullList: List<Song>) {
        songListFull = ArrayList(fullList)
        songs = ArrayList(fullList)
        notifyDataSetChanged()
    }

    // !! FONCTION CORRIGÉE ET PLACÉE À L'INTÉRIEUR DE LA CLASSE !!
    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        songListFull = ArrayList(newSongs)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return songFilter
    }

    private val songFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: MutableList<Song> = ArrayList()
            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(songListFull)
            } else {
                val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()
                for (song in songListFull) {
                    // Note : Le filtre étendu (fullText) sera ajouté sur la branche ImpMelodie
                    if (song.title.lowercase(Locale.getDefault()).contains(filterPattern) ||
                        song.id.toString().contains(filterPattern)) {
                        filteredList.add(song)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            songs = results?.values as? List<Song> ?: emptyList()
            notifyDataSetChanged()
        }
    }
}