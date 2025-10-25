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
import java.util.ArrayList // Import nécessaire
import java.util.Locale

class SongAdapter(private var songs: List<Song>) : RecyclerView.Adapter<SongAdapter.SongViewHolder>(), Filterable {

    // Garder une copie de la liste complète pour le filtrage ET pour le Pager
    private var songListFull: List<Song> = ArrayList(songs)

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // J'utilise les IDs de votre code
        val songNumber: TextView = itemView.findViewById(R.id.song_number)
        val songTitle: TextView = itemView.findViewById(R.id.song_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        // J'utilise le layout de votre code
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        val context = holder.itemView.context // Récupérer le contexte

        holder.songNumber.text = song.id.toString()
        holder.songTitle.text = song.title.replace("\n", " ").trim()

        // Appliquer la taille de police sauvegardée
        val savedFontSize = SettingsManager.getFontSize(context)
        holder.songTitle.textSize = savedFontSize - 2f

        // --- Logique de Clic (votre code) ---
        holder.itemView.setOnClickListener {
            val intent = Intent(context, SongPagerActivity::class.java)

            // On passe TOUJOURS la liste complète
            val listToPass = ArrayList(songListFull)

            // Trouver la VRAIE position dans la liste complète
            val originalPosition = songListFull.indexOf(song)
            val positionToPass = if (originalPosition != -1) originalPosition else 0

            // Mettre la liste complète et la position dans l'Intent
            intent.putParcelableArrayListExtra(EXTRA_SONGS_LIST, listToPass)
            intent.putExtra(EXTRA_CURRENT_SONG_POSITION, positionToPass)

            context.startActivity(intent)
        }
    }

    fun updateList(newList: List<Song>) {
        songs = ArrayList(newList)
        // Mettre à jour la liste complète SEULEMENT si la nouvelle liste est la liste complète
        // Pour le filtre, songListFull ne doit pas changer.
        if (songs.size > songListFull.size) { // Simple heuristique
            songListFull = ArrayList(newList)
        } else if (songs.isEmpty() && songListFull.isNotEmpty()) {
            // cas spécial: la liste filtrée est vide
        } else if (songListFull.isEmpty()) {
            songListFull = ArrayList(newList)
        }

        notifyDataSetChanged()
    }

    // S'assurer que la liste complète est bien à jour quand on crée l'adaptateur
    fun setFullList(fullList: List<Song>) {
        songListFull = ArrayList(fullList)
        songs = ArrayList(fullList) // Au début, la liste affichée est la liste complète
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = songs.size

    // --- Votre code de filtrage (inchangé) ---
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

    // --- AJOUT : Constantes Manquantes ---
    companion object {
        const val EXTRA_SONGS_LIST = "SONG_LIST"
        const val EXTRA_CURRENT_SONG_POSITION = "CURRENT_POSITION"
    }
    // --- FIN DE L'AJOUT ---
}