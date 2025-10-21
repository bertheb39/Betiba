package com.example.cantiquesdioula

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.InputStreamReader

class CategoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_categories, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.categories_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 1. Charger la liste complète de tous les cantiques une seule fois
        val allSongs = loadSongsFromAssets()

        // 2. Définir vos catégories et les numéros correspondants
        val categoryRanges = mapOf(
            "Ala Tanuli Beetiw" to (1..20),
            "Ala Dence Yesu Bangi Koo - Nowεli" to (21..42),
            "Ala Dence Yesu bε Mɔgɔw Cεma" to (43..49),
            "Ala Dence Yesu Joli Bɔn Koo" to (50..58),
            "Ala Dence Yesu Tanuli Koo" to (51..68), // Attention: chevauchement avec la précédente
            "Ala ka Kuma Koo" to (69..72),
            "Ala Koo" to (73..77),
            "Ala Dence Yesu bε Mɔgɔ Kεneya" to (78..78),
            "Dalili Koo" to (79..90),
            "Denmisenw Beetiw" to (91..106),
            "Furu Koo" to (107..110),
            "Kibaru Diman Jεnsεnni Koo" to (111..123),
            "Kitabu la Beetiw" to (124..136),
            "Kisili Koo" to (137..164),
            "Kɔrɔbɔli ani Tɔɔrɔ Koo" to (165..177),
            "Danabaa ka Taama ani Seereya Koo" to (178..233),
            "Danabaa k'a Yεrε Bila Ala ye Koo" to (234..237),
            "Matigi Yesu ka Tabali Koo" to (238..241),
            "Nii Senu Koo" to (242..246),
            "Nilifεnw Di Tuma Beeti" to (247..247),
            "Nimisi Koo" to (248..256),
            "Sankolo Koo" to (257..269),
            "Setigi Yesu Koo" to (270..283),
            "Yesu Gwengweyiri Koo" to (284..292),
            "Yesu Krisita Koo" to (293..324),
            "Yesu Nali Filanan Koo" to (325..346),
            "Yesu Tɔgɔ Koo" to (347..355),
            "Danabaa K'a Yεrε Di Ala Ma" to (356..357)
        )

        // 3. Créer la liste finale des catégories avec les cantiques correspondants
        val realCategories = mutableListOf<Category>()
        categoryRanges.forEach { (categoryName, range) ->
            val songsInCategory = allSongs.filter { it.id in range }
            realCategories.add(Category(categoryName, songsInCategory))
        }

        // 4. Donner la liste réelle à l'adaptateur
        recyclerView.adapter = CategoryAdapter(realCategories)

        return view
    }

    private fun loadSongsFromAssets(): List<Song> {
        return try {
            val inputStream = requireContext().assets.open("CAD.json")
            val reader = InputStreamReader(inputStream)
            val songData = Gson().fromJson(reader, SongData::class.java)
            songData.songs
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}