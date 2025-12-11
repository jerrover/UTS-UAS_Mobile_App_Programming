package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.example.dermamindapp.R
import com.example.dermamindapp.ui.adapter.ArticleAdapter
import com.example.dermamindapp.ui.viewmodel.HomeViewModel
import com.example.dermamindapp.ui.viewmodel.ProfileViewModel

// Tambahkan impor untuk Handler dan Looper
import android.os.Handler
import android.os.Looper
//
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var articleAdapter: ArticleAdapter

    private lateinit var tvUserName: TextView
    private lateinit var ivProfile: ImageView
    private lateinit var rvArticles: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnStartAnalysis: MaterialCardView
    private lateinit var tvSeeAllArticles: TextView

    // >>> START: Logika Auto-Scroll
    private val handler = Handler(Looper.getMainLooper())
    private val SCROLL_DELAY: Long = 3000 // 3 detik
    private var currentPage = 0

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val totalItems = articleAdapter.itemCount
            if (totalItems > 1) {
                // Hitung posisi berikutnya, dan kembali ke 0 jika sudah di akhir
                currentPage = (currentPage + 1) % totalItems

                // Scroll ke posisi berikutnya
                rvArticles.smoothScrollToPosition(currentPage)

                // Jadwal ulang runnable
                handler.postDelayed(this, SCROLL_DELAY)
            } else {
                // Hentikan jika hanya ada 1 atau 0 item
                stopAutoScroll()
            }
        }
    }

    private fun startAutoScroll() {
        handler.removeCallbacks(autoScrollRunnable) // Hapus callback sebelumnya
        currentPage = 0 // Mulai dari posisi pertama
        // Langsung mulai scroll tanpa menunggu delay awal
        handler.postDelayed(autoScrollRunnable, SCROLL_DELAY)
    }

    private fun stopAutoScroll() {
        handler.removeCallbacks(autoScrollRunnable)
    }
    // <<< END: Logika Auto-Scroll

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    // ... (onViewCreated tetap sama)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Init ViewModel
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // 2. Bind Views
        tvUserName = view.findViewById(R.id.tvHomeUserName)
        ivProfile = view.findViewById(R.id.ivHomeProfile)
        rvArticles = view.findViewById(R.id.rvHomeArticles)
        progressBar = view.findViewById(R.id.progressBarArticles)
        btnStartAnalysis = view.findViewById(R.id.btnStartAnalysis)
        tvSeeAllArticles = view.findViewById(R.id.tvSeeAllArticles)

        // 3. Setup RecyclerView
        setupArticleRecyclerView()

        // 4. Setup Listeners (FIX: Navigasi Kamera yang Aman)
        btnStartAnalysis.setOnClickListener {
            try {
                // Pastikan ID 'cameraFragment' ada di graph navigasi Anda
                findNavController().navigate(R.id.cameraFragment)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membuka kamera. Cek Nav Graph.", Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", "Nav Error: ${e.message}")
            }
        }

        tvSeeAllArticles.setOnClickListener {
            try {
                findNavController().navigate(R.id.articlesFragment)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Nav Error: ${e.message}")
            }
        }

        // 5. Observers
        setupObservers()

        // 6. Load Data
        profileViewModel.loadProfile()
    }

    private fun setupArticleRecyclerView() {
        // Init Adapter dengan Lambda onClick
        articleAdapter = ArticleAdapter { article ->
            try {
                // Navigasi ke Detail Artikel (Pastikan argument di nav_graph sesuai)
                val action = HomeFragmentDirections.actionHomeFragmentToArticleDetailFragment(article)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Gagal buka artikel: ${e.message}")
            }
        }

        rvArticles.apply {
            // Layout Manager sudah Horizontal
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = articleAdapter
        }
    }

    private fun setupObservers() {
        // Profil
        profileViewModel.userProfile.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                tvUserName.text = user.name.ifEmpty { "Glow User" }
                if (user.photoUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(user.photoUrl)
                        .signature(ObjectKey(System.currentTimeMillis().toString()))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.ic_profile_avatar)
                        .circleCrop()
                        .into(ivProfile)
                }
            }
        }

        // Artikel
        homeViewModel.articles.observe(viewLifecycleOwner) { articles ->
            if (!articles.isNullOrEmpty()) {
                articleAdapter.submitList(articles.take(5))
                rvArticles.visibility = View.VISIBLE

                // Mulai auto-scroll jika ada lebih dari satu artikel
                if (articles.size > 1) {
                    startAutoScroll()
                } else {
                    stopAutoScroll()
                }
            } else {
                stopAutoScroll() // Hentikan jika list kosong
            }
        }

        // Loading
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        profileViewModel.loadProfile()
        // Coba mulai ulang auto-scroll jika data sudah ada saat fragment kembali aktif
        val articles = homeViewModel.articles.value
        if (!articles.isNullOrEmpty() && articles.size > 1) {
            startAutoScroll()
        }
    }

    // Hentikan auto-scroll saat Fragment tidak terlihat
    override fun onPause() {
        super.onPause()
        stopAutoScroll()
    }

    // Hentikan auto-scroll saat View dihancurkan untuk mencegah kebocoran memori (memory leak)
    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoScroll()
    }
}