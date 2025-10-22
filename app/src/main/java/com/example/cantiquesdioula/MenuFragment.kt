package com.example.cantiquesdioula

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        val menuItems = listOf(
            MenuItemData("Paramètres", R.drawable.ic_settings),
            MenuItemData("Favoris", R.drawable.ic_favorite_filled),
            MenuItemData("Cantiques maîtrisés", R.drawable.ic_check_circle),

            // --- AJOUT 1: Le nouvel item de menu ---
            // (Assurez-vous que R.string.menu_update et R.drawable.ic_menu_update existent)
            MenuItemData(getString(R.string.menu_update), R.drawable.ic_menu_update),
            // --- Fin de l'ajout ---

            MenuItemData("Partager l'application", R.drawable.ic_share),
            MenuItemData("Commentaires et avis", R.drawable.ic_rate_review),
            MenuItemData("Faire un don", R.drawable.ic_donate),
            MenuItemData("À propos", R.drawable.ic_info),
            MenuItemData("Contactez-nous", R.drawable.ic_contact)
        )

        val recyclerView: RecyclerView = view.findViewById(R.id.menu_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = MenuAdapter(menuItems) { selectedItem ->
            when (selectedItem.title) {
                "Paramètres" -> {
                    startActivity(Intent(requireContext(), SettingsActivity::class.java))
                }
                "Favoris" -> {
                    val intent = Intent(requireContext(), SongListActivity::class.java).apply {
                        putExtra("LIST_TYPE", "FAVORITES")
                    }
                    startActivity(intent)
                }
                "Cantiques maîtrisés" -> {
                    val intent = Intent(requireContext(), SongListActivity::class.java).apply {
                        putExtra("LIST_TYPE", "MASTERED")
                    }
                    startActivity(intent)
                }
                "À propos" -> {
                    startActivity(Intent(requireContext(), AboutActivity::class.java))
                }
                "Faire un don" -> {
                    startActivity(Intent(requireContext(), DonateActivity::class.java))
                }
                "Contactez-nous" -> {
                    startActivity(Intent(requireContext(), ContactActivity::class.java))
                }
                "Commentaires et avis" -> {
                    openAppInPlayStore()
                }
                "Partager l'application" -> {
                    shareApplication()
                }

                // --- AJOUT 2: La logique de clic pour le nouvel item ---
                getString(R.string.menu_update) -> {
                    openAppInPlayStore() // On réutilise la même fonction
                }
                // --- Fin de l'ajout ---
            }
        }

        return view
    }

    // --- Votre fonction existante (parfaite pour la mise à jour) ---
    private fun openAppInPlayStore() {
        // Remplacez "com.example.cantiquesdioula" par votre véritable ID d'application quand elle sera publiée
        val packageName = "com.example.cantiquesdioula" // TODO: Mettre à jour si nécessaire
        try {
            // Essaie d'ouvrir directement dans l'application Play Store
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            // Si le Play Store n'est pas installé, ouvre dans le navigateur web
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    // --- Votre fonction existante ---
    private fun shareApplication() {
        // TODO: Mettre à jour l'URL avec votre vrai lien Play Store
        val shareText = "Découvrez l'application Cantiques Dioula ! Une collection complète de chants spirituels. Téléchargez-la ici : https://play.google.com/store/apps/details?id=com.example.cantiquesdioula"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Partager l'application via..."))
    }
}