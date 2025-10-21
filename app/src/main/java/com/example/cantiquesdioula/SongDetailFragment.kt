package com.example.cantiquesdioula

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar

class SongDetailFragment : Fragment() {

    private var song: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Utilisation de la nouvelle méthode getParcelable
            song = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_SONG, Song::class.java)
            } else {
                @Suppress("DEPRECATION") // Pour les versions plus anciennes
                it.getParcelable(ARG_SONG)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_song_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView: TextView? = view.findViewById(R.id.toolbar_title)
        val contentTextView: TextView? = view.findViewById(R.id.detail_song_lyrics)
        val toolbar: Toolbar? = view.findViewById(R.id.detail_toolbar)

        if (titleTextView == null || contentTextView == null || toolbar == null) {
            return // Quitter si une vue essentielle manque
        }

        // Afficher les détails du cantique
        song?.let { currentSong ->
            // --- CORRECTION TITRE ---
            // On utilise directement currentSong.title (qui contient déjà le numéro ?)
            titleTextView.text = currentSong.title

            // --- CORRECTION PAROLES ---
            // On construit les paroles en joignant le texte de chaque couplet (Verse)
            // On ajoute deux sauts de ligne entre chaque couplet
            val lyricsText = currentSong.verses.joinToString(separator = "\n\n") { verse ->
                verse.text // Prend le texte de chaque objet Verse
            }
            contentTextView.text = lyricsText // Afficher les paroles construites

        } ?: run {
            // Cas où song est null
            titleTextView.text = getString(R.string.error_title) // Utiliser strings.xml
            contentTextView.text = getString(R.string.error_loading_song) // Utiliser strings.xml
        }

        // --- Gérer le bouton retour dans le Toolbar ---
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
            (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
            toolbar.setNavigationOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
    }

    companion object {
        private const val ARG_SONG = "song_arg"

        // Toujours ignorer l'avertissement "never used"
        fun newInstance(song: Song): SongDetailFragment {
            val fragment = SongDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_SONG, song)
            fragment.arguments = args
            return fragment
        }
    }
}