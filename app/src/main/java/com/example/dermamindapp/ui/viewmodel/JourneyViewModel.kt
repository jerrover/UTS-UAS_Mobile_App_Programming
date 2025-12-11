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
import java.text.SimpleDateFormat // Import ini penting
import java.util.Date             // Import ini penting
import java.util.Locale

class JourneyViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)

    // Data yang diobservasi oleh UI (Bisa berisi semua data, atau hasil filter)
    private val _analyses = MutableLiveData<List<SkinAnalysis>>()
    val analyses: LiveData<List<SkinAnalysis>> = _analyses

    // Variable untuk menyimpan Master Data (Semua data asli dari DB)
    private var allAnalysesList: List<SkinAnalysis> = emptyList()

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

                // Simpan ke Master Data & LiveData
                allAnalysesList = list
                _analyses.value = list

            } catch (e: Exception) {
                _analyses.value = emptyList()
                allAnalysesList = emptyList()
                _statusMessage.value = "Gagal memuat data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- FUNGSI FILTER KALENDER (SUDAH DIPERBAIKI) ---
    fun filterByDate(year: Int, month: Int, dayOfMonth: Int) {
        // 1. Format tanggal yang dipilih dari Kalender (Target)
        // Format: "yyyy-MM-dd" (contoh: 2023-10-25)
        val selectedDateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)

        // 2. Siapkan formatter untuk mengubah Timestamp (Long) ke String
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        // 3. Lakukan filtering
        val filteredList = allAnalysesList.filter { analysis ->
            // Ubah analysis.date (Long) menjadi Date, lalu format ke String
            val analysisDate = Date(analysis.date)
            val dateString = sdf.format(analysisDate)

            // Bandingkan apakah tanggalnya sama
            dateString == selectedDateStr
        }

        // Update LiveData agar UI berubah
        _analyses.value = filteredList
    }

    // --- FUNGSI RESET FILTER ---
    fun showAllData() {
        // Kembalikan data ke Master Data (Semua)
        _analyses.value = allAnalysesList
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

    // --- FUNGSI UPDATE SKINCARE ROUTINE ---
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