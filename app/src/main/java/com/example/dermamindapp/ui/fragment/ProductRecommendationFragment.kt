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
import com.example.dermamindapp.R
import com.example.dermamindapp.databinding.FragmentProductRecommendationBinding
import com.example.dermamindapp.ui.adapter.ProductAdapter
import com.example.dermamindapp.ui.viewmodel.ProductViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.example.dermamindapp.data.db.DatabaseHelper

class ProductRecommendationFragment : Fragment() {

    private var _binding: FragmentProductRecommendationBinding? = null
    private lateinit var dbHelper: DatabaseHelper
    private val binding get() = _binding!!

    private val viewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductRecommendationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleBottomNavigation(true)
        dbHelper = DatabaseHelper(requireContext())

        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.filterBySuitability("Semua")
    }

    // Fungsi helper untuk memastikan Navbar muncul
    private fun toggleBottomNavigation(isVisible: Boolean) {
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        if (bottomNav != null) {
            bottomNav.visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            productList = emptyList(),
            onItemClick = { product ->
                val action = ProductRecommendationFragmentDirections
                    .actionProductRecommendationFragmentToProductDetailsFragment(product)
                findNavController().navigate(action)
            },
            onAddToShelfClick = { product ->
                viewModel.addToShelf(dbHelper, product)
                Toast.makeText(context, "Menambahkan ke Rak Skincare...", Toast.LENGTH_SHORT).show()
            }
        )

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
        // 1. Search Bar Listener
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.search(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 2. Filter Chip Listener
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