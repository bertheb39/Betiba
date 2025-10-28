package com.example.cantiquesdioula

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
// !! Supprimez les imports de ViewPager2, TabLayout, et TabLayoutMediator !!
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    // !! On supprime viewPager, tabLayout, et adapter !!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth) // Charge le FrameLayout

        auth = Firebase.auth

        // 1. Vérifier si l'utilisateur est déjà connecté
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("AuthActivity", "Utilisateur déjà connecté (${currentUser.uid}), redirection.")
            navigateToMainActivity()
            return // Quitte onCreate pour ne pas charger le fragment
        }

        // 2. Si non connecté et si c'est la première création de l'activité :
        if (savedInstanceState == null) {
            Log.d("AuthActivity", "Aucun utilisateur, chargement du LoginHostFragment.")
            // On charge le fragment "Connexion" par défaut
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_fragment_container, LoginHostFragment())
                .commit()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}