package com.example.cantiquesdioula

// Un "Singleton" (un objet unique) qui servira de mémoire temporaire
// pour passer la liste des cantiques à SongPagerActivity sans utiliser d'Intent.
object SongRepository {
    var allSongs: List<Song> = emptyList()
}