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

class SongAdapter(private var songs: List<Song>) : RecyclerView.Adapter<SongAdapter.SongViewHolder>(), Filterable {

    // Garder une copie de la liste complète pour le filtrage ET pour le Pager
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
        // S'assurer que la position est valide
        if (position < 0 || position >= songs.size) {
            return // Ne rien faire si la position est invalide
        }

        val song = songs[position]
        val context = holder.itemView.context

        holder.songNumber.text = song.id.toString()
        holder.songTitle.text = song.title.replace("\n", " ").trim()

        val savedFontSize = SettingsManager.getFontSize(context)
        holder.songTitle.textSize = savedFontSize - 2f

        // --- Logique de Clic CORRIGÉE ---
        holder.itemView.setOnClickListener {
            // 1. On stocke la liste complète dans notre "mémoire" (Repository)
            SongRepository.allSongs = songListFull

            // 2. Trouver la VRAIE position dans la liste complète
            val originalPosition = songListFull.indexOf(song)
            val positionToPass = if (originalPosition != -1) originalPosition else 0

            // 3. Créer l'Intent
            val intent = Intent(context, SongPagerActivity::class.java)

            // 4. On ne passe QUE la position !
            intent.putExtra(EXTRA_CURRENT_SONG_POSITION, positionToPass)

            context.startActivity(intent)
        }
    }

    fun updateList(newList: List<Song>) {
        songs = ArrayList(newList)
        notifyDataSetChanged()
    }

    // Fonction pour définir à la fois la liste complète (pour les clics)
    // et la liste à afficher
    fun setFullList(fullList: List<Song>) {
        songListFull = ArrayList(fullList)
        songs = ArrayList(fullList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = songs.size

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
            // Mettre à jour seulement la liste affichée
            songs = results?.values as? List<Song> ?: emptyList()
            notifyDataSetChanged()
        }
    }
}