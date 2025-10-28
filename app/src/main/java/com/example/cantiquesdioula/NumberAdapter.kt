package com.example.cantiquesdioula

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList
import java.util.Locale
import android.widget.Filter
import android.widget.Filterable

class NumberAdapter(
    private var songs: List<Song> // La liste affichée
) : RecyclerView.Adapter<NumberAdapter.NumberViewHolder>(), Filterable {

    // Garder la liste complète pour le filtrage et les clics
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

    // S'assurer que la liste complète est bien à jour
    fun setFullList(fullList: List<Song>) {
        songListFull = ArrayList(fullList)
        songs = ArrayList(fullList)
        notifyDataSetChanged()
    }

    // --- !! C'EST LA FONCTION QUI MANQUAIT !! ---
    // Elle doit être ICI, à l'intérieur de la classe NumberAdapter
    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        songListFull = ArrayList(newSongs) // Met aussi à jour la liste complète pour le filtre
        notifyDataSetChanged()
    }
    // --- FIN DE L'AJOUT ---

    // --- Filtrage ---
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
    // L'accolade de fermeture de la classe est ci-dessous
}