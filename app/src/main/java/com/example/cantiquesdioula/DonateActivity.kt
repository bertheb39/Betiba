package com.example.cantiquesdioula

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class DonateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        val toolbar: Toolbar = findViewById(R.id.donate_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Affiche la flèche de retour

        // Rendre les numéros cliquables
        val orangeMoneyNumber: TextView = findViewById(R.id.orange_money_number)
        val sankMoneyNumber: TextView = findViewById(R.id.sank_money_number)

        orangeMoneyNumber.setOnClickListener {
            // Pour les codes USSD, il faut encoder le '#'
            val orangeUssdCode = Uri.encode("*144*2*1*64721227#")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$orangeUssdCode"))
            startActivitySafely(intent, "Impossible d'ouvrir l'application Téléphone.")
        }

        sankMoneyNumber.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+22664721227"))
            startActivitySafely(intent, "Impossible d'ouvrir l'application Téléphone.")
        }
    }

    private fun startActivitySafely(intent: Intent, errorMessage: String) {
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // Gère le clic sur la flèche de retour
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}