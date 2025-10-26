package com.example.cantiquesdioula

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2 // Import pour ViewPager2

// Les constantes ont été déplacées dans Constants.kt

class SongPagerActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var songPagerAdapter: SongPagerAdapter
    private var songsList: List<Song> = emptyList()
    private var currentPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_pager2) // Assurez-vous que c'est le bon layout

        // --- 1. Récupérer les données ---
        intent?.let {
            // On ne récupère QUE la position (en utilisant notre constante)
            currentPosition = it.getIntExtra(EXTRA_CURRENT_SONG_POSITION, 0)
        }

        // On récupère la liste complète depuis notre "mémoire" (Repository)
        songsList = SongRepository.allSongs

        // --- 2. Vérifier si la liste de cantiques n'est pas vide ---
        if (songsList.isEmpty()) {
            // Gérer l'erreur : afficher un message, fermer l'activité...
            // Log.e("SongPagerActivity", "La liste des cantiques est vide !")
            finish() // Ferme l'activité s'il n'y a rien à afficher
            return
        }

        // --- 3. Initialiser le ViewPager2 et l'Adapter ---
        viewPager = findViewById(R.id.song_view_pager) // Utiliser l'ID de votre ViewPager2

        // Créer l'adapter en lui passant l'activité (this) et la liste des cantiques
        songPagerAdapter = SongPagerAdapter(this, songsList)

        // Attacher l'adapter au ViewPager2
        viewPager.adapter = songPagerAdapter

        // --- 4. Afficher la bonne page au démarrage ---
        // Se positionner sur le cantique qui a été cliqué
        viewPager.setCurrentItem(currentPosition, false) // false pour ne pas animer
    }
}