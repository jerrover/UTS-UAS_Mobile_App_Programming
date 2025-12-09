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

    private val TAG = "ANALYSIS_DEBUG"
    private val dbHelper = DatabaseHelper(application)

    private val _saveStatus = MutableLiveData<Boolean?>()
    val saveStatus: LiveData<Boolean?> = _saveStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    fun uploadAndSaveAnalysis(localUriString: String, result: String, userId: String) {
        Log.d(TAG, "Proses dimulai untuk User ID: $userId")
        _isLoading.value = true
        _statusMessage.value = "Mengupload foto..."

        val uri = Uri.parse(localUriString)

        MediaManager.get().upload(uri)
            .option("folder", "skin_journey")
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d(TAG, "Upload Cloudinary dimulai...")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val remoteUrl = resultData["secure_url"] as String
                    Log.d(TAG, "Upload SUKSES! URL: $remoteUrl")

                    val analysis = SkinAnalysis(
                        userId = userId,
                        date = System.currentTimeMillis(),
                        imageUri = remoteUrl,
                        result = result,
                        notes = ""
                    )

                    saveToFirestore(analysis)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e(TAG, "Upload GAGAL: ${error.description}")
                    _isLoading.postValue(false)
                    _statusMessage.postValue("Gagal upload gambar: ${error.description}")
                    _saveStatus.postValue(false)
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun saveToFirestore(analysis: SkinAnalysis) {
        Log.d(TAG, "Mencoba simpan data ke Database...")
        viewModelScope.launch {
            try {
                dbHelper.addAnalysis(analysis)
                Log.d(TAG, "Simpan Database BERHASIL!")
                _saveStatus.value = true
                _statusMessage.value = "Berhasil disimpan!"
            } catch (e: Exception) {
                Log.e(TAG, "Simpan Database GAGAL: ${e.message}")
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