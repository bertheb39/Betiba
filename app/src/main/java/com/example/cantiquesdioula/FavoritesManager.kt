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
     * (Ton code est bon, pas de changement)
     */
    fun loadFavorites(context: Context, onComplete: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = auth.currentUser
            favoritesCache.clear()

            if (user != null) {
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
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Erreur de chargement des favoris Firestore", e)
                        onComplete()
                    }
            } else {
                val prefs = getLocalPrefs(context)
                val localStrings = prefs.getStringSet(KEY_LOCAL_FAVORITES, emptySet()) ?: emptySet()
                favoritesCache.addAll(localStrings.map { it.toInt() })
                Log.d(TAG, "Favoris chargés depuis SharedPreferences: ${favoritesCache.size} éléments.")

                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    /**
     * Ajoute un favori.
     *
     * !! CORRECTION APPLIQUÉE ICI !!
     * On utilise .set(..., SetOptions.merge()) au lieu de .update()
     * pour créer le document s'il n'existe pas.
     */
    fun addFavorite(context: Context, songId: Int) {
        if (favoritesCache.contains(songId)) return

        favoritesCache.add(songId)
        val user = auth.currentUser

        if (user != null) {
            // --- DÉBUT DE LA CORRECTION ---
            val userDoc = db.collection("users").document(user.uid)

            // On prépare les données à "fusionner" (merge)
            // FieldValue.arrayUnion() ajoute l'ID à la liste sans le dupliquer
            val favoriteData = mapOf("favorites" to FieldValue.arrayUnion(songId))

            // On utilise set() avec SetOptions.merge()
            // Cela crée le document s'il est absent, ou met à jour le champ "favorites" s'il existe.
            userDoc.set(favoriteData, SetOptions.merge())
                .addOnFailureListener { e ->
                    // Si même cette opération échoue (ex: pas de réseau), on log l'erreur.
                    Log.w(TAG, "Erreur 'addFavorite' Firestore avec set/merge", e)
                }
            // --- FIN DE LA CORRECTION ---
        } else {
            val prefs = getLocalPrefs(context)
            val localStrings = prefs.getStringSet(KEY_LOCAL_FAVORITES, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            localStrings.add(songId.toString())
            prefs.edit().putStringSet(KEY_LOCAL_FAVORITES, localStrings).apply()
        }
    }

    /**
     * Retire un favori.
     * (Ton code est bon, pas de changement. .update() est correct ici
     * car on ne peut pas retirer de favori d'un document qui n'existe pas)
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