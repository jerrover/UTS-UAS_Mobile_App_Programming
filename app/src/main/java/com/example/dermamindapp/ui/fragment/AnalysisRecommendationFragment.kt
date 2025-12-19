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
import com.example.dermamindapp.data.db.DatabaseHelper
import com.example.dermamindapp.databinding.FragmentAnalysisRecommendationBinding
import com.example.dermamindapp.ui.adapter.ProductAdapter
import com.example.dermamindapp.ui.viewmodel.ProductViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip

class AnalysisRecommendationFragment : Fragment() {

    private var _binding: FragmentAnalysisRecommendationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter
    private lateinit var dbHelper: DatabaseHelper // Tambahkan helper DB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisRecommendationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext()) // Inisialisasi DB
        toggleBottomNavigation(false)
        setupRecyclerView()
        setupObservers()

        val analysisResult = arguments?.getString("analysisResult") ?: ""
        setupToolbar()

        setupDynamicChips(analysisResult)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToJourney()
            }
        })
    }

    private fun setupDynamicChips(resultString: String) {
        val chipGroup = binding.chipGroupDynamic
        chipGroup.removeAllViews()

        val problems = resultString.split(",").map { it.trim() }
        var firstCategoryFound: String? = null

        for (problem in problems) {
            val (label, categoryFilter) = mapProblemToCategory(problem)

            if (label != null && categoryFilter != null) {
                val chip = layoutInflater.inflate(R.layout.item_chip_dynamic, chipGroup, false) as Chip
                chip.text = label

                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        viewModel.filterBySuitability(categoryFilter)
                    }
                }
                chipGroup.addView(chip)

                if (firstCategoryFound == null) {
                    firstCategoryFound = categoryFilter
                    chip.isChecked = true
                }
            }
        }

        if (firstCategoryFound != null) {
            viewModel.filterBySuitability(firstCategoryFound!!)
            binding.tvSubtitle.text = "Rekomendasi difokuskan untuk masalah yang terdeteksi."
        } else {
            binding.tvSubtitle.text = "Menampilkan semua rekomendasi."
            viewModel.filterBySuitability("Semua")
        }
    }

    private fun mapProblemToCategory(problem: String): Pair<String?, String?> {
        return when {
            problem.contains("Jerawat", true) || problem.contains("Acne", true) ->
                Pair("Jerawat (Acne)", "Acne")
            problem.contains("Kering", true) || problem.contains("Dry", true) ->
                Pair("Kulit Kering", "Dry")
            problem.contains("Minyak", true) || problem.contains("Oily", true) || problem.contains("Berminyak", true) ->
                Pair("Kulit Berminyak", "Oily")
            problem.contains("Kusam", true) || problem.contains("Dull", true) ->
                Pair("Kulit Kusam", "Dull")
            problem.contains("Pori", true) || problem.contains("Pore", true) ->
                Pair("Pori-pori Besar", "Oily")
            else -> Pair(null, null)
        }
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbar
        toolbar.title = "Hasil Analisis"
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { navigateToJourney() }
    }

    private fun navigateToJourney() {
        try {
            val navOptions = NavOptions.Builder().setPopUpTo(R.id.homeFragment, false).build()
            findNavController().navigate(R.id.skinJourneyFragment, null, navOptions)
        } catch (e: Exception) {
            findNavController().navigate(R.id.homeFragment)
        }
    }

    private fun toggleBottomNavigation(isVisible: Boolean) {
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        // PERBAIKAN DI SINI: Tambahkan callback kedua (onAddToShelfClick)
        adapter = ProductAdapter(
            productList = emptyList(),
            onItemClick = { product ->
                val action = AnalysisRecommendationFragmentDirections
                    .actionAnalysisRecommendationFragmentToProductDetailsFragment(product)
                findNavController().navigate(action)
            },
            onAddToShelfClick = { product ->
                // Panggil ViewModel untuk simpan ke database
                viewModel.addToShelf(dbHelper, product)
                Toast.makeText(context, "Menambahkan ke Rak Skincare...", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AnalysisRecommendationFragment.adapter
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

    override fun onDestroyView() {
        super.onDestroyView()
        toggleBottomNavigation(true)
        _binding = null
    }
}