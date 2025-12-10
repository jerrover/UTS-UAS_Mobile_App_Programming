package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.databinding.FragmentProductRecommendationBinding
import com.example.dermamindapp.ui.adapter.ProductAdapter
import com.example.dermamindapp.ui.viewmodel.ProductViewModel
import com.google.android.material.chip.Chip

class ProductRecommendationFragment : Fragment() {

    private var _binding: FragmentProductRecommendationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter
    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductRecommendationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = PreferencesHelper(requireContext())

        setupRecyclerView()
        setupObservers()
        setupListeners()

        // JALANKAN LOGIKA FILTER OTOMATIS
        handleAutoFilter()
    }

    private fun handleAutoFilter() {
        // 1. Cek apakah ada kiriman data "analysisResult" dari halaman AnalysisResultFragment?
        val analysisResultArgs = arguments?.getString("analysisResult")

        if (!analysisResultArgs.isNullOrEmpty()) {
            // JIKA ADA HASIL SCAN: Filter berdasarkan hasil scan
            autoSelectFilter(analysisResultArgs)
            binding.tvSubtitle.text = "Rekomendasi berdasarkan analisis: ${analysisResultArgs.replace("_", " ")}"
        } else {
            // JIKA TIDAK ADA SCAN: Ambil dari Profil User (Preferences)
            val userProblems = prefsHelper.getString(PreferencesHelper.KEY_PREFERENCES) ?: ""
            val userSkinType = prefsHelper.getString(PreferencesHelper.KEY_SKIN_TYPE) ?: ""

            if (userProblems.isNotEmpty()) {
                val firstProblem = userProblems.split(",")[0].trim()
                autoSelectFilter(firstProblem)
                binding.tvSubtitle.text = "Disarankan untukmu ($userSkinType - $firstProblem)"
            } else if (userSkinType.isNotEmpty()) {
                autoSelectFilter(userSkinType)
                binding.tvSubtitle.text = "Disarankan untuk kulit $userSkinType"
            }
        }
    }

    private fun autoSelectFilter(keyword: String) {
        // Reset dulu biar bersih
        binding.chipGroupFilter.clearCheck()

        // Logika pencocokan kata kunci dengan Filter Chip
        when {
            keyword.contains("Jerawat", true) || keyword.contains("Acne", true) -> {
                binding.chipAcne.isChecked = true
                viewModel.filterBySuitability("Acne")
            }
            keyword.contains("Kering", true) || keyword.contains("Dry", true) -> {
                binding.chipDry.isChecked = true
                viewModel.filterBySuitability("Dry")
            }
            keyword.contains("Minyak", true) || keyword.contains("Oily", true) || keyword.contains("Berminyak", true) -> {
                binding.chipOily.isChecked = true
                viewModel.filterBySuitability("Oily")
            }
            keyword.contains("Kusam", true) || keyword.contains("Dull", true) -> {
                binding.chipDull.isChecked = true
                viewModel.filterBySuitability("Dull")
            }
            else -> {
                // Jika tidak ada Chip yang cocok, filter "Semua" tapi cari di text
                viewModel.search(keyword)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(emptyList()) { product ->
            // Navigasi ke Detail
            val action = ProductRecommendationFragmentDirections
                .actionProductRecommendationFragmentToProductDetailsFragment(product)
            findNavController().navigate(action)
        }
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ProductRecommendationFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.products.observe(viewLifecycleOwner) { productList ->
            adapter.updateData(productList)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.search(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.chipGroupFilter.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            if (chip != null) {
                val category = mapChipTextToFilter(chip.text.toString())
                viewModel.filterBySuitability(category)
            } else {
                viewModel.filterBySuitability("Semua")
            }
        }
    }

    private fun mapChipTextToFilter(text: String): String {
        return when (text) {
            "Jerawat (Acne)" -> "Acne"
            "Kering (Dry)" -> "Dry"
            "Berminyak (Oily)" -> "Oily"
            "Kusam (Dull)" -> "Dull"
            else -> "Semua"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}