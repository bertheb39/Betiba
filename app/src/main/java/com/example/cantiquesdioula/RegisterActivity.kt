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

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Déclaration des vues
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerEmailButton: Button
    private lateinit var registerPhoneButton: Button
    private lateinit var loginTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialiser Firebase Auth
        auth = Firebase.auth

        // Initialiser les vues (basé sur vos IDs de activity_register.xml)
        emailEditText = findViewById(R.id.register_email)
        passwordEditText = findViewById(R.id.register_password)
        confirmPasswordEditText = findViewById(R.id.register_confirm_password)
        registerEmailButton = findViewById(R.id.button_register_email)
        registerPhoneButton = findViewById(R.id.button_register_phone)
        loginTextView = findViewById(R.id.text_login)

        // Clic sur le bouton "S'INSCRIRE (EMAIL)"
        registerEmailButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            // --- Validation des champs ---
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
            if (confirmPassword.isEmpty()) {
                confirmPasswordEditText.error = "Veuillez confirmer le mot de passe"
                confirmPasswordEditText.requestFocus()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                confirmPasswordEditText.error = "Les mots de passe ne correspondent pas"
                confirmPasswordEditText.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6) {
                passwordEditText.error = "Le mot de passe doit contenir au moins 6 caractères"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }
            // --- Fin Validation ---

            // Lancer la création de l'utilisateur
            registerUserWithEmail(email, password)
        }

        // Clic sur "J'ai déjà un compte. Se connecter"
        loginTextView.setOnClickListener {
            finish() // Ferme cet écran et retourne à AuthActivity
        }

        // Clic sur le bouton téléphone (placeholder)
        registerPhoneButton.setOnClickListener {
            Toast.makeText(this, "Inscription par téléphone bientôt disponible", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Fonction de création de l'utilisateur
     */
    private fun registerUserWithEmail(email: String, password: String) {
        Toast.makeText(this, "Création du compte...", Toast.LENGTH_SHORT).show()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inscription réussie
                    Log.d("RegisterActivity", "createUserWithEmail:success")
                    Toast.makeText(baseContext, "Inscription réussie !", Toast.LENGTH_SHORT).show()

                    // Redémarrer MainActivity pour rafraîchir le menu
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish() // Ferme l'écran d'inscription
                } else {
                    // Si l'inscription échoue
                    Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Échec de l'authentification: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}