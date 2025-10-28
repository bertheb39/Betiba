package com.example.cantiquesdioula

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginEmailFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_email, container, false)

        auth = Firebase.auth

        emailEditText = view.findViewById(R.id.login_email)
        passwordEditText = view.findViewById(R.id.login_password)
        loginButton = view.findViewById(R.id.button_login_email)
        forgotPasswordTextView = view.findViewById(R.id.text_forgot_password)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            signInUserWithEmail(email, password)
        }

        forgotPasswordTextView.setOnClickListener {
            showForgotPasswordDialog()
        }

        return view
    }

    private fun signInUserWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginEmailFragment", "signInWithEmail:success")
                    Toast.makeText(requireContext(), "Connexion réussie.", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Log.w("LoginEmailFragment", "signInWithEmail:failure", task.exception)
                    Toast.makeText(requireContext(), "Échec: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.forgot_password_title))
        builder.setMessage(getString(R.string.forgot_password_message))

        val input = EditText(requireContext())
        input.hint = getString(R.string.forgot_password_email_hint)
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.forgot_password_send)) { dialog, _ ->
            val email = input.text.toString().trim()
            if (email.isNotEmpty()) {
                sendPasswordResetEmail(email)
            } else {
                Toast.makeText(requireContext(), "Veuillez entrer un e-mail", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.forgot_password_cancel)) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("LoginEmailFragment", "Email de réinitialisation envoyé.")
                    Toast.makeText(requireContext(), getString(R.string.forgot_password_success), Toast.LENGTH_SHORT).show()
                } else {
                    Log.w("LoginEmailFragment", "Erreur envoi e-mail", task.exception)
                    Toast.makeText(requireContext(), "${getString(R.string.forgot_password_failure)} ${task.exception?.message}", Toast.LENGTH_LONG).show()
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