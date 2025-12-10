package com.example.dermamindapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dermamindapp.data.db.DatabaseHelper
import com.example.dermamindapp.data.model.Product
import com.example.dermamindapp.data.model.SkinAnalysis
import kotlinx.coroutines.launch

class JourneyViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)

    private val _analyses = MutableLiveData<List<SkinAnalysis>>()
    val analyses: LiveData<List<SkinAnalysis>> = _analyses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    // Variabel untuk menyimpan ID User saat ini
    private var currentUserId: String = ""

    // --- FUNGSI LOAD DATA ---
    fun loadAnalyses(userId: String) {
        this.currentUserId = userId

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Mengambil semua data analisis dari database
                val list = dbHelper.getAllAnalyses()
                _analyses.value = list
            } catch (e: Exception) {
                _analyses.value = emptyList()
                _statusMessage.value = "Gagal memuat data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- FUNGSI DELETE ---
    fun deleteAnalysis(id: String) {
        viewModelScope.launch {
            try {
                dbHelper.deleteAnalysis(id)
                _statusMessage.value = "Riwayat berhasil dihapus"

                // Refresh data setelah hapus agar list terupdate
                loadAnalyses(currentUserId)
            } catch (e: Exception) {
                _statusMessage.value = "Gagal menghapus: ${e.message}"
            }
        }
    }

    // --- FUNGSI UPDATE NOTES ---
    fun updateNotes(id: String, notes: String) {
        viewModelScope.launch {
            try {
                dbHelper.updateNotes(id, notes)
                _statusMessage.value = "Catatan berhasil diperbarui"

                // Refresh data setelah update
                loadAnalyses(currentUserId)
            } catch (e: Exception) {
                _statusMessage.value = "Gagal update: ${e.message}"
            }
        }
    }

    // --- FUNGSI BARU: UPDATE SKINCARE ROUTINE (INI YANG HILANG) ---
    fun updateSkincareRoutine(analysisId: String, products: List<Product>) {
        viewModelScope.launch {
            try {
                // Memanggil fungsi di DatabaseHelper
                dbHelper.updateAnalysisProducts(analysisId, products)

                _statusMessage.value = "Skincare routine berhasil disimpan!"

                // Refresh data agar UI sinkron
                loadAnalyses(currentUserId)
            } catch (e: Exception) {
                _statusMessage.value = "Gagal simpan routine: ${e.message}"
            }
        }
    }

    // --- UTILS ---
    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}