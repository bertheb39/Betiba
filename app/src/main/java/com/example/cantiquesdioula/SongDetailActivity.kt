package com.example.cantiquesdioula

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.gson.Gson
import java.io.InputStreamReader

class SongDetailActivity : AppCompatActivity() {

    private var currentSong: Song? = null
    private var favoriteMenuItem: MenuItem? = null
    private var masteredMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_song_detail)

        val toolbar: Toolbar = findViewById(R.id.detail_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val songId = intent.getIntExtra("SONG_ID", -1)
        if (songId != -1) {
            val allSongs = loadSongsFromAssets()
            currentSong = allSongs.firstOrNull { it.id == songId }

            currentSong?.let { song ->
                val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
                toolbarTitle.text = "${song.id}: ${song.title.replace("\n", " ").trim()}"

                val lyricsTextView: TextView = findViewById(R.id.detail_song_lyrics)

                // Appliquer la taille de la police dès la création
                lyricsTextView.textSize = SettingsManager.getFontSize(this)

                val lyricsBuilder = SpannableStringBuilder()
                song.verses.forEach { verse ->
                    val start = lyricsBuilder.length
                    if (verse.isDisplayed == 0) {
                        lyricsBuilder.append("\n(Refrain)\n")
                        val verseText = verse.text.trim() + "\n\n"
                        lyricsBuilder.append(verseText)
                        val end = lyricsBuilder.length
                        lyricsBuilder.setSpan(StyleSpan(Typeface.BOLD_ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    } else {
                        lyricsBuilder.append(verse.text.trim()).append("\n\n")
                    }
                }
                lyricsTextView.text = lyricsBuilder
            }
        }
    }

    // --- CORRECTION IMPORTANTE ---
    // Cette fonction est appelée chaque fois que l'utilisateur revient sur cet écran.
    override fun onResume() {
        super.onResume()
        // On ré-applique la taille de la police au cas où elle aurait été changée.
        val lyricsTextView: TextView = findViewById(R.id.detail_song_lyrics)
        lyricsTextView.textSize = SettingsManager.getFontSize(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        favoriteMenuItem = menu.findItem(R.id.action_toggle_favorite)
        masteredMenuItem = menu.findItem(R.id.action_toggle_mastered)
        updateIcons()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                shareSong()
                return true
            }
            R.id.action_toggle_favorite -> {
                currentSong?.let { FavoritesManager.toggleFavorite(this, it.id) }
                updateIcons()
                return true
            }
            R.id.action_toggle_mastered -> {
                currentSong?.let { MasteredManager.toggleMastered(this, it.id) }
                updateIcons()
                return true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shareSong() {
        currentSong?.let { song ->
            val title = "Cantique ${song.id}: ${song.title.replace("\n", " ").trim()}"
            val lyrics = song.verses.joinToString(separator = "\n") { it.text.trim() }
            val appLink = "\n\nTéléchargez l'application Cantiques Dioula ici : [VOTRE LIEN PLAY STORE]"
            val shareText = "$title\n\n$lyrics$appLink"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "Partager le cantique via..."))
        }
    }

    private fun updateIcons() {
        currentSong?.let { song ->
            val isFav = FavoritesManager.isFavorite(this, song.id)
            favoriteMenuItem?.setIcon(if (isFav) R.drawable.ic_like_filled_white else R.drawable.ic_like_border)

            val isMastered = MasteredManager.isMastered(this, song.id)
            masteredMenuItem?.setIcon(if (isMastered) R.drawable.ic_check_circle_active else R.drawable.ic_check_circle_white)
        }
    }

    private fun loadSongsFromAssets(): List<Song> {
        return try {
            val inputStream = assets.open("CAD.json")
            val reader = InputStreamReader(inputStream)
            Gson().fromJson(reader, SongData::class.java).songs
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}