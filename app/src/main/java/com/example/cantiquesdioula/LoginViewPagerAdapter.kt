package com.example.cantiquesdioula

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

// Cet adaptateur a besoin d'un Fragment en paramètre (le LoginHostFragment)
class LoginViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 2 // 2 sous-onglets : Email (0) et Téléphone (1)
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LoginEmailFragment() // Position 0 = Le formulaire de connexion email
            1 -> PhoneAuthFragment()  // Position 1 = Le formulaire de téléphone (on le réutilise !)
            else -> throw IllegalStateException("Position invalide")
        }
    }
}