package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.data.db.DatabaseHelper
import com.example.dermamindapp.data.model.Article
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var prefsHelper: PreferencesHelper

    // UI Components
    private lateinit var lastAnalysisCard: MaterialCardView
    private lateinit var tvEmptyState: TextView
    private lateinit var tvLastAnalysisDate: TextView
    private lateinit var tvLastAnalysisResult: TextView
    private lateinit var btnViewDetails: Button
    private lateinit var greetingTextView: TextView
    private lateinit var scanButton: Button
    private lateinit var articleCard1: MaterialCardView
    private lateinit var articleImage: ImageView
    private lateinit var btnViewAllArticles: Button

    // Data dummy artikel
    private var articles: List<Article> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 1. Inisialisasi Database & Prefs
        dbHelper = DatabaseHelper(requireContext())
        prefsHelper = PreferencesHelper(requireContext())

        // 2. Binding Views
        lastAnalysisCard = view.findViewById(R.id.lastAnalysisCard)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        tvLastAnalysisDate = view.findViewById(R.id.tvLastAnalysisDate)
        tvLastAnalysisResult = view.findViewById(R.id.tvLastAnalysisResult)
        btnViewDetails = view.findViewById(R.id.btnViewDetails)
        greetingTextView = view.findViewById(R.id.greetingTextView)

        scanButton = view.findViewById(R.id.scanButton)
        articleCard1 = view.findViewById(R.id.articleCard1)
        articleImage = view.findViewById(R.id.articleImage)
        btnViewAllArticles = view.findViewById(R.id.btnViewAllArticles)

        // 3. Setup Tombol Scan
        scanButton.setOnClickListener {
            try {
                // Menggunakan NavController dari Activity (Induk) untuk navigasi ke CameraFragment
                requireActivity().findNavController(R.id.nav_host_fragment)
                    .navigate(R.id.action_mainFragment_to_cameraFragment)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Gagal navigasi ke kamera: ${e.message}")
            }
        }

        // 4. Setup Artikel (Dummy Data)
        setupArticles()

        // 5. Setup Tombol Lihat Semua Artikel
        btnViewAllArticles.setOnClickListener {
            try {
                // Navigasi internal dalam main_nav
                findNavController().navigate(R.id.action_homeFragment_to_articlesFragment)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Navigasi ke ArticlesFragment gagal", e)
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Muat ulang data setiap kali fragment tampil
        loadLastAnalysis()
    }

    private fun loadLastAnalysis() {
        // Menggunakan lifecycleScope agar aman dari memory leak
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Ambil data user dari SharedPrefs
                val userName = prefsHelper.getString(PreferencesHelper.KEY_USER_NAME) ?: "User"

                // Cek apakah userId valid (Opsional, karena dbHelper sekarang auto-handle ID)
                val userId = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)
                if (userId.isNullOrEmpty()) {
                    showEmptyState(userName)
                    return@launch
                }

                // PERBAIKAN: Panggil getAllAnalyses() TANPA parameter userId
                // DatabaseHelper sudah menyimpan userId secara internal
                val analyses = dbHelper.getAllAnalyses()
                val lastAnalysis = analyses.firstOrNull()

                if (lastAnalysis != null) {
                    // KASUS: USER LAMA (Sudah ada riwayat)
                    lastAnalysisCard.visibility = View.VISIBLE
                    tvEmptyState.visibility = View.GONE

                    greetingTextView.text = "Hello, $userName!"

                    // Format Tanggal
                    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                    tvLastAnalysisDate.text = sdf.format(Date(lastAnalysis.date))

                    // Tampilkan Hasil Singkat
                    tvLastAnalysisResult.text = lastAnalysis.result

                    // Tombol Detail
                    btnViewDetails.setOnClickListener {
                        val action = HomeFragmentDirections.actionHomeFragmentToSkinDetailFragment(lastAnalysis)
                        findNavController().navigate(action)
                    }

                } else {
                    // KASUS: USER BARU (ID ada tapi belum scan)
                    showEmptyState(userName)
                }

            } catch (e: Exception) {
                Log.e("HomeFragment", "Gagal memuat data analisis", e)
                // Jika error, anggap kosong agar tidak crash
                lastAnalysisCard.visibility = View.GONE
                tvEmptyState.visibility = View.VISIBLE
            }
        }
    }

    // Fungsi helper untuk menampilkan tampilan kosong
    private fun showEmptyState(userName: String) {
        lastAnalysisCard.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
        greetingTextView.text = "Welcome, $userName!\nLet's start your journey."
    }

    private fun setupArticles() {
        articles = listOf(
            Article(1, "5 Mitos Skincare yang Salah Kaprah", "Konten artikel 1...", "https://www.beauty-heroes.com/wp-content/uploads/As-organic-skincares-1150x912.jpg.webp"),
            Article(2, "Kenali Tipe Kulitmu", "Konten artikel 2...", "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEiPekzufeCYmTDOuTkVNGHKH1qHPkuG6nPNxjYeHT8aMhTIJt6Hoc3I5eT12Qd4RShChna6pf3f9Qj3PCzYJwObwjmdq6j-amYZn8p8qpBVJBVFK93wgVcHTpFZbIN5qKX1m4OvbLhmo4oc3xYrvu2VrqpX5gl04ZEaEzzhXXVb9Tc6-gTUPSmU0IOv_q5E/w518-h518-rw/gambar-lima-jenis-kulit-wajah.webp"),
            Article(3, "Bahan Aktif untuk Jerawat", "Konten artikel 3...", "https://www.mitrakeluarga.com/_next/image?url=https%3A%2F%2Fd3uhejzrzvtlac.cloudfront.net%2Fcompro%2FarticleDesktop%2Fe5eb00ff-0dff-4319-bf11-3f8ce7b73e8e.webp&w=1920&q=100")
        )

        val latestArticle = articles.firstOrNull()

        if (latestArticle != null) {
            Glide.with(this)
                .load(latestArticle.imageUrl)
                .placeholder(R.drawable.ic_article)
                .centerCrop()
                .into(articleImage)

            articleCard1.setOnClickListener {
                try {
                    val action = HomeFragmentDirections.actionHomeFragmentToArticleDetailFragment(latestArticle)
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Navigasi ke ArticleDetail gagal", e)
                }
            }
        }
    }
}