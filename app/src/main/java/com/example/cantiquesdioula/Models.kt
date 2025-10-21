package com.example.cantiquesdioula

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize // Ajoutez cet import
import android.os.Parcelable // Ajoutez cet import

// Pour le JSON principal des cantiques
data class SongData(
    @SerializedName("Songs") val songs: List<Song>
)

@Parcelize
data class Song(
    @SerializedName("ID") val id: Int,
    @SerializedName("Text") val title: String,
    @SerializedName("Verses") val verses: List<Verse>
) : Parcelable

@Parcelize
data class Verse(
    @SerializedName("Text") val text: String,
    @SerializedName("IsDisplayed") val isDisplayed: Int? = 0
): Parcelable

// Pour la liste dans l'écran Menu
data class MenuItemData(val title: String, val iconResId: Int)

data class Category(val name: String, val songs: List<Song> = emptyList())