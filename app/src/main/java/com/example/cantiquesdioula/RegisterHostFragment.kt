package com.example.cantiquesdioula

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class RegisterHostFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: RegisterViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register_host, container, false)

        viewPager = view.findViewById(R.id.view_pager_register_options)
        tabLayout = view.findViewById(R.id.tab_layout_register_options)
        adapter = RegisterViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_email)
                1 -> getString(R.string.tab_phone)
                else -> null
            }
        }.attach()

        val goToLoginText: TextView = view.findViewById(R.id.text_go_to_login)

        // --- BLOC CORRIGÉ (pour le texte en gras) ---
        // Ceci est le nouveau code qui corrige l'erreur "SpannableStringBuilder"
        val fullTextLogin = getString(R.string.auth_go_to_login)
        val boldTextLogin = getString(R.string.auth_log_in_bold) // "SE CONNECTER"
        val spannableLogin = SpannableString(fullTextLogin)

        // Trouve l'index de "SE CONNECTER" dans "J'ai déjà un compte. SE CONNECTER"
        val startIndexLogin = fullTextLogin.indexOf(boldTextLogin)

        // Applique le gras seulement si le mot est trouvé
        if (startIndexLogin != -1) {
            spannableLogin.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndexLogin,
                startIndexLogin + boldTextLogin.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        goToLoginText.text = spannableLogin
        // --- FIN DU BLOC CORRIGÉ ---

        goToLoginText.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }
}