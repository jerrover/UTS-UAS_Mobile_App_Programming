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
import com.example.dermamindapp.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)
    private val auth = FirebaseAuth.getInstance()

    // Data Profil User
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    // Status Pesan (untuk Toast di UI)
    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    // Status Loading (untuk ProgressBar saat upload)
    private val _isUploading = MutableLiveData<Boolean>()
    val isUploading: LiveData<Boolean> = _isUploading

    // Navigasi ke Login (jika logout)
    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    // --- 1. LOAD DATA PROFIL ---
    fun loadProfile() {
        viewModelScope.launch {
            val profile = dbHelper.getUserProfile()
            _userProfile.postValue(profile)
        }
    }

    // --- 2. UPDATE DATA TEKS (Nama, Umur, dll) ---
    fun updateProfileData(updatedProfile: UserProfile) {
        viewModelScope.launch {
            try {
                dbHelper.saveUserProfile(updatedProfile)
                loadProfile() // Refresh data setelah simpan
                _statusMessage.value = "Profil berhasil diperbarui"
            } catch (e: Exception) {
                _statusMessage.value = "Gagal update profil: ${e.message}"
            }
        }
    }

    // --- 3. FITUR BARU: GANTI FOTO PROFIL (UPLOAD) ---
    fun updateProfilePicture(localUri: Uri) {
        val currentUser = _userProfile.value
        // Pastikan kita punya User ID. Jika profile belum load, ambil dari Auth langsung.
        val userId = currentUser?.id?.ifEmpty { auth.currentUser?.uid } ?: return

        _isUploading.value = true
        _statusMessage.value = "Mengupload foto..."

        // STRATEGI PENTING: Gunakan User ID sebagai nama file (public_id).
        // Cloudinary akan OTOMATIS menimpa file lama dengan nama yang sama.
        // Jadi kita tidak perlu repot menghapus file lama manual.
        val uniquePublicId = "profile_$userId"

        MediaManager.get().upload(localUri)
            .option("public_id", uniquePublicId)
            .option("folder", "user_profiles") // Folder di Cloudinary
            .option("overwrite", true)         // Paksa timpa file lama
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val remoteUrl = resultData["secure_url"] as String
                    Log.d("Cloudinary", "Upload Sukses: $remoteUrl")

                    // Simpan URL baru ke Database
                    savePhotoUrlToDb(remoteUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    _isUploading.postValue(false)
                    _statusMessage.postValue("Gagal upload: ${error.description}")
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    // --- 4. FITUR BARU: HAPUS FOTO PROFIL ---
    fun deleteProfilePicture() {
        _isUploading.value = true
        // Kita kirim null untuk menghapus link foto di database
        savePhotoUrlToDb(null)
    }

    // Helper Private untuk update DB setelah aksi foto
    private fun savePhotoUrlToDb(url: String?) {
        viewModelScope.launch {
            try {
                dbHelper.updateUserPhoto(url)

                val msg = if (url == null) "Foto profil dihapus" else "Foto profil diperbarui"
                _statusMessage.postValue(msg)

                // Reload agar UI berubah (gambar hilang/ganti)
                loadProfile()
            } catch (e: Exception) {
                _statusMessage.postValue("Gagal update database")
            } finally {
                _isUploading.postValue(false)
            }
        }
    }

    // --- LOGOUT & LAINNYA ---
    fun logout() {
        auth.signOut()
        _navigateToLogin.value = true
    }

    fun resetNavigate() { _navigateToLogin.value = false }
    fun clearStatus() { _statusMessage.value = null }

    fun deleteAccount() {
        // Logika hapus akun (bisa ditambahkan nanti)
        _statusMessage.value = "Fitur hapus akun akan segera hadir"
    }
}