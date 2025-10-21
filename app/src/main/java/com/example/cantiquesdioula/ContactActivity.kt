package com.example.cantiquesdioula

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ContactActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        val toolbar: Toolbar = findViewById(R.id.contact_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val callLayout: LinearLayout = findViewById(R.id.call_layout)
        val whatsappLayout: LinearLayout = findViewById(R.id.whatsapp_layout)
        val emailLayout: LinearLayout = findViewById(R.id.email_layout)

        // Rendre l'appel cliquable
        callLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+22664721227"))
            startActivitySafely(intent, "Impossible d'ouvrir l'application Téléphone.")
        }

        // Rendre WhatsApp cliquable
        whatsappLayout.setOnClickListener {
            val url = "https://wa.me/22664721227"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivitySafely(intent, "WhatsApp n'est pas installé.")
        }

        // Rendre l'email cliquable
        emailLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:bertheb39@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Contact depuis l'application Cantiques Dioula")
            }
            startActivitySafely(intent, "Aucune application de messagerie trouvée.")
        }
    }

    private fun startActivitySafely(intent: Intent, errorMessage: String) {
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
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