package com.example.cantiquesdioula

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat // <<< L'IMPORT CORRIGÉ EST ICI
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle

class SongDetailFragment : Fragment() {

    private var song: Song? = null
    private var totalSongs: Int = 0
    private var favoriteMenuItem: MenuItem? = null
    private var masteredMenuItem: MenuItem? = null
    private var isFavorite: Boolean = false
    private var isMastered: Boolean = false

    // La variable 'refrainMarker' n'est plus nécessaire car on vérifie 'isDisplayed'
    // private val refrainMarker = "(Refrain)"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            song = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_SONG, Song::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable(ARG_SONG)
            }
            totalSongs = it.getInt(ARG_TOTAL_SONGS, 0)
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

        val toolbarTitleCentered: TextView? = view.findViewById(R.id.toolbar_title_centered)
        val titleTextView: TextView? = view.findViewById(R.id.detail_song_title)
        val contentTextView: TextView? = view.findViewById(R.id.detail_song_lyrics)
        val toolbar: Toolbar? = view.findViewById(R.id.detail_toolbar)

        if (toolbarTitleCentered == null || titleTextView == null || contentTextView == null || toolbar == null) {
            Log.e("SongDetailFragment", "Une vue essentielle est manquante!")
            return
        }

        var toolbarDisplayText = ""

        song?.let { currentSong ->
            toolbarDisplayText = "Bεtiba ${currentSong.id} / $totalSongs"
            toolbarTitleCentered.text = toolbarDisplayText
            titleTextView.text = currentSong.title

            val lyricsBuilder = StringBuilder()
            currentSong.verses.forEachIndexed { index, verse ->
                val verseTextTrimmed = verse.text.trim()

                // Correction des avertissements "Initializer redundant" et "Assignment..."
                val formattedVerse: String
                if (verse.isDisplayed == 0) { // Refrain
                    Log.d("RefrainDebug", ">>> Refrain détecté par isDisplayed == 0!")
                    formattedVerse = "<b><i>$verseTextTrimmed</i></b>"
                } else { // Couplet normal
                    formattedVerse = verseTextTrimmed
                }
                lyricsBuilder.append(formattedVerse)

                if (index < currentSong.verses.size - 1) {
                    lyricsBuilder.append("<br><br>")
                }
            }
            contentTextView.text = Html.fromHtml(lyricsBuilder.toString(), Html.FROM_HTML_MODE_LEGACY)

            val savedFontSize = SettingsManager.getFontSize(requireContext())
            contentTextView.textSize = savedFontSize

            isFavorite = FavoritesManager.isFavorite(requireContext(), currentSong.id)
            isMastered = MasteredManager.isMastered(requireContext(), currentSong.id)

        } ?: run {
            toolbarDisplayText = getString(R.string.error_title)
            titleTextView.text = ""
            contentTextView.text = getString(R.string.error_loading_song)
        }

        toolbarTitleCentered.text = toolbarDisplayText

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
            (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
            toolbar.setNavigationOnClickListener {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
        setupMenu()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.detail_menu, menu)
                favoriteMenuItem = menu.findItem(R.id.action_toggle_favorite)
                masteredMenuItem = menu.findItem(R.id.action_toggle_mastered)
                updateFavoriteIcon()
                updateMasteredIcon()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_toggle_favorite -> {
                        handleFavoriteClick()
                        true
                    }
                    R.id.action_toggle_mastered -> {
                        handleMasteredClick()
                        true
                    }
                    R.id.action_share -> {
                        shareSong()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun handleFavoriteClick() {
        song?.let { currentSong ->
            FavoritesManager.toggleFavorite(requireContext(), currentSong.id)
            isFavorite = !isFavorite
            updateFavoriteIcon()
            val message = if (isFavorite) "Ajouté aux favoris" else "Retiré des favoris"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleMasteredClick() {
        song?.let { currentSong ->
            MasteredManager.toggleMastered(requireContext(), currentSong.id)
            isMastered = !isMastered
            updateMasteredIcon()
            val message = if (isMastered) "Marqué comme maîtrisé" else "Marque 'maîtrisé' retirée"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFavoriteIcon() {
        favoriteMenuItem?.setIcon(
            if (isFavorite) R.drawable.ic_like_filled // REMPLACEZ si votre icône pleine a un autre nom
            else R.drawable.ic_like_border
        )
    }

    private fun updateMasteredIcon() {
        masteredMenuItem?.icon?.let { icon ->
            val colorRes = if (isMastered) R.color.icon_tint_mastered else R.color.icon_tint_normal
            val color = ContextCompat.getColor(requireContext(), colorRes)
            icon.setTint(color)
        }
    }

    private fun shareSong() {
        song?.let { currentSong ->
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                val subjectText = "Bεtiba ${currentSong.id} / $totalSongs: ${currentSong.title}"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Cantique: $subjectText")

                val lyricsTextPlain = currentSong.verses.joinToString(separator = "\n\n") { verse ->
                    verse.text.trim()
                }
                val shareBody = "$subjectText\n\n$lyricsTextPlain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody)

                startActivity(Intent.createChooser(shareIntent, "Partager le cantique via"))
            } catch (e: Exception) {
                // Utiliser le paramètre 'e'
                Log.e("SongDetailFragment", "Erreur lors du partage", e)
                Toast.makeText(context, "Impossible de lancer le partage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val ARG_SONG = "song_arg"
        private const val ARG_TOTAL_SONGS = "total_songs_arg"
        fun newInstance(song: Song, totalSongs: Int): SongDetailFragment {
            val fragment = SongDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_SONG, song)
            args.putInt(ARG_TOTAL_SONGS, totalSongs)
            fragment.arguments = args
            return fragment
        }
    }
}