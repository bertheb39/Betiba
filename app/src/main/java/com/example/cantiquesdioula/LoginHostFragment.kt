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

class LoginHostFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: LoginViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_host, container, false)

        viewPager = view.findViewById(R.id.view_pager_login_options)
        tabLayout = view.findViewById(R.id.tab_layout_login_options)
        adapter = LoginViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_email)
                1 -> getString(R.string.tab_phone)
                else -> null
            }
        }.attach()

        val goToRegisterText: TextView = view.findViewById(R.id.text_go_to_register)

        // --- BLOC CORRIGÉ (pour le texte en gras) ---
        // Ceci est le nouveau code qui corrige l'erreur "SpannableStringBuilder"
        val fullTextRegister = getString(R.string.auth_go_to_register)
        val boldTextRegister = getString(R.string.auth_sign_up_bold) // "S'INSCRIRE"
        val spannableRegister = SpannableString(fullTextRegister)

        // Trouve l'index de "S'INSCRIRE" dans "Je n'ai pas de compte. S'INSCRIRE"
        val startIndexRegister = fullTextRegister.indexOf(boldTextRegister)

        // Applique le gras seulement si le mot est trouvé (pour éviter les crashs)
        if (startIndexRegister != -1) {
            spannableRegister.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndexRegister,
                startIndexRegister + boldTextRegister.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        goToRegisterText.text = spannableRegister
        // --- FIN DU BLOC CORRIGÉ ---

        goToRegisterText.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.auth_fragment_container, RegisterHostFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}