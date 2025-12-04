package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope // Import ini penting
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.data.db.DatabaseHelper
import com.example.dermamindapp.data.model.Article
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch // Import Coroutine
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var lastAnalysisCard: MaterialCardView
    private lateinit var tvEmptyState: TextView
    private lateinit var tvLastAnalysisDate: TextView
    private lateinit var tvLastAnalysisResult: TextView
    private lateinit var btnViewDetails: Button
    private lateinit var articles: List<Article>
    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var greetingTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        dbHelper = DatabaseHelper(requireContext())
        prefsHelper = PreferencesHelper(requireContext())

        lastAnalysisCard = view.findViewById(R.id.lastAnalysisCard)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        tvLastAnalysisDate = view.findViewById(R.id.tvLastAnalysisDate)
        tvLastAnalysisResult = view.findViewById(R.id.tvLastAnalysisResult)
        btnViewDetails = view.findViewById(R.id.btnViewDetails)
        greetingTextView = view.findViewById(R.id.greetingTextView)

        val scanButton = view.findViewById<Button>(R.id.scanButton)
        val articleCard1 = view.findViewById<MaterialCardView>(R.id.articleCard1)
        val btnViewAllArticles = view.findViewById<Button>(R.id.btnViewAllArticles)
        val articleImage = view.findViewById<ImageView>(R.id.articleImage)

        val userName = prefsHelper.getString(PreferencesHelper.KEY_USER_NAME)
        greetingTextView.text = "Hello, ${userName ?: "User"}!"

        scanButton.setOnClickListener {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_mainFragment_to_cameraFragment)
        }

        articles = listOf(
            Article(1, "5 Mitos Skincare yang Salah Kaprah", "Konten artikel 1...", "https://www.beauty-heroes.com/wp-content/uploads/As-organic-skincares-1150x912.jpg.webp"),
            Article(2, "Kenali Tipe Kulitmu", "Konten artikel 2...", "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEiPekzufeCYmTDOuTkVNGHKH1qHPkuG6nPNxjYeHT8aMhTIJt6Hoc3I5eT12Qd4RShChna6pf3f9Qj3PCzYJwObwjmdq6j-amYZn8p8qpBVJBVFK93wgVcHTpFZbIN5qKX1m4OvbLhmo4oc3xYrvu2VrqpX5gl04ZEaEzzhXXVb9Tc6-gTUPSmU0IOv_q5E/w518-h518-rw/gambar-lima-jenis-kulit-wajah.webp"),
            Article(3, "Bahan Aktif untuk Jerawat", "Konten artikel 3...", "https://www.mitrakeluarga.com/_next/image?url=https%3A%2F%2Fd3uhejzrzvtlac.cloudfront.net%2Fcompro%2FarticleDesktop%2Fe5eb00ff-0dff-4319-bf11-3f8ce7b73e8e.webp&w=1920&q=100")
        )

        val latestArticle = articles.firstOrNull()

        if (latestArticle != null) {
            Glide.with(this)
                .load(latestArticle.imageUrl)
                .into(articleImage)

            articleCard1.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToArticleDetailFragment(latestArticle)
                findNavController().navigate(action)
            }
        }

        btnViewAllArticles.setOnClickListener{
            val action = HomeFragmentDirections.actionHomeFragmentToArticlesFragment()
            findNavController().navigate(action)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadLastAnalysis()
    }

    private fun loadLastAnalysis() {
        // PERBAIKAN: Bungkus dengan lifecycleScope.launch karena dbHelper sekarang async
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Sekarang getAllAnalyses() aman dipanggil di sini
                val analyses = dbHelper.getAllAnalyses()
                val lastAnalysis = analyses.firstOrNull()

                if (lastAnalysis != null) {
                    lastAnalysisCard.visibility = View.VISIBLE
                    tvEmptyState.visibility = View.GONE

                    tvLastAnalysisDate.text = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(lastAnalysis.date))
                    tvLastAnalysisResult.text = lastAnalysis.result

                    btnViewDetails.setOnClickListener {
                        val action = HomeFragmentDirections.actionHomeFragmentToSkinDetailFragment(lastAnalysis)
                        findNavController().navigate(action)
                    }
                } else {
                    lastAnalysisCard.visibility = View.GONE
                    tvEmptyState.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // Opsional: Handle error jika koneksi gagal dll
                // Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                lastAnalysisCard.visibility = View.GONE
                tvEmptyState.visibility = View.VISIBLE
            }
        }
    }
}