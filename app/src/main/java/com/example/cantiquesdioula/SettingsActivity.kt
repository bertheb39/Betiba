package com.example.cantiquesdioula

import android.os.Bundle
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar: Toolbar = findViewById(R.id.settings_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupFontSizeSlider()
        setupThemeSwitch()
    }

    private fun setupFontSizeSlider() {
        val seekBar: SeekBar = findViewById(R.id.font_size_seekbar)
        val previewTextView: TextView = findViewById(R.id.font_size_preview)

        val currentSize = SettingsManager.getFontSize(this)
        previewTextView.textSize = currentSize
        seekBar.progress = (currentSize - 14).toInt()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newSize = (progress + 14).toFloat()
                previewTextView.textSize = newSize
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    val finalSize = (it.progress + 14).toFloat()
                    SettingsManager.saveFontSize(this@SettingsActivity, finalSize)
                }
            }
        })
    }

    private fun setupThemeSwitch() {
        val themeSwitch: SwitchMaterial = findViewById(R.id.theme_switch)

        // Mettre l'interrupteur dans le bon état au démarrage
        themeSwitch.isChecked = SettingsManager.getTheme(this) == SettingsManager.THEME_DARK

        // Gérer le clic sur l'interrupteur
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                SettingsManager.saveTheme(this, SettingsManager.THEME_DARK)
                SettingsManager.applyTheme(SettingsManager.THEME_DARK)
            } else {
                SettingsManager.saveTheme(this, SettingsManager.THEME_LIGHT)
                SettingsManager.applyTheme(SettingsManager.THEME_LIGHT)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}