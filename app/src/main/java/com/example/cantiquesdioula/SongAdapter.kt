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
        // --- CORRECTION IDs --- (J'utilise les IDs de votre code)
        val songNumber: TextView = itemView.findViewById(R.id.song_number)
        val songTitle: TextView = itemView.findViewById(R.id.song_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        // --- CORRECTION Layout --- (J'utilise le layout de votre code)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        val context = holder.itemView.context // Récupérer le contexte

        // --- CORRECTION Affichage --- (J'utilise la logique de votre code)
        // Note: Est-ce que song.id est vraiment le numéro du cantique ?
        // Si vous avez une propriété 'number' dans Song, utilisez-la.
        holder.songNumber.text = song.id.toString()
        holder.songTitle.text = song.title.replace("\n", " ").trim()

        // Appliquer la taille de police sauvegardée
        val savedFontSize = SettingsManager.getFontSize(context)
        holder.songTitle.textSize = savedFontSize - 2f

        // --- DÉBUT DE LA MODIFICATION POUR LE PAGER ---
        holder.itemView.setOnClickListener {
            // 1. Créer l'Intent pour SongPagerActivity
            val intent = Intent(context, SongPagerActivity::class.java)

            // 2. Déterminer quelle liste passer (filtrée ou complète)
            // Pour le swipe, il est souvent mieux de passer la liste COMPLÈTE
            // même si l'affichage actuel est filtré.
            val listToPass = ArrayList(songListFull) // On passe TOUJOURS la liste complète

            // 3. Trouver la VRAIE position dans la liste complète
            // car 'position' est relatif à la liste filtrée 'songs'
            val originalPosition = songListFull.indexOf(song)
            val positionToPass = if (originalPosition != -1) originalPosition else 0 // Sécurité

            // 4. Mettre la liste complète et la position dans l'Intent
            intent.putParcelableArrayListExtra(EXTRA_SONGS_LIST, listToPass)
            intent.putExtra(EXTRA_CURRENT_SONG_POSITION, positionToPass)

            // 5. Démarrer l'activité Pager
            context.startActivity(intent)
        }
        // --- FIN DE LA MODIFICATION POUR LE PAGER ---
    }

    fun updateList(newList: List<Song>) {
        songs = ArrayList(newList)
        songListFull = ArrayList(newList) // Mettre à jour la liste complète aussi
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
                    // Vérifiez si votre classe Song a bien une propriété 'id' et 'title'
                    if (song.title.lowercase(Locale.getDefault()).contains(filterPattern) ||
                        song.id.toString().contains(filterPattern)) { // Assurez-vous que 'id' est le bon champ pour la recherche par numéro
                        filteredList.add(song)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            // Attention: Ne pas écraser songListFull ici
            val newFilteredList = results?.values as? List<Song> ?: emptyList()
            songs = newFilteredList // Mettre à jour seulement la liste affichée
            notifyDataSetChanged()
        }
    }
}