package com.example.cantiquesdioula

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val toolbar: Toolbar = findViewById(R.id.about_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Afficher le numéro de version de l'application
        val versionTextView: TextView = findViewById(R.id.version_text)
        try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            versionTextView.text = "Version $versionName"
        } catch (e: Exception) {
            e.printStackTrace()
            versionTextView.text = "Version inconnue"
        }
    }

    // Gérer le clic sur la flèche de retour
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}