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

    // Status pesan untuk memberi tahu UI (misal: "Sukses update", "Gagal hapus")
    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    fun loadAnalyses() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
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

    fun deleteAnalysis(id: String) {
        viewModelScope.launch {
            try {
                dbHelper.deleteAnalysis(id)
                _statusMessage.value = "Riwayat berhasil dihapus"
                loadAnalyses() // Reload data
            } catch (e: Exception) {
                _statusMessage.value = "Gagal menghapus: ${e.message}"
            }
        }
    }

    // --- FUNGSI BARU ---
    fun updateNotes(id: String, notes: String) {
        viewModelScope.launch {
            try {
                dbHelper.updateNotes(id, notes)
                _statusMessage.value = "Catatan berhasil diperbarui"
                loadAnalyses() // Reload data biar sinkron
            } catch (e: Exception) {
                _statusMessage.value = "Gagal update: ${e.message}"
            }
        }
    }

    // Reset pesan setelah ditampilkan agar tidak muncul berulang
    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}