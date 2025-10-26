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

object FavoritesManager {

    private const val TAG = "FavoritesManager"
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private var favoritesCache = mutableSetOf<Int>()

    private const val PREFS_NAME = "FavoritesPrefs"
    private const val KEY_LOCAL_FAVORITES = "key_local_favorites"

    private fun getLocalPrefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Vérifie si un cantique est en favori (utilise le cache local).
     */
    fun isFavorite(songId: Int): Boolean {
        return favoritesCache.contains(songId)
    }

    /**
     * Charge les favoris au démarrage de l'application.
     * Si connecté: depuis Firestore vers le cache.
     * Si déconnecté: depuis SharedPreferences vers le cache.
     * * CORRECTION : S'exécute maintenant sur un thread d'arrière-plan (Dispatchers.IO)
     * pour ne pas bloquer l'interface utilisateur.
     */
    fun loadFavorites(context: Context, onComplete: () -> Unit) {
        // On lance le travail sur un thread d'arrière-plan
        CoroutineScope(Dispatchers.IO).launch {
            val user = auth.currentUser
            favoritesCache.clear() // Vider le cache avant de charger

            if (user != null) {
                // Utilisateur connecté : Charger depuis Firestore
                // L'API Firebase gère son propre thread, mais on l'appelle
                // depuis le thread IO pour être sûr que l'initialisation ne bloque pas.
                db.collection("users").document(user.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val firestoreFavorites = document.get("favorites") as? List<Long>
                            if (firestoreFavorites != null) {
                                favoritesCache.addAll(firestoreFavorites.map { it.toInt() })
                                Log.d(TAG, "Favoris chargés depuis Firestore: ${favoritesCache.size} éléments.")
                            }
                        } else {
                            Log.d(TAG, "Aucun document utilisateur, favoris vides.")
                        }
                        // Le callback de Firebase est déjà sur le thread principal
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Erreur de chargement des favoris Firestore", e)
                        // Le callback de Firebase est déjà sur le thread principal
                        onComplete()
                    }
            } else {
                // Utilisateur déconnecté : Charger depuis SharedPreferences
                // C'est cette opération (I/O) qui bloquait le thread principal
                val prefs = getLocalPrefs(context)
                val localStrings = prefs.getStringSet(KEY_LOCAL_FAVORITES, emptySet()) ?: emptySet()
                favoritesCache.addAll(localStrings.map { it.toInt() })
                Log.d(TAG, "Favoris chargés depuis SharedPreferences: ${favoritesCache.size} éléments.")

                // On revient sur le thread principal pour appeler le callback
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    /**
     * Ajoute un favori.
     * (Fonction inchangée)
     */
    fun addFavorite(context: Context, songId: Int) {
        if (favoritesCache.contains(songId)) return

        favoritesCache.add(songId)
        val user = auth.currentUser

        if (user != null) {
            val userDoc = db.collection("users").document(user.uid)
            userDoc.update("favorites", FieldValue.arrayUnion(songId))
                .addOnFailureListener { e ->
                    if (e is com.google.firebase.firestore.FirebaseFirestoreException && e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND) {
                        userDoc.set(mapOf("favorites" to listOf(songId)), SetOptions.merge())
                    } else {
                        Log.w(TAG, "Erreur 'addFavorite' Firestore", e)
                    }
                }
        } else {
            val prefs = getLocalPrefs(context)
            val localStrings = prefs.getStringSet(KEY_LOCAL_FAVORITES, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            localStrings.add(songId.toString())
            prefs.edit().putStringSet(KEY_LOCAL_FAVORITES, localStrings).apply()
        }
    }

    /**
     * Retire un favori.
     * (Fonction inchangée)
     */
    fun removeFavorite(context: Context, songId: Int) {
        if (!favoritesCache.contains(songId)) return

        favoritesCache.remove(songId)
        val user = auth.currentUser

        if (user != null) {
            val userDoc = db.collection("users").document(user.uid)
            userDoc.update("favorites", FieldValue.arrayRemove(songId))
                .addOnFailureListener { e ->
                    Log.w(TAG, "Erreur 'removeFavorite' Firestore", e)
                }
        } else {
            val prefs = getLocalPrefs(context)
            val localStrings = prefs.getStringSet(KEY_LOCAL_FAVORITES, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            localStrings.remove(songId.toString())
            prefs.edit().putStringSet(KEY_LOCAL_FAVORITES, localStrings).apply()
        }
    }

    /**
     * Inverse l'état de favori (ajoute ou retire).
     * (Fonction inchangée)
     */
    fun toggleFavorite(context: Context, songId: Int) {
        if (isFavorite(songId)) {
            removeFavorite(context, songId)
        } else {
            addFavorite(context, songId)
        }
    }

    /**
     * Vide le cache et les favoris locaux lors de la déconnexion.
     * (Fonction inchangée)
     */
    fun clearFavoritesOnLogout(context: Context) {
        favoritesCache.clear()
        val prefs = getLocalPrefs(context)
        prefs.edit().remove(KEY_LOCAL_FAVORITES).apply()
        Log.d(TAG, "Cache et favoris locaux vidés.")
    }
}