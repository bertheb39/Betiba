package com.example.cantiquesdioula

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hbb20.CountryCodePicker
import java.util.concurrent.TimeUnit

class PhoneAuthFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    // Déclaration de TOUTES les vues
    private lateinit var ccp: CountryCodePicker
    private lateinit var editTextPhoneNumberOnly: TextInputEditText
    private lateinit var buttonSendCode: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutSendCode: LinearLayout
    private lateinit var layoutVerifyCode: LinearLayout

    // Vues qui causaient l'erreur
    private lateinit var editTextSmsCode: TextInputEditText
    private lateinit var buttonVerifyCode: Button
    private lateinit var textViewResendCode: TextView
    private lateinit var textViewCodeTitle: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_phone_auth, container, false)

        auth = FirebaseAuth.getInstance()

        // Initialisation de TOUTES les vues
        ccp = view.findViewById(R.id.ccp)
        editTextPhoneNumberOnly = view.findViewById(R.id.editTextPhoneNumberOnly)
        buttonSendCode = view.findViewById(R.id.buttonSendCode)
        progressBar = view.findViewById(R.id.progressBar)
        layoutSendCode = view.findViewById(R.id.layout_send_code)
        layoutVerifyCode = view.findViewById(R.id.layout_verify_code)

        // C'est ici que vos erreurs se produisaient.
        // Ces `findViewById` vont maintenant trouver les ID dans le layout
        editTextSmsCode = view.findViewById(R.id.editTextSmsCode)
        buttonVerifyCode = view.findViewById(R.id.buttonVerifyCode)
        textViewResendCode = view.findViewById(R.id.textViewResendCode)
        textViewCodeTitle = view.findViewById(R.id.textViewCodeTitle)

        ccp.registerCarrierNumberEditText(editTextPhoneNumberOnly)

        buttonSendCode.setOnClickListener {
            if (ccp.isValidFullNumber) {
                val fullPhoneNumber = ccp.fullNumberWithPlus
                showProgressBar()
                sendVerificationCode(fullPhoneNumber)
            } else {
                Toast.makeText(requireContext(), "Veuillez entrer un numéro valide.", Toast.LENGTH_SHORT).show()
                editTextPhoneNumberOnly.requestFocus()
            }
        }

        buttonVerifyCode.setOnClickListener {
            val code = editTextSmsCode.text.toString().trim()
            if (code.isNotEmpty() && code.length == 6) {
                showProgressBar()
                verifyCodeAndSignIn(code)
            } else {
                Toast.makeText(requireContext(), "Veuillez entrer le code à 6 chiffres.", Toast.LENGTH_SHORT).show()
            }
        }

        textViewResendCode.setOnClickListener {
            if (ccp.isValidFullNumber && resendToken != null) {
                val fullPhoneNumber = ccp.fullNumberWithPlus
                showProgressBar()
                resendVerificationCode(fullPhoneNumber)
            } else {
                Toast.makeText(requireContext(), "Impossible de renvoyer le code.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d("PhoneAuth", "Demande d'envoi du code SMS pour $phoneNumber")
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d("PhoneAuth", "onVerificationCompleted: ${credential.smsCode}")
            hideProgressBar()
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w("PhoneAuth", "onVerificationFailed", e)
            hideProgressBar()
            if (isAdded) {
                Toast.makeText(requireContext(), "Échec: ${e.message}", Toast.LENGTH_LONG).show()
            }
            // Afficher le layout d'envoi, cacher le layout de vérification
            layoutSendCode.visibility = View.VISIBLE
            layoutVerifyCode.visibility = View.GONE
            ccp.isEnabled = true
            editTextPhoneNumberOnly.isEnabled = true
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d("PhoneAuth", "onCodeSent: $verificationId")
            storedVerificationId = verificationId
            resendToken = token
            hideProgressBar()

            // Afficher le layout de vérification, cacher le layout d'envoi
            layoutSendCode.visibility = View.GONE
            layoutVerifyCode.visibility = View.VISIBLE

            ccp.isEnabled = false
            editTextPhoneNumberOnly.isEnabled = false

            if (isAdded) {
                Toast.makeText(requireContext(), "Code SMS envoyé.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyCodeAndSignIn(code: String) {
        if (storedVerificationId != null) {
            try {
                val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
                signInWithPhoneAuthCredential(credential)
                Log.d("PhoneAuth", "Tentative de connexion avec le code.")
            } catch (e: Exception) {
                hideProgressBar()
                Log.e("PhoneAuth", "Code invalide ou expiré", e)
                if (isAdded) {
                    Toast.makeText(requireContext(), "Code invalide ou expiré.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            hideProgressBar()
            Log.e("PhoneAuth", "storedVerificationId est null")
            if (isAdded) {
                Toast.makeText(requireContext(), "Erreur interne, veuillez renvoyer le code.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                hideProgressBar()
                if (task.isSuccessful) {
                    val user = task.result?.user
                    Log.d("PhoneAuth", "Connexion réussie: ${user?.uid}")
                    Toast.makeText(requireContext(), "Authentification réussie.", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Log.w("PhoneAuth", "signInWithCredential échec", task.exception)
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Échec: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun resendVerificationCode(phoneNumber: String) {
        if (resendToken != null) {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callbacks)
                .setForceResendingToken(resendToken!!)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
            Log.d("PhoneAuth", "Demande de renvoi du code SMS.")
            if (isAdded) {
                Toast.makeText(requireContext(), "Renvoi du code...", Toast.LENGTH_SHORT).show()
            }
        } else {
            hideProgressBar()
            Log.e("PhoneAuth", "resendToken est null")
            if (isAdded) {
                Toast.makeText(requireContext(), "Impossible de renvoyer le code.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        layoutSendCode.visibility = View.GONE
        layoutVerifyCode.visibility = View.GONE
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
        // Les callbacks gèrent l'affichage des layouts
    }
}