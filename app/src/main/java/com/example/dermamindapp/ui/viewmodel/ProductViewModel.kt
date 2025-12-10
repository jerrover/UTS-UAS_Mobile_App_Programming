package com.example.dermamindapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dermamindapp.data.model.Product
import com.example.dermamindapp.data.repository.ProductRepository

class ProductViewModel : ViewModel() {

    private val repository = ProductRepository()

    // Data mentah (semua produk dari database)
    private var allProducts = listOf<Product>()

    // Data yang ditampilkan ke UI (sudah difilter)
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Menyimpan status filter saat ini
    var currentFilterSuitability: String = "Semua"
    var currentSearchQuery: String = ""

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        _isLoading.value = true
        repository.getProducts().observeForever { result ->
            _isLoading.value = false
            result.onSuccess { list ->
                allProducts = list
                // Setelah data diambil, terapkan filter awal (jika ada)
                applyFilter()
            }.onFailure { e ->
                _errorMessage.value = "Gagal memuat data: ${e.message}"
            }
        }
    }

    // Fungsi utama untuk memfilter data
    fun applyFilter() {
        var filteredList = allProducts

        // 1. Filter berdasarkan Kategori/Masalah (Chip)
        if (currentFilterSuitability != "Semua") {
            filteredList = filteredList.filter { product ->
                // Mengecek apakah text suitability produk mengandung kata kunci filter (case insensitive)
                product.suitability.contains(currentFilterSuitability, ignoreCase = true) ||
                        product.category.contains(currentFilterSuitability, ignoreCase = true)
            }
        }

        // 2. Filter berdasarkan Search Query (Nama Produk)
        if (currentSearchQuery.isNotEmpty()) {
            filteredList = filteredList.filter { product ->
                product.name.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        _products.value = filteredList
    }

    // Dipanggil saat user mengetik di search bar
    fun search(query: String) {
        currentSearchQuery = query
        applyFilter()
    }

    // Dipanggil saat user klik Chip
    fun filterBySuitability(suitability: String) {
        currentFilterSuitability = suitability
        applyFilter()
    }
}