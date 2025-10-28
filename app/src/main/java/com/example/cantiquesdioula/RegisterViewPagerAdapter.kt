package com.example.cantiquesdioula

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class RegisterViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 2 // Email et Téléphone
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RegisterEmailFragment() // Position 0 = Le formulaire d'inscription email
            1 -> PhoneAuthFragment()   // Position 1 = Le formulaire de téléphone (on le réutilise !)
            else -> throw IllegalStateException("Position invalide")
        }
    }
}