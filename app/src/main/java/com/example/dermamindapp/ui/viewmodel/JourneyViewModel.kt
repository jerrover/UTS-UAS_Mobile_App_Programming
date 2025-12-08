package com.example.dermamindapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dermamindapp.data.db.DatabaseHelper
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

    // PERBAIKAN: Variabel untuk menyimpan ID User saat ini
    private var currentUserId: String = ""

    // Fungsi Load Data (Sekarang menyimpan ID-nya)
    fun loadAnalyses(userId: String) {
        this.currentUserId = userId // Simpan ID ke variabel

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Ambil data sesuai ID
                val list = dbHelper.getAllAnalyses(userId)
                _analyses.value = list
            } catch (e: Exception) {
                _analyses.value = emptyList()
                _statusMessage.value = "Gagal memuat data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAnalysis(id: String) {
        viewModelScope.launch {
            try {
                dbHelper.deleteAnalysis(id)
                _statusMessage.value = "Riwayat berhasil dihapus"

                // PERBAIKAN: Panggil loadAnalyses menggunakan ID yang sudah disimpan
                loadAnalyses(currentUserId)
            } catch (e: Exception) {
                _statusMessage.value = "Gagal menghapus: ${e.message}"
            }
        }
    }

    fun updateNotes(id: String, notes: String) {
        viewModelScope.launch {
            try {
                dbHelper.updateNotes(id, notes)
                _statusMessage.value = "Catatan berhasil diperbarui"

                // PERBAIKAN: Panggil loadAnalyses menggunakan ID yang sudah disimpan
                loadAnalyses(currentUserId)
            } catch (e: Exception) {
                _statusMessage.value = "Gagal update: ${e.message}"
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}