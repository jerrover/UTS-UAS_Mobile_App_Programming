package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController // Pastikan import ini ada
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.data.db.DatabaseHelper
import com.example.dermamindapp.data.model.Article
import com.example.dermamindapp.data.model.SkinAnalysis
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

// Fragment ini berfungsi sebagai layar utama (beranda) aplikasi.
class HomeFragment : Fragment() {

    // Helper untuk interaksi dengan database SQLite.
    private lateinit var dbHelper: DatabaseHelper
    // Komponen UI untuk menampilkan kartu analisis terakhir.
    private lateinit var lastAnalysisCard: MaterialCardView
    // Tampilan teks saat tidak ada riwayat analisis.
    private lateinit var tvEmptyState: TextView
    // Komponen UI untuk detail analisis terakhir.
    private lateinit var tvLastAnalysisDate: TextView
    private lateinit var tvLastAnalysisResult: TextView
    private lateinit var btnViewDetails: Button
    // Daftar artikel yang akan ditampilkan.
    private lateinit var articles: List<Article>
    // Helper untuk mengakses SharedPreferences.
    private lateinit var prefsHelper: PreferencesHelper
    // Tampilan teks untuk sapaan pengguna.
    private lateinit var greetingTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Inisialisasi helper dan komponen UI.
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

        // Mengatur teks sapaan berdasarkan nama pengguna yang tersimpan.
        val userName = prefsHelper.getString(PreferencesHelper.KEY_USER_NAME)
        greetingTextView.text = "Hello, ${userName ?: "User"}!"

        // Menangani navigasi untuk tombol scan utama.
        scanButton.setOnClickListener {
            // Menggunakan NavController dari Activity untuk navigasi antar-grafik navigasi.
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_mainFragment_to_cameraFragment)
        }

        // Mengelola data dan navigasi untuk fitur artikel.
        articles = listOf(
            Article(1, "5 Mitos Skincare yang Salah Kaprah", "Konten artikel 1...", "https://www.beauty-heroes.com/wp-content/uploads/As-organic-skincares-1150x912.jpg.webp"),
            Article(2, "Kenali Tipe Kulitmu", "Konten artikel 2...", "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEiPekzufeCYmTDOuTkVNGHKH1qHPkuG6nPNxjYeHT8aMhTIJt6Hoc3I5eT12Qd4RShChna6pf3f9Qj3PCzYJwObwjmdq6j-amYZn8p8qpBVJBVFK93wgVcHTpFZbIN5qKX1m4OvbLhmo4oc3xYrvu2VrqpX5gl04ZEaEzzhXXVb9Tc6-gTUPSmU0IOv_q5E/w518-h518-rw/gambar-lima-jenis-kulit-wajah.webp"),
            Article(3, "Bahan Aktif untuk Jerawat", "Konten artikel 3...", "https://www.mitrakeluarga.com/_next/image?url=https%3A%2F%2Fd3uhejzrzvtlac.cloudfront.net%2Fcompro%2FarticleDesktop%2Fe5eb00ff-0dff-4319-bf11-3f8ce7b73e8e.webp&w=1920&q=100")
        )

        // Menampilkan artikel terbaru di kartu pratinjau.
        val latestArticle = articles.firstOrNull()

        if (latestArticle != null) {
            // Memuat gambar artikel menggunakan Glide.
            Glide.with(this)
                .load(latestArticle.imageUrl)
                .into(articleImage)

            // Navigasi ke detail artikel saat kartu diklik.
            articleCard1.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToArticleDetailFragment(latestArticle)
                findNavController().navigate(action)
            }
        }

        // Navigasi ke daftar semua artikel.
        btnViewAllArticles.setOnClickListener{
            val action = HomeFragmentDirections.actionHomeFragmentToArticlesFragment()
            findNavController().navigate(action)
        }

        return view
    }

    // Memuat ulang data analisis terakhir setiap kali fragment ini ditampilkan kembali.
    override fun onResume() {
        super.onResume()
        loadLastAnalysis()
    }

    // Fungsi untuk memuat dan menampilkan data analisis terakhir dari database.
    private fun loadLastAnalysis() {
        val analyses = dbHelper.getAllAnalyses()
        val lastAnalysis = analyses.firstOrNull()

        if (lastAnalysis != null) {
            // Jika ada riwayat, tampilkan kartu analisis.
            lastAnalysisCard.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE

            // Mengisi data ke dalam kartu.
            tvLastAnalysisDate.text = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(lastAnalysis.date))
            tvLastAnalysisResult.text = lastAnalysis.result

            // Navigasi ke detail analisis saat tombol "Lihat Detail" diklik.
            btnViewDetails.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToSkinDetailFragment(lastAnalysis)
                findNavController().navigate(action)
            }
        } else {
            // Jika tidak ada riwayat, tampilkan pesan untuk memulai analisis.
            lastAnalysisCard.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
        }
    }
}