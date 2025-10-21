package com.example.cantiquesdioula

import android.os.Build // Nécessaire pour getParcelableArrayListExtra
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2 // <-- Import pour ViewPager2

// Clés pour les extras (vous pouvez aussi les mettre dans un fichier Constants.kt)
const val EXTRA_SONGS_LIST = "com.example.cantiquesdioula.EXTRA_SONGS_LIST"
const val EXTRA_CURRENT_SONG_POSITION = "com.example.cantiquesdioula.EXTRA_CURRENT_SONG_POSITION"

class SongPagerActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var songPagerAdapter: SongPagerAdapter
    private var songsList: List<Song> = emptyList()
    private var currentPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_pager2) // Assurez-vous que c'est le bon layout

        // --- 1. Récupérer les données passées par l'Intent ---
        intent?.let {
            // Récupérer la liste des cantiques
            songsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelableArrayListExtra(EXTRA_SONGS_LIST, Song::class.java) ?: emptyList()
            } else {
                @Suppress("DEPRECATION")
                it.getParcelableArrayListExtra(EXTRA_SONGS_LIST) ?: emptyList()
            }

            // Récupérer la position du cantique cliqué (0 par défaut si non trouvée)
            currentPosition = it.getIntExtra(EXTRA_CURRENT_SONG_POSITION, 0)
        }

        // --- 2. Vérifier si la liste de cantiques n'est pas vide ---
        if (songsList.isEmpty()) {
            // Gérer l'erreur : afficher un message, fermer l'activité...
            // Log.e("SongPagerActivity", "La liste des cantiques est vide !")
            finish() // Ferme l'activité s'il n'y a rien à afficher
            return
        }

        // --- 3. Initialiser le ViewPager2 et l'Adapter ---
        viewPager = findViewById(R.id.song_view_pager) // Utiliser l'ID de votre ViewPager2 dans activity_song_pager2.xml

        // Créer l'adapter en lui passant l'activité (this) et la liste des cantiques
        songPagerAdapter = SongPagerAdapter(this, songsList)

        // Attacher l'adapter au ViewPager2
        viewPager.adapter = songPagerAdapter

        // --- 4. Afficher la bonne page au démarrage ---
        // Se positionner sur le cantique qui a été cliqué
        viewPager.setCurrentItem(currentPosition, false) // false pour ne pas animer le premier affichage
    }
}