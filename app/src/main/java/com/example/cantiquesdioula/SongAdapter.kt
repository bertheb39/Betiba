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
import java.util.*

class SongAdapter(private var songs: List<Song>) : RecyclerView.Adapter<SongAdapter.SongViewHolder>(), Filterable {

    private var songListFull: List<Song> = ArrayList(songs)

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songNumber: TextView = itemView.findViewById(R.id.song_number)
        val songTitle: TextView = itemView.findViewById(R.id.song_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        val context = holder.itemView.context

        holder.songNumber.text = song.id.toString()
        holder.songTitle.text = song.title.replace("\n", " ").trim()

        // --- CORRECTION IMPORTANTE ---
        // On applique la taille de police sauvegardée au titre du cantique
        val savedFontSize = SettingsManager.getFontSize(context)
        holder.songTitle.textSize = savedFontSize - 2f // On met le titre un peu plus petit que les paroles

        holder.itemView.setOnClickListener {
            val intent = Intent(context, SongDetailActivity::class.java).apply {
                putExtra("SONG_ID", song.id)
            }
            context.startActivity(intent)
        }
    }

    fun updateList(newList: List<Song>) {
        songs = ArrayList(newList)
        songListFull = ArrayList(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = songs.size

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

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            songs = results?.values as? List<Song> ?: emptyList()
            notifyDataSetChanged()
        }
    }
}