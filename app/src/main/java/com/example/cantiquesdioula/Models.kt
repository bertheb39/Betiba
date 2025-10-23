package com.example.cantiquesdioula

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// Classe pour parser le JSON principal
data class SongData(
    @SerializedName("Songs") val songs: List<Song>
)

// Classe pour un cantique
@Parcelize
data class Song(
    @SerializedName("ID") val id: Int,
    @SerializedName("Text") val title: String,
    @SerializedName("Verses") val verses: List<Verse>
) : Parcelable

// Classe pour un verset
@Parcelize
data class Verse(
    // 'text' est nullable (peut être absent)
    @SerializedName("Text") val text: String?,

    // 'isDisplayed' est nullable (peut être absent)
    @SerializedName("IsDisplayed") val isDisplayed: Int?,

    // 'lines' est nullable (pour le cantique 216)
    @SerializedName("Lines") val lines: List<SongLine>?

) : Parcelable

// Classe pour les lignes spéciales du cantique 216
@Parcelize
data class SongLine(
    @SerializedName("Speaker") val speaker: String,
    @SerializedName("Line") val line: String
) : Parcelable

// Classe pour une catégorie (pour l'onglet Catégories)
data class Category(
    val name: String,
    val songs: List<Song>
)

// Classe pour un item du menu (pour l'onglet Menu)
data class MenuItemData(
    val title: String,
    val iconResId: Int
)