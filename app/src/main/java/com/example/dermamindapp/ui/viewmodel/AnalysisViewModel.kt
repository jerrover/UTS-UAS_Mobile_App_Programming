package com.example.dermamindapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dermamindapp.data.db.DatabaseHelper
import com.example.dermamindapp.data.model.SkinAnalysis
import kotlinx.coroutines.launch

class AnalysisViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)

    // LiveData untuk memantau status simpan (Sukses/Gagal)
    private val _saveStatus = MutableLiveData<Boolean?>()
    val saveStatus: LiveData<Boolean?> = _saveStatus

    // Fungsi untuk menyimpan hasil analisis ke Firebase
    fun saveAnalysis(analysis: SkinAnalysis) {
        viewModelScope.launch {
            try {
                dbHelper.addAnalysis(analysis)
                _saveStatus.value = true // Berhasil
            } catch (e: Exception) {
                _saveStatus.value = false // Gagal
            }
        }
    }

    // Reset status setelah navigasi selesai (biar ga kepanggil 2x)
    fun resetSaveStatus() {
        _saveStatus.value = null
    }
}