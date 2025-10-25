package com.example.cantiquesdioula

import android.content.Context
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FavoritesManager {

    private const val TAG = "FavoritesManager"
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    // Cache en mémoire pour un accès instantané (l'app est plus rapide)
    private var favoritesCache = mutableSetOf<Int>()

    // SharedPreferences pour les favoris de l'utilisateur "déconnecté"
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
     */
    fun loadFavorites(context: Context, onComplete: () -> Unit) {
        val user = auth.currentUser
        favoritesCache.clear() // Vider le cache avant de charger

        if (user != null) {
            // Utilisateur connecté : Charger depuis Firestore
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firestoreFavorites = document.get("favorites") as? List<Long>
                        if (firestoreFavorites != null) {
                            // Convertir List<Long> en MutableSet<Int>
                            favoritesCache.addAll(firestoreFavorites.map { it.toInt() })
                            Log.d(TAG, "Favoris chargés depuis Firestore: ${favoritesCache.size} éléments.")
                        }
                    } else {
                        Log.d(TAG, "Aucun document utilisateur, favoris vides.")
                    }
                    onComplete()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Erreur de chargement des favoris Firestore", e)
                    onComplete()
                }
        } else {
            // Utilisateur déconnecté : Charger depuis SharedPreferences
            val prefs = getLocalPrefs(context)
            val localStrings = prefs.getStringSet(KEY_LOCAL_FAVORITES, emptySet()) ?: emptySet()
            favoritesCache.addAll(localStrings.map { it.toInt() })
            Log.d(TAG, "Favoris chargés depuis SharedPreferences: ${favoritesCache.size} éléments.")
            onComplete()
        }
    }

    /**
     * Ajoute un favori.
     * Si connecté: au cache ET à Firestore.
     * Si déconnecté: au cache ET aux SharedPreferences.
     */
    fun addFavorite(context: Context, songId: Int) {
        if (favoritesCache.contains(songId)) return // Déjà favori

        favoritesCache.add(songId)
        val user = auth.currentUser

        if (user != null) {
            // Utilisateur connecté : Sauvegarder sur Firestore
            val userDoc = db.collection("users").document(user.uid)
            // FieldValue.arrayUnion() ajoute l'ID seulement s'il n'existe pas
            userDoc.update("favorites", FieldValue.arrayUnion(songId))
                .addOnFailureListener { e ->
                    // Échec (probablement le document n'existe pas), on le crée
                    if (e is com.google.firebase.firestore.FirebaseFirestoreException && e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND) {
                        userDoc.set(mapOf("favorites" to listOf(songId)), SetOptions.merge())
                    } else {
                        Log.w(TAG, "Erreur 'addFavorite' Firestore", e)
                    }
                }
        } else {
            // Utilisateur déconnecté : Sauvegarder en local
            val prefs = getLocalPrefs(context)
            val localStrings = prefs.getStringSet(KEY_LOCAL_FAVORITES, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            localStrings.add(songId.toString())
            prefs.edit().putStringSet(KEY_LOCAL_FAVORITES, localStrings).apply()
        }
    }

    /**
     * Retire un favori.
     * Si connecté: du cache ET de Firestore.
     * Si déconnecté: du cache ET des SharedPreferences.
     */
    fun removeFavorite(context: Context, songId: Int) {
        if (!favoritesCache.contains(songId)) return // Pas en favori

        favoritesCache.remove(songId)
        val user = auth.currentUser

        if (user != null) {
            // Utilisateur connecté : Mettre à jour Firestore
            val userDoc = db.collection("users").document(user.uid)
            // FieldValue.arrayRemove() retire l'ID
            userDoc.update("favorites", FieldValue.arrayRemove(songId))
                .addOnFailureListener { e ->
                    Log.w(TAG, "Erreur 'removeFavorite' Firestore", e)
                }
        } else {
            // Utilisateur déconnecté : Mettre à jour SharedPreferences
            val prefs = getLocalPrefs(context)
            val localStrings = prefs.getStringSet(KEY_LOCAL_FAVORITES, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            localStrings.remove(songId.toString())
            prefs.edit().putStringSet(KEY_LOCAL_FAVORITES, localStrings).apply()
        }
    }

    /**
     * Inverse l'état de favori (ajoute ou retire).
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
     */
    fun clearFavoritesOnLogout(context: Context) {
        favoritesCache.clear()
        val prefs = getLocalPrefs(context)
        prefs.edit().remove(KEY_LOCAL_FAVORITES).apply()
        Log.d(TAG, "Cache et favoris locaux vidés.")
    }
}