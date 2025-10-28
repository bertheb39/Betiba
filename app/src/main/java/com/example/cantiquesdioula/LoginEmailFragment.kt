package com.example.cantiquesdioula

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginEmailFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordText: TextView
    private var progressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_email, container, false)

        auth = FirebaseAuth.getInstance()

        // Liaison des vues avec les IDs corrigés
        emailEditText = view.findViewById(R.id.login_email)
        passwordEditText = view.findViewById(R.id.login_password)
        loginButton = view.findViewById(R.id.button_login_email) // ID Corrigé
        forgotPasswordText = view.findViewById(R.id.text_forgot_password) // ID Corrigé

        // Trouver le ProgressBar dans l'Activity parente (AuthActivity)
        // Note : L'ID 'auth_progress_bar' doit exister dans 'activity_auth.xml'
        try {
            progressBar = activity?.findViewById(R.id.auth_progress_bar)
        } catch (e: Exception) {
            Log.e("LoginEmailFragment", "ProgressBar non trouvé dans AuthActivity", e)
        }


        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty()) {
                emailEditText.error = "Email requis"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Mot de passe requis"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            signInUserWithEmail(email, password)
        }

        forgotPasswordText.setOnClickListener {
            Toast.makeText(requireContext(), "Fonctionnalité de réinitialisation à venir.", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun signInUserWithEmail(email: String, password: String) {
        showProgressBar()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                hideProgressBar()
                if (task.isSuccessful) {
                    Log.d("LoginEmailFragment", "signInWithEmail:success")
                    Toast.makeText(requireContext(), "Connexion réussie.", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Log.w("LoginEmailFragment", "signInWithEmail:failure", task.exception)
                    if(isAdded) { // Vérifie si le fragment est toujours attaché
                        Toast.makeText(
                            requireContext(),
                            "Échec de l'authentification: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }

    private fun navigateToMainActivity() {
        if (!isAdded) return // Vérifie si le fragment est attaché avant de naviguer
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar?.visibility = View.GONE
    }
}