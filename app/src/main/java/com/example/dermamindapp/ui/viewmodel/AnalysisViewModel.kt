package com.example.dermamindapp.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.dermamindapp.data.db.DatabaseHelper
import com.example.dermamindapp.data.model.SkinAnalysis
import kotlinx.coroutines.launch

class AnalysisViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)

    // Status: Loading, Sukses, Gagal
    private val _saveStatus = MutableLiveData<Boolean?>()
    val saveStatus: LiveData<Boolean?> = _saveStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    // Fungsi BARU: Upload gambar dulu, baru simpan data
    fun uploadAndSaveAnalysis(localUriString: String, result: String, userId: String) {
        _isLoading.value = true
        _statusMessage.value = "Mengupload foto..."

        val uri = Uri.parse(localUriString)

        MediaManager.get().upload(uri)
            .option("folder", "skin_journey") // Simpan di folder khusus history
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    // 1. Upload Berhasil, Ambil URL Publik
                    val remoteUrl = resultData["secure_url"] as String
                    Log.d("Cloudinary", "Upload Sukses: $remoteUrl")

                    // 2. Buat Objek Data dengan URL Cloudinary
                    val analysis = SkinAnalysis(
                        userId = userId,
                        date = System.currentTimeMillis(),
                        imageUri = remoteUrl, // Simpan URL Cloud, bukan lokal!
                        result = result,
                        notes = ""
                    )

                    // 3. Simpan ke Firestore Database
                    saveToFirestore(analysis)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    _isLoading.postValue(false)
                    _statusMessage.postValue("Gagal upload gambar: ${error.description}")
                    _saveStatus.postValue(false)
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun saveToFirestore(analysis: SkinAnalysis) {
        viewModelScope.launch {
            try {
                dbHelper.addAnalysis(analysis)
                _saveStatus.value = true // Sukses total
                _statusMessage.value = "Berhasil disimpan!"
            } catch (e: Exception) {
                _saveStatus.value = false
                _statusMessage.value = "Gagal simpan database: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSaveStatus() {
        _saveStatus.value = null
        _statusMessage.value = null
    }
}