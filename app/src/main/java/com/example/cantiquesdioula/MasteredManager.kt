package com.example.cantiquesdioula

import android.content.Context
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
// Imports Coroutines que nous ajoutons
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
     *
     * CORRECTION : S'exécute maintenant sur un thread d'arrière-plan (Dispatchers.IO)
     * pour ne pas bloquer l'interface utilisateur.
     */
    fun loadMastered(context: Context, onComplete: () -> Unit) {
        // On lance le travail sur un thread d'arrière-plan
        CoroutineScope(Dispatchers.IO).launch {
            val user = auth.currentUser
            masteredCache.clear()

            if (user != null) {
                // Connecté: Charger depuis Firestore
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
                        // Callback sur le thread principal (géré par Firebase)
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Erreur de chargement des 'mastered' Firestore", e)
                        // Callback sur le thread principal (géré par Firebase)
                        onComplete()
                    }
            } else {
                // Déconnecté: Charger depuis SharedPreferences
                // C'est cette opération (I/O) qui bloquait le thread principal
                val prefs = getLocalPrefs(context)
                val localStrings = prefs.getStringSet(KEY_LOCAL_MASTERED, emptySet()) ?: emptySet()
                masteredCache.addAll(localStrings.map { it.toInt() })
                Log.d(TAG, "Maîtrisés chargés depuis SharedPreferences: ${masteredCache.size} éléments.")

                // On revient sur le thread principal pour appeler le callback
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    /**
     * Ajoute un cantique aux maîtrisés.
     * (Fonction inchangée)
     */
    fun addMastered(context: Context, songId: Int) {
        if (masteredCache.contains(songId)) return

        masteredCache.add(songId)
        val user = auth.currentUser

        if (user != null) {
            val userDoc = db.collection("users").document(user.uid)
            userDoc.update("mastered", FieldValue.arrayUnion(songId))
                .addOnFailureListener { e ->
                    if (e is com.google.firebase.firestore.FirebaseFirestoreException && e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND) {
                        userDoc.set(mapOf("mastered" to listOf(songId)), SetOptions.merge())
                    } else {
                        Log.w(TAG, "Erreur 'addMastered' Firestore", e)
                    }
                }
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