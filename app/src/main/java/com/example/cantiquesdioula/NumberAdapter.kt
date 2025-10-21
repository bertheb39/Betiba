package com.example.cantiquesdioula

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class NumberAdapter(private var songs: List<Song>) : RecyclerView.Adapter<NumberAdapter.NumberViewHolder>(), Filterable {
    private var songListFull: List<Song> = ArrayList(songs)

    class NumberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numberText: TextView = itemView.findViewById(R.id.number_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NumberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item_number, parent, false)
        return NumberViewHolder(view)
    }

    override fun onBindViewHolder(holder: NumberViewHolder, position: Int) {
        val song = songs[position]
        holder.numberText.text = song.id.toString()
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, SongDetailActivity::class.java).apply {
                putExtra("SONG_ID", song.id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = songs.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = if (constraint.isNullOrEmpty()) {
                    songListFull
                } else {
                    val filterPattern = constraint.toString().trim()
                    songListFull.filter { it.id.toString().contains(filterPattern) }
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
}