package com.example.cantiquesdioula

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterEmailFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var registerButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register_email, container, false)

        auth = Firebase.auth

        emailEditText = view.findViewById(R.id.register_email)
        passwordEditText = view.findViewById(R.id.register_password)
        confirmPasswordEditText = view.findViewById(R.id.register_confirm_password)
        registerButton = view.findViewById(R.id.button_register_email)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (email.isEmpty()) {
                emailEditText.error = "L'e-mail ne peut pas être vide"
                emailEditText.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty() || password.length < 6) {
                passwordEditText.error = "Mot de passe d'au moins 6 caractères"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                confirmPasswordEditText.error = "Les mots de passe ne correspondent pas"
                confirmPasswordEditText.requestFocus()
                return@setOnClickListener
            }

            createUserWithEmail(email, password)
        }
        return view
    }

    private fun createUserWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterEmailFragment", "createUserWithEmail:success")
                    Toast.makeText(requireContext(), "Inscription réussie.", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Log.w("RegisterEmailFragment", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(requireContext(), "Échec: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}