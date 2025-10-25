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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MenuFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)

        auth = Firebase.auth
        val menuItems = mutableListOf<MenuItemData>()

        // Vérifie si l'utilisateur est connecté
        if (auth.currentUser == null) {
            menuItems.add(MenuItemData(getString(R.string.menu_login), R.drawable.ic_menu_login))
        }

        // Ajouter le reste des boutons
        menuItems.add(MenuItemData(getString(R.string.menu_settings), R.drawable.ic_settings))
        menuItems.add(MenuItemData(getString(R.string.menu_favorites), R.drawable.ic_favorite_filled))
        menuItems.add(MenuItemData(getString(R.string.menu_mastered), R.drawable.ic_check_circle))
        menuItems.add(MenuItemData(getString(R.string.menu_update), R.drawable.ic_menu_update))
        menuItems.add(MenuItemData(getString(R.string.menu_share_app), R.drawable.ic_share))
        menuItems.add(MenuItemData(getString(R.string.menu_rate_review), R.drawable.ic_rate_review))
        menuItems.add(MenuItemData(getString(R.string.menu_donate), R.drawable.ic_donate))
        menuItems.add(MenuItemData(getString(R.string.menu_about), R.drawable.ic_info))
        menuItems.add(MenuItemData(getString(R.string.menu_contact), R.drawable.ic_contact))

        // Si l'utilisateur est connecté, ajouter le bouton de déconnexion à la fin
        if (auth.currentUser != null) {
            menuItems.add(MenuItemData(getString(R.string.menu_logout), R.drawable.ic_menu_logout))
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.menu_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = MenuAdapter(menuItems) { selectedItem ->
            when (selectedItem.title) {
                getString(R.string.menu_login) -> {
                    startActivity(Intent(requireContext(), AuthActivity::class.java))
                }
                getString(R.string.menu_logout) -> {
                    auth.signOut()
                    // Vider les caches locaux
                    FavoritesManager.clearFavoritesOnLogout(requireContext())
                    MasteredManager.clearMasteredOnLogout(requireContext())

                    Toast.makeText(requireContext(), "Déconnexion réussie.", Toast.LENGTH_SHORT).show()
                    // Redémarrer l'application pour rafraîchir le menu
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                getString(R.string.menu_settings) -> {
                    startActivity(Intent(requireContext(), SettingsActivity::class.java))
                }
                getString(R.string.menu_favorites) -> {
                    val intent = Intent(requireContext(), SongListActivity::class.java).apply {
                        putExtra("LIST_TYPE", "FAVORITES")
                    }
                    startActivity(intent)
                }
                getString(R.string.menu_mastered) -> {
                    val intent = Intent(requireContext(), SongListActivity::class.java).apply {
                        putExtra("LIST_TYPE", "MASTERED")
                    }
                    startActivity(intent)
                }
                getString(R.string.menu_about) -> {
                    startActivity(Intent(requireContext(), AboutActivity::class.java))
                }
                getString(R.string.menu_donate) -> {
                    startActivity(Intent(requireContext(), DonateActivity::class.java))
                }
                getString(R.string.menu_contact) -> {
                    startActivity(Intent(requireContext(), ContactActivity::class.java))
                }
                getString(R.string.menu_rate_review) -> {
                    openAppInPlayStore()
                }
                getString(R.string.menu_share_app) -> {
                    shareApplication()
                }
                getString(R.string.menu_update) -> {
                    openAppInPlayStore()
                }
            }
        }

        return view
    }

    private fun openAppInPlayStore() {
        val packageName = "com.example.cantiquesdioula"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun shareApplication() {
        val shareText = "Découvrez l'application Cantiques Dioula ! Une collection complète de chants spirituels. Téléchargez-la ici : https://play.google.com/store/apps/details?id=com.example.cantiquesdioula"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Partager l'application via..."))
    }
}