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
                // --- AJOUT DE LA LOGIQUE POUR "COMMENTAIRES ET AVIS" ---
                "Commentaires et avis" -> {
                    openAppInPlayStore()
                }
                // --- AJOUT DE LA LOGIQUE POUR "PARTAGER L'APPLICATION" ---
                "Partager l'application" -> {
                    shareApplication()
                }
            }
        }

        return view
    }

    // --- NOUVELLE FONCTION ---
    private fun openAppInPlayStore() {
        // Remplacez "com.example.cantiquesdioula" par votre véritable ID d'application quand elle sera publiée
        val packageName = "com.example.cantiquesdioula"
        try {
            // Essaie d'ouvrir directement dans l'application Play Store
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            // Si le Play Store n'est pas installé, ouvre dans le navigateur web
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    // --- NOUVELLE FONCTION ---
    private fun shareApplication() {
        val shareText = "Découvrez l'application Cantiques Dioula ! Une collection complète de chants spirituels. Téléchargez-la ici : https://play.google.com/store/apps/details?id=com.example.cantiquesdioula"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Partager l'application via..."))
    }
}