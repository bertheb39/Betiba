package com.example.cantiquesdioula

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categories: List<Category>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val expandedCategories = mutableSetOf<String>()

    companion object {
        private const val TYPE_CATEGORY = 0
        private const val TYPE_SONG = 1
    }

    private var items: List<Any> = buildList()

    private fun buildList(): List<Any> {
        val list = mutableListOf<Any>()
        categories.forEach { category ->
            list.add(category)
            if (expandedCategories.contains(category.name)) {
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
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_song, parent, false)
            SongItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is CategoryViewHolder && item is Category) {
            holder.bind(item)
        } else if (holder is SongItemViewHolder && item is Song) {
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)
        private val arrow: ImageView = itemView.findViewById(R.id.category_arrow)

        fun bind(category: Category) {
            categoryName.text = category.name
            val isExpanded = expandedCategories.contains(category.name)
            arrow.rotation = if (isExpanded) 180f else 0f

            itemView.setOnClickListener {
                if (category.songs.isNotEmpty()) {
                    if (isExpanded) {
                        expandedCategories.remove(category.name)
                    } else {
                        expandedCategories.add(category.name)
                    }
                    items = buildList()
                    notifyDataSetChanged()
                }
            }
        }
    }

    inner class SongItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songNumber: TextView = itemView.findViewById(R.id.song_number)
        private val songTitle: TextView = itemView.findViewById(R.id.song_title)

        fun bind(song: Song) {
            val context = itemView.context
            songNumber.text = song.id.toString()
            songTitle.text = song.title.replace("\n", " ").trim()

            // --- CORRECTION IMPORTANTE : APPLIQUER LA TAILLE DE POLICE ---
            val savedFontSize = SettingsManager.getFontSize(context)
            songTitle.textSize = savedFontSize - 2f // On applique la taille (un peu réduite)

            itemView.setOnClickListener {
                val intent = Intent(context, SongDetailActivity::class.java).apply {
                    putExtra("SONG_ID", song.id)
                }
                context.startActivity(intent)
            }
        }
    }
}