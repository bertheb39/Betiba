package com.example.cantiquesdioula

import android.content.Context
import androidx.core.content.edit

object MasteredManager {
    private const val PREFS_NAME = "CantiquesDioulaPrefs"
    private const val MASTERED_KEY = "mastered"

    private fun getMastered(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(MASTERED_KEY, emptySet()) ?: emptySet()
    }

    fun isMastered(context: Context, songId: Int): Boolean {
        return getMastered(context).contains(songId.toString())
    }

    fun toggleMastered(context: Context, songId: Int) {
        val mastered = getMastered(context).toMutableSet()
        val idString = songId.toString()
        if (mastered.contains(idString)) {
            mastered.remove(idString)
        } else {
            mastered.add(idString)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putStringSet(MASTERED_KEY, mastered)
        }
    }

    fun getMasteredSongs(context: Context, allSongs: List<Song>): List<Song> {
        val masteredIds = getMastered(context)
        return allSongs.filter { masteredIds.contains(it.id.toString()) }
    }
}