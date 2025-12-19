package com.example.dermamindapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dermamindapp.data.db.DatabaseHelper
import com.example.dermamindapp.data.model.Product
import kotlinx.coroutines.launch

class MyShelfViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application.applicationContext)

    private val _shelfProducts = MutableLiveData<List<Product>>()
    val shelfProducts: LiveData<List<Product>> = _shelfProducts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _deleteStatus = MutableLiveData<String?>()
    val deleteStatus: LiveData<String?> = _deleteStatus

    // Muat data saat ViewModel diinisialisasi
    init {
        loadShelf()
    }

    fun loadShelf() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val products = dbHelper.getMyShelfProducts()
                _shelfProducts.value = products
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat rak: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeProductFromShelf(product: Product) {
        viewModelScope.launch {
            try {
                dbHelper.removeFromShelf(product.id)
                _deleteStatus.value = "${product.name} dihapus dari rak"

                // Refresh data list secara lokal biar UI cepat update
                val currentList = _shelfProducts.value?.toMutableList() ?: mutableListOf()
                currentList.remove(product)
                _shelfProducts.value = currentList

                // ATAU panggil loadShelf() lagi untuk memastikan sinkronisasi server
                // loadShelf()
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menghapus: ${e.message}"
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _deleteStatus.value = null
    }
}