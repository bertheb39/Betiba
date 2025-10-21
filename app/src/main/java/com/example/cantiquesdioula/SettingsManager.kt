package com.example.cantiquesdioula

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

object SettingsManager {
    private const val PREFS_NAME = "CantiquesDioulaSettings"

    // --- GESTION DE LA TAILLE DE LA POLICE ---
    private const val FONT_SIZE_KEY = "font_size"
    const val DEFAULT_FONT_SIZE = 20f

    fun saveFontSize(context: Context, size: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putFloat(FONT_SIZE_KEY, size)
        }
    }

    fun getFontSize(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(FONT_SIZE_KEY, DEFAULT_FONT_SIZE)
    }

    // --- GESTION DU THÈME (JOUR/NUIT) ---
    private const val THEME_KEY = "theme_mode"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    fun saveTheme(context: Context, theme: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(THEME_KEY, theme)
        }
    }

    fun getTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Par défaut, on utilise le thème clair
        return prefs.getString(THEME_KEY, THEME_LIGHT) ?: THEME_LIGHT
    }

    // Fonction pour appliquer le thème choisi
    fun applyTheme(theme: String) {
        when (theme) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}