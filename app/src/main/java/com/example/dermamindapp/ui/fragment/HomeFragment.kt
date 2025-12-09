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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

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
    }
}