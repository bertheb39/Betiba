package com.example.cantiquesdioula

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MenuAdapter(
    private val menuItems: List<MenuItemData>,
    private val onItemClick: (MenuItemData) -> Unit // Le gestionnaire de clic
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView = itemView.findViewById(R.id.menu_item_icon)
        val titleTextView: TextView = itemView.findViewById(R.id.menu_item_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.iconImageView.setImageResource(menuItem.iconResId)
        holder.titleTextView.text = menuItem.title

        // S'assurer que le clic est bien transmis
        holder.itemView.setOnClickListener {
            onItemClick(menuItem)
        }
    }

    override fun getItemCount(): Int = menuItems.size
}