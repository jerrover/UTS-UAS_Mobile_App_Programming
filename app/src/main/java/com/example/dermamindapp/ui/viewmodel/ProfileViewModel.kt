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
import com.google.firebase.firestore.FirebaseFirestore // Import Firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Import await untuk operasi suspend

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)
    private val auth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance() // Inisialisasi Firestore

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
                // Optimasi pesan error
                Log.e("ProfileVM", "Gagal update profil", e)
                _statusMessage.value = "Gagal memperbarui profil. Mohon coba lagi."
            }
        }
    }

    // --- 3. GANTI FOTO PROFIL (UPLOAD) ---
    fun updateProfilePicture(localUri: Uri) {
        val currentUser = _userProfile.value
        // Pastikan kita punya User ID. Jika profile belum load, ambil dari Auth langsung.
        val userId = currentUser?.id?.ifEmpty { auth.currentUser?.uid } ?: run {
            _statusMessage.value = "Gagal upload: Sesi tidak valid."
            return
        }

        _isUploading.value = true
        _statusMessage.value = "Mengupload foto..."

        val uniquePublicId = "profile_$userId"

        MediaManager.get().upload(localUri)
            .option("public_id", uniquePublicId)
            .option("folder", "user_profiles")
            .option("overwrite", true)
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val remoteUrl = resultData["secure_url"] as String
                    Log.d("Cloudinary", "Upload Sukses: $remoteUrl")
                    savePhotoUrlToDb(remoteUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    _isUploading.postValue(false)
                    // Optimasi pesan error
                    _statusMessage.postValue("Gagal upload foto. Mohon periksa koneksi internet Anda.")
                    Log.e("Cloudinary", "Upload Gagal: ${error.description}")
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    // --- 4. HAPUS FOTO PROFIL ---
    fun deleteProfilePicture() {
        _isUploading.value = true
        savePhotoUrlToDb(null)
    }

    // Helper Private untuk update DB setelah aksi foto
    private fun savePhotoUrlToDb(url: String?) {
        viewModelScope.launch {
            try {
                dbHelper.updateUserPhoto(url)

                val msg = if (url == null) "Foto profil dihapus" else "Foto profil diperbarui"
                _statusMessage.postValue(msg)

                loadProfile()
            } catch (e: Exception) {
                Log.e("ProfileVM", "Gagal update database photo", e)
                _statusMessage.postValue("Gagal update database")
            } finally {
                _isUploading.postValue(false)
            }
        }
    }

    // --- 5. LOGOUT & LAINNYA ---
    fun logout() {
        auth.signOut()
        // dbHelper.clearAllPreferences() // Tambahkan ini jika ada di DatabaseHelper
        _navigateToLogin.value = true
    }

    // --- 6. IMPLEMENTASI HAPUS AKUN ---
    fun deleteAccount() {
        val firebaseUser = auth.currentUser
        // ID Dokumen di Firestore adalah Username/ID User
        val userId = _userProfile.value?.id

        if (firebaseUser == null || userId.isNullOrEmpty()) {
            _statusMessage.value = "Hapus Akun Gagal. Sesi tidak valid, mohon login ulang."
            return
        }

        _isUploading.value = true
        _statusMessage.value = "Menghapus akun..."

        viewModelScope.launch {
            try {
                // 1. Hapus data user dari Firestore (Dokumen user)
                // Asumsi: 'users' adalah collection yang menyimpan profil user (sesuai AuthViewModel)
                db.collection("users").document(userId).delete().await()
                Log.d("ProfileVM", "User data $userId deleted from Firestore.")

                // 2. Hapus user dari Firebase Auth
                // Catatan: Ini bisa gagal jika otentikasi terakhir sudah lama
                firebaseUser.delete().await()
                Log.d("ProfileVM", "Firebase Auth user deleted.")

                // 3. Clear local session (logout)
                auth.signOut()
                // dbHelper.clearAllPreferences() // Tambahkan ini jika ada di DatabaseHelper

                _isUploading.value = false
                _statusMessage.value = "Akun berhasil dihapus. Sampai jumpa!"
                _navigateToLogin.value = true

            } catch (e: Exception) {
                Log.e("ProfileVM", "Failed to delete account.", e)
                _isUploading.value = false

                // Penanganan error khusus Re-authentication
                val msg = e.message ?: "Terjadi kesalahan yang tidak diketahui."
                if (msg.contains("requires recent authentication")) {
                    _statusMessage.value = "Hapus Akun Gagal. Mohon logout dan login kembali untuk menghapus akun Anda."
                } else {
                    // Pesan umum untuk user
                    _statusMessage.value = "Hapus Akun Gagal. Terjadi kesalahan saat menghapus data."
                }
            }
        }
    }

    fun resetNavigate() { _navigateToLogin.value = false }
    fun clearStatus() { _statusMessage.value = null }
}