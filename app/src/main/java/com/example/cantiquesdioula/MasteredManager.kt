package com.example.cantiquesdioula

import android.content.Context
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
// Imports Coroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object MasteredManager {

    private const val TAG = "MasteredManager"
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private var masteredCache = mutableSetOf<Int>()

    private const val PREFS_NAME = "MasteredPrefs"
    private const val KEY_LOCAL_MASTERED = "key_local_mastered"

    private fun getLocalPrefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Vérifie si un cantique est maîtrisé (utilise le cache).
     */
    fun isMastered(songId: Int): Boolean {
        return masteredCache.contains(songId)
    }

    /**
     * Charge les cantiques maîtrisés au démarrage.
     * (Ton code est bon, pas de changement)
     */
    fun loadMastered(context: Context, onComplete: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = auth.currentUser
            masteredCache.clear()

            if (user != null) {
                db.collection("users").document(user.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val firestoreMastered = document.get("mastered") as? List<Long>
                            if (firestoreMastered != null) {
                                masteredCache.addAll(firestoreMastered.map { it.toInt() })
                                Log.d(TAG, "Maîtrisés chargés depuis Firestore: ${masteredCache.size} éléments.")
                            }
                        }
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Erreur de chargement des 'mastered' Firestore", e)
                        onComplete()
                    }
            } else {
                val prefs = getLocalPrefs(context)
                val localStrings = prefs.getStringSet(KEY_LOCAL_MASTERED, emptySet()) ?: emptySet()
                masteredCache.addAll(localStrings.map { it.toInt() })
                Log.d(TAG, "Maîtrisés chargés depuis SharedPreferences: ${masteredCache.size} éléments.")

                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    /**
     * Ajoute un cantique aux maîtrisés.
     *
     * !! CORRECTION APPLIQUÉE ICI !!
     * On utilise .set(..., SetOptions.merge()) au lieu de .update()
     * pour créer le document s'il n'existe pas.
     */
    fun addMastered(context: Context, songId: Int) {
        if (masteredCache.contains(songId)) return

        masteredCache.add(songId)
        val user = auth.currentUser

        if (user != null) {
            // --- DÉBUT DE LA CORRECTION ---
            val userDoc = db.collection("users").document(user.uid)

            // On prépare les données à "fusionner" (merge)
            // FieldValue.arrayUnion() ajoute l'ID à la liste sans le dupliquer
            val masteredData = mapOf("mastered" to FieldValue.arrayUnion(songId))

            // On utilise set() avec SetOptions.merge()
            userDoc.set(masteredData, SetOptions.merge())
                .addOnFailureListener { e ->
                    Log.w(TAG, "Erreur 'addMastered' Firestore avec set/merge", e)
                }
            // --- FIN DE LA CORRECTION ---
        } else {
            val prefs = getLocalPrefs(context)
            val localStrings = prefs.getStringSet(KEY_LOCAL_MASTERED, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            localStrings.add(songId.toString())
            prefs.edit().putStringSet(KEY_LOCAL_MASTERED, localStrings).apply()
        }
    }

    /**
     * Retire un cantique des maîtrisés.
     * (Fonction inchangée)
     */
    fun removeMastered(context: Context, songId: Int) {
        if (!masteredCache.contains(songId)) return

        masteredCache.remove(songId)
        val user = auth.currentUser

        if (user != null) {
            val userDoc = db.collection("users").document(user.uid)
            userDoc.update("mastered", FieldValue.arrayRemove(songId))
                .addOnFailureListener { e ->
                    Log.w(TAG, "Erreur 'removeMastered' Firestore", e)
                }
        } else {
            val prefs = getLocalPrefs(context)
            val localStrings = prefs.getStringSet(KEY_LOCAL_MASTERED, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            localStrings.remove(songId.toString())
            prefs.edit().putStringSet(KEY_LOCAL_MASTERED, localStrings).apply()
        }
    }

    /**
     * Inverse l'état "maîtrisé".
     * (Fonction inchangée)
     */
    fun toggleMastered(context: Context, songId: Int) {
        if (isMastered(songId)) {
            removeMastered(context, songId)
        } else {
            addMastered(context, songId)
        }
    }

    /**
     * Vide le cache et les "maîtrisés" locaux lors de la déconnexion.
     * (Fonction inchangée)
     */
    fun clearMasteredOnLogout(context: Context) {
        masteredCache.clear()
        val prefs = getLocalPrefs(context)
        prefs.edit().remove(KEY_LOCAL_MASTERED).apply()
        Log.d(TAG, "Cache et 'mastered' locaux vidés.")
    }
}