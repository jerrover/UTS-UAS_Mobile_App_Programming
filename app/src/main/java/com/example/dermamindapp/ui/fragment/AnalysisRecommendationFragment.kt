package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dermamindapp.R
import com.example.dermamindapp.databinding.FragmentAnalysisRecommendationBinding
import com.example.dermamindapp.ui.adapter.ProductAdapter
import com.example.dermamindapp.ui.viewmodel.ProductViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class AnalysisRecommendationFragment : Fragment() {

    private var _binding: FragmentAnalysisRecommendationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisRecommendationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. MATIKAN NAVBAR (Sesuai ID di XML Anda: bottom_navigation)
        toggleBottomNavigation(false)

        setupRecyclerView()
        setupObservers()

        val analysisResult = arguments?.getString("analysisResult") ?: ""
        setupToolbar()
        loadRecommendation(analysisResult)

        // 2. ATUR LOGIKA BACK SYSTEM (Tombol Back HP)
        // Saat dipencet, paksa pindah ke Journey, jangan balik ke scan result
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToJourney()
            }
        })
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbar
        toolbar.title = "Hasil Analisis"

        // Pasang ikon Back
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back)

        // 3. ATUR LOGIKA BACK TOOLBAR (Pojok Kiri Atas)
        toolbar.setNavigationOnClickListener {
            navigateToJourney()
        }
    }

    // Fungsi khusus untuk pindah ke halaman Journey
    private fun navigateToJourney() {
        try {
            // Opsi: Bersihkan stack sampai Home agar tidak menumpuk, lalu buka Journey
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false)
                .build()

            // Arahkan ke ID Fragment Journey (sesuai main_nav.xml)
            findNavController().navigate(R.id.skinJourneyFragment, null, navOptions)

        } catch (e: Exception) {
            // Fallback jika gagal, balik ke Home saja
            findNavController().navigate(R.id.homeFragment)
        }
    }

    // Fungsi untuk Menyembunyikan/Menampilkan Navbar
    private fun toggleBottomNavigation(isVisible: Boolean) {
        // Menggunakan ID 'bottom_navigation' sesuai file XML yang Anda kirim
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun loadRecommendation(resultKeyword: String) {
        if (resultKeyword.isNotEmpty()) {
            binding.tvSubtitle.text = "Berdasarkan analisis: ${resultKeyword.replace("_", " ")}"
            when {
                resultKeyword.contains("Jerawat", true) || resultKeyword.contains("Acne", true) -> {
                    viewModel.filterBySuitability("Acne")
                }
                resultKeyword.contains("Kering", true) || resultKeyword.contains("Dry", true) -> {
                    viewModel.filterBySuitability("Dry")
                }
                resultKeyword.contains("Minyak", true) || resultKeyword.contains("Oily", true) || resultKeyword.contains("Berminyak", true) -> {
                    viewModel.filterBySuitability("Oily")
                }
                resultKeyword.contains("Kusam", true) || resultKeyword.contains("Dull", true) -> {
                    viewModel.filterBySuitability("Dull")
                }
                else -> {
                    viewModel.search(resultKeyword)
                }
            }
        } else {
            binding.tvSubtitle.text = "Menampilkan semua produk rekomendasi."
            viewModel.filterBySuitability("Semua")
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(emptyList()) { product ->
            // Navigasi ke Detail Produk
            val action = AnalysisRecommendationFragmentDirections
                .actionAnalysisRecommendationFragmentToProductDetailsFragment(product)
            findNavController().navigate(action)
        }
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AnalysisRecommendationFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.products.observe(viewLifecycleOwner) { productList ->
            adapter.updateData(productList)
            if (productList.isEmpty()) {
                binding.tvSubtitle.text = "${binding.tvSubtitle.text}\n(Belum ada produk spesifik untuk kategori ini)"
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // PENTING: Munculkan kembali Navbar saat keluar dari halaman ini
        toggleBottomNavigation(true)
        _binding = null
    }
}