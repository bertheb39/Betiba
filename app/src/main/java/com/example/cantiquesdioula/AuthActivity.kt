package com.example.cantiquesdioula

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Déclaration des vues
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var forgotPasswordTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Initialiser Firebase Auth
        auth = Firebase.auth

        // Initialiser les vues avec les bons IDs
        emailEditText = findViewById(R.id.auth_credential)
        passwordEditText = findViewById(R.id.auth_password)
        loginButton = findViewById(R.id.button_login)
        registerTextView = findViewById(R.id.text_register)
        forgotPasswordTextView = findViewById(R.id.text_forgot_password)

        // Logique de Connexion pour le bouton "Se connecter"
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty()) {
                emailEditText.error = "L'e-mail ne peut pas être vide"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Le mot de passe ne peut pas être vide"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            // Lancer la connexion Firebase
            signInUserWithEmail(email, password)
        }

        // On connecte le texte "Je n'ai pas de compte. S'inscrire"
        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Logique pour le mot de passe oublié (placeholder)
        forgotPasswordTextView.setOnClickListener {
            Toast.makeText(this, "Fonctionnalité bientôt disponible", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Fonction de connexion de l'utilisateur
     */
    private fun signInUserWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Connexion réussie
                    Log.d("AuthActivity", "signInWithEmail:success")
                    Toast.makeText(baseContext, "Connexion réussie.", Toast.LENGTH_SHORT).show()

                    // Redémarrer MainActivity pour rafraîchir le menu
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish() // Ferme l'écran de connexion
                } else {
                    // Si la connexion échoue
                    Log.w("AuthActivity", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Échec de l'authentification: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    public override fun onStart() {
        super.onStart()
        // Vérifier si l'utilisateur est déjà connecté
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Si oui, on ferme cette activité et on retourne à l'accueil
            Log.d("AuthActivity", "Utilisateur déjà connecté, fermeture.")
            finish()
        }
    }
}