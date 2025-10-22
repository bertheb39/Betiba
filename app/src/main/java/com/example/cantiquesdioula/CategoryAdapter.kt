package com.example.cantiquesdioula

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList // Import nécessaire

class CategoryAdapter(
    private val categories: List<Category>,
    private val allSongs: List<Song> // <-- AJOUT : Recevoir la liste complète
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val expandedCategories = mutableSetOf<String>()

    companion object {
        private const val TYPE_CATEGORY = 0
        private const val TYPE_SONG = 1
    }

    // items contient maintenant des Category ou des Song
    private var items: List<Any> = buildList()

    private fun buildList(): List<Any> {
        val list = mutableListOf<Any>()
        categories.forEach { category ->
            list.add(category) // Ajouter la catégorie elle-même
            if (expandedCategories.contains(category.name)) {
                // Ajouter les chansons de cette catégorie si elle est dépliée
                list.addAll(category.songs)
            }
        }
        return list
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is Category) TYPE_CATEGORY else TYPE_SONG
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_CATEGORY) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_category, parent, false)
            CategoryViewHolder(view)
        } else { // TYPE_SONG
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_song, parent, false)
            SongItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is CategoryViewHolder && item is Category) {
            holder.bind(item)
        } else if (holder is SongItemViewHolder && item is Song) {
            holder.bind(item) // Lier les données du cantique
        }
    }

    override fun getItemCount(): Int = items.size

    // ViewHolder pour une catégorie (ne change pas)
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)
        private val arrow: ImageView = itemView.findViewById(R.id.category_arrow)

        fun bind(category: Category) {
            categoryName.text = category.name
            val isExpanded = expandedCategories.contains(category.name)
            arrow.rotation = if (isExpanded) 180f else 0f // Tourner la flèche

            itemView.setOnClickListener {
                if (category.songs.isNotEmpty()) { // Ne rien faire si la catégorie est vide
                    if (isExpanded) {
                        expandedCategories.remove(category.name)
                    } else {
                        expandedCategories.add(category.name)
                    }
                    // Reconstruire la liste des items à afficher et notifier l'adaptateur
                    items = buildList()
                    notifyDataSetChanged() // Attention: Peut être inefficace pour de grandes listes
                }
            }
        }
    }

    // ViewHolder pour un cantique (le clic est modifié ici)
    inner class SongItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songNumber: TextView = itemView.findViewById(R.id.song_number)
        private val songTitle: TextView = itemView.findViewById(R.id.song_title)

        fun bind(song: Song) {
            val context = itemView.context
            songNumber.text = song.id.toString()
            songTitle.text = song.title.replace("\n", " ").trim()

            val savedFontSize = SettingsManager.getFontSize(context)
            songTitle.textSize = savedFontSize - 2f

            // --- DÉBUT MODIFICATION CLIC ---
            itemView.setOnClickListener {
                val intent = Intent(context, SongPagerActivity::class.java)

                // Utiliser la liste complète 'allSongs' fournie à l'adaptateur
                val listToPass = ArrayList(allSongs) // Toujours passer la liste complète et triée

                // Trouver la position du cantique cliqué dans cette liste complète
                val originalPosition = allSongs.indexOf(song)
                val positionToPass = if (originalPosition != -1) originalPosition else 0

                intent.putParcelableArrayListExtra(EXTRA_SONGS_LIST, listToPass)
                intent.putExtra(EXTRA_CURRENT_SONG_POSITION, positionToPass)

                context.startActivity(intent)
            }
            // --- FIN MODIFICATION CLIC ---
        }
    }
}