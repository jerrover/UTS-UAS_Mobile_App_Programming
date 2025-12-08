package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.data.db.DatabaseHelper
import com.example.dermamindapp.data.model.Article // Pastikan Import Article
import com.example.dermamindapp.ui.viewmodel.HomeViewModel // Pastikan Import ViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var homeViewModel: HomeViewModel // Tambahan

    // UI Components
    private lateinit var lastAnalysisCard: MaterialCardView
    private lateinit var tvEmptyState: TextView
    private lateinit var tvLastAnalysisDate: TextView
    private lateinit var tvLastAnalysisResult: TextView
    private lateinit var btnViewDetails: Button
    private lateinit var greetingTextView: TextView
    private lateinit var articleImage: ImageView
    private lateinit var articleCard1: MaterialCardView // Kartu Artikel Utama

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 1. Inisialisasi ViewModel
        dbHelper = DatabaseHelper(requireContext())
        prefsHelper = PreferencesHelper(requireContext())
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Bind Views
        lastAnalysisCard = view.findViewById(R.id.lastAnalysisCard)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        tvLastAnalysisDate = view.findViewById(R.id.tvLastAnalysisDate)
        tvLastAnalysisResult = view.findViewById(R.id.tvLastAnalysisResult)
        btnViewDetails = view.findViewById(R.id.btnViewDetails)
        greetingTextView = view.findViewById(R.id.greetingTextView)

        // View Artikel
        articleImage = view.findViewById(R.id.articleImage)
        articleCard1 = view.findViewById(R.id.articleCard1)
        val btnViewAllArticles = view.findViewById<Button>(R.id.btnViewAllArticles)
        val scanButton = view.findViewById<Button>(R.id.scanButton)

        val userName = prefsHelper.getString(PreferencesHelper.KEY_USER_NAME)
        greetingTextView.text = "Hello, ${userName ?: "User"}!"

        scanButton.setOnClickListener {
            requireActivity().findNavController(R.id.nav_host_fragment).navigate(R.id.action_mainFragment_to_cameraFragment)
        }

        // --- BAGIAN PENTING: OBSERVASI ARTIKEL ---

        // Dengarkan data dari ViewModel (bukan list manual lagi!)
        homeViewModel.articles.observe(viewLifecycleOwner) { articleList ->
            if (articleList.isNotEmpty()) {
                val latestArticle = articleList.first() // Ambil artikel pertama

                // 1. Tampilkan Gambar
                Glide.with(this)
                    .load(latestArticle.imageUrl)
                    .into(articleImage)

                // 2. Tampilkan Judul & Isi (INI YANG BARU)
                view.findViewById<TextView>(R.id.articleTitle).text = latestArticle.title
                view.findViewById<TextView>(R.id.articleContent).text = latestArticle.content

                // 3. Klik kartu -> Buka Detail
                articleCard1.setOnClickListener {
                    val action = HomeFragmentDirections.actionHomeFragmentToArticleDetailFragment(latestArticle)
                    findNavController().navigate(action)
                }
            }
        }

        // PERINTAH: Ambil data sekarang!
        homeViewModel.fetchArticles()

        // ----------------------------------------

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
        // Biarkan kode loadLastAnalysis ini tetap seperti semula
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userId = prefsHelper.getString("KEY_FIREBASE_USER_ID") ?: ""
                val analyses = dbHelper.getAllAnalyses(userId)
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
                lastAnalysisCard.visibility = View.GONE
                tvEmptyState.visibility = View.VISIBLE
            }
        }
    }
}