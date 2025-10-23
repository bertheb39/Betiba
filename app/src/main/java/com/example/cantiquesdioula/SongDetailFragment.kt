package com.example.cantiquesdioula

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.example.cantiquesdioula.SongLine // <<< L'IMPORT EST ICI
import java.io.File
import java.io.FileOutputStream

class SongDetailFragment : Fragment() {

    private var song: Song? = null
    private var totalSongs: Int = 0
    private var favoriteMenuItem: MenuItem? = null
    private var masteredMenuItem: MenuItem? = null
    private var isFavorite: Boolean = false
    private var isMastered: Boolean = false

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

                val formattedVerse: String

                // Vérifie d'abord s'il y a des "Lines" (pour le cantique 216)
                if (verse.lines != null && verse.lines.isNotEmpty()) {
                    // Cas spécial pour le cantique 216 (format Cɛw/Musow)
                    val linesHtml = verse.lines.joinToString(separator = "<br>") { songLine: SongLine ->
                        "&nbsp;&nbsp;&nbsp;&nbsp;<i><b>${songLine.speaker}:</b> ${songLine.line}</i>"
                    }
                    formattedVerse = linesHtml

                } else {
                    // Cas normal pour tous les autres cantiques
                    val verseText = (verse.text ?: "").trim()

                    val textWithHtmlBreaks = verseText
                        .replace("\r\n", "<br>")
                        .replace("\r", "<br>")
                        .replace("\n", "<br>")

                    if (verse.isDisplayed == 0) { // Refrain
                        formattedVerse = "<b><i>$textWithHtmlBreaks</i></b>"
                    } else { // Couplet normal
                        formattedVerse = textWithHtmlBreaks
                    }
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
                    R.id.action_toggle_favorite -> { handleFavoriteClick(); true }
                    R.id.action_toggle_mastered -> { handleMasteredClick(); true }
                    R.id.action_share -> { shareSong(); true }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun handleFavoriteClick() {
        song?.let { currentSong ->
            FavoritesManager.toggleFavorite(requireContext(), currentSong.id)
            isFavorite = !isFavorite
            requireActivity().invalidateMenu() // Force le menu à se redessiner
            val message = if (isFavorite) "Ajouté aux favoris" else "Retiré des favoris"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleMasteredClick() {
        song?.let { currentSong ->
            MasteredManager.toggleMastered(requireContext(), currentSong.id)
            isMastered = !isMastered
            requireActivity().invalidateMenu() // Force le menu à se redessiner
            val message = if (isMastered) "Marqué comme maîtrisé" else "Marque 'maîtrisé' retirée"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFavoriteIcon() {
        favoriteMenuItem?.setIcon(
            if (isFavorite) R.drawable.ic_like_filled
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

    // Affiche le dialogue de choix
    private fun shareSong() {
        val context = requireContext()
        val options = arrayOf("Partager le texte complet", "Partager un couplet en image")

        AlertDialog.Builder(context)
            .setTitle("Choisir une option de partage")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> { // Option 1: Texte complet
                        shareSongAsText()
                    }
                    1 -> { // Option 2: Image de couplet
                        showVerseSelectionDialog()
                    }
                }
            }
            .show()
    }

    // Partage le texte (ancienne logique)
    private fun shareSongAsText() {
        song?.let { currentSong ->
            try {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                val subjectText = "Bεtiba ${currentSong.id} / $totalSongs: ${currentSong.title}"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Cantique: $subjectText")

                val lyricsTextPlain = currentSong.verses.joinToString(separator = "\n\n") { verse ->
                    if (verse.lines != null && verse.lines.isNotEmpty()) {
                        verse.lines.joinToString(separator = "\n") { songLine: SongLine ->
                            "${songLine.speaker}: ${songLine.line}"
                        }
                    } else {
                        (verse.text ?: "").trim()
                    }
                }

                val shareBody = "$subjectText\n\n$lyricsTextPlain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody)

                startActivity(Intent.createChooser(shareIntent, "Partager le cantique via"))
            } catch (e: Exception) {
                Log.e("SongDetailFragment", "Erreur lors du partage texte", e)
                Toast.makeText(context, "Impossible de lancer le partage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Affiche le dialogue de sélection de couplet
    private fun showVerseSelectionDialog() {
        val currentSong = song ?: return
        val context = requireContext()

        val verseNames = currentSong.verses.mapIndexed { index, verse ->
            if (verse.lines != null && verse.lines.isNotEmpty()) {
                "Strophe ${index + 1} (Cɛw/Musow)"
            } else if (verse.isDisplayed == 0) {
                "Refrain (Position ${index + 1})"
            } else {
                "Couplet ${index + 1}"
            }
        }.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("Quel couplet partager en image ?")
            .setItems(verseNames) { dialog, which ->
                val selectedVerse = currentSong.verses[which]
                shareVerseAsImage(currentSong, selectedVerse)
            }
            .show()
    }

    // Gère la création et le partage de l'image
    private fun shareVerseAsImage(currentSong: Song, verseToShare: Verse) {
        try {
            val bitmap = generateShareableImage(currentSong, verseToShare)
            if (bitmap == null) {
                Toast.makeText(context, "Erreur lors de la création de l'image", Toast.LENGTH_SHORT).show()
                return
            }

            val imageUri = saveBitmapAndGetUri(bitmap)
            if (imageUri == null) {
                Toast.makeText(context, "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show()
                return
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_SUBJECT, "Cantique: ${currentSong.id}: ${currentSong.title}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Partager l'image du couplet via"))

        } catch (e: Exception) {
            Log.e("SongDetailFragment", "Erreur partage image de couplet", e)
            Toast.makeText(context, "Impossible de lancer le partage", Toast.LENGTH_SHORT).show()
        }
    }

    // Génère l'image (Bitmap)
    private fun generateShareableImage(currentSong: Song, verseToShare: Verse): Bitmap? {
        val context = requireContext()
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.layout_song_share_image, null)

        val titleTextView: TextView = view.findViewById(R.id.share_image_title)
        val lyricsTextView: TextView = view.findViewById(R.id.share_image_lyrics)

        titleTextView.text = "${currentSong.id}: ${currentSong.title}"

        var verseText = ""
        if (verseToShare.lines != null && verseToShare.lines.isNotEmpty()) {
            verseText = verseToShare.lines.joinToString(separator = "\n") { "${it.speaker}: ${it.line}" }
        } else {
            verseText = (verseToShare.text ?: "").trim()
        }
        lyricsTextView.text = verseText

        // Correction de la mesure pour le retour à la ligne
        val width = 1440
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthMeasureSpec, heightMeasureSpec)
        val height = view.measuredHeight
        view.layout(0, 0, width, height)

        if (height == 0) return null

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    // Sauvegarde l'image et obtient l'URI
    private fun saveBitmapAndGetUri(bitmap: Bitmap): Uri? {
        val context = requireContext()
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "cantique_share.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e("SongDetailFragment", "Erreur sauvegarde bitmap", e)
            null
        }
    }

    // Companion Object
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