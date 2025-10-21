package com.example.cantiquesdioula

import android.content.Context
import androidx.core.content.edit

object FavoritesManager {
    private const val PREFS_NAME = "CantiquesDioulaPrefs"
    private const val FAVORITES_KEY = "favorites"

    private fun getFavorites(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(FAVORITES_KEY, emptySet()) ?: emptySet()
    }

    fun isFavorite(context: Context, songId: Int): Boolean {
        return getFavorites(context).contains(songId.toString())
    }

    fun toggleFavorite(context: Context, songId: Int) {
        val favorites = getFavorites(context).toMutableSet()
        val idString = songId.toString()
        if (favorites.contains(idString)) {
            favorites.remove(idString)
        } else {
            favorites.add(idString)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putStringSet(FAVORITES_KEY, favorites)
        }
    }

    fun getFavoriteSongs(context: Context, allSongs: List<Song>): List<Song> {
        val favoritesIds = getFavorites(context)
        return allSongs.filter { favoritesIds.contains(it.id.toString()) }
    }
}