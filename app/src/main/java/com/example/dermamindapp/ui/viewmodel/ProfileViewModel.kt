package com.example.dermamindapp.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
// Import Cloudinary (Pastikan library sudah ada di build.gradle)
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val prefsHelper = PreferencesHelper(application)

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    // --- LOAD PROFILE ---
    fun loadProfile() {
        val storedUsername = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)
        if (storedUsername.isNullOrEmpty()) {
            _statusMessage.value = "User ID tidak ditemukan di HP."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val document = usersCollection.document(storedUsername).get().await()
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    _userProfile.value = user
                } else {
                    _statusMessage.value = "Profil tidak ditemukan."
                }
            } catch (e: Exception) {
                _statusMessage.value = "Gagal memuat profil: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- UPLOAD FOTO (VERSI CLOUDINARY) ---
    fun uploadProfilePicture(imageUri: Uri) {
        val currentUser = _userProfile.value
        // Ambil username sebagai ID unik
        val username = currentUser?.id ?: prefsHelper.getString(PreferencesHelper.KEY_USER_ID)

        if (username.isNullOrEmpty()) {
            _statusMessage.value = "Gagal: User ID tidak ditemukan."
            return
        }

        _isLoading.value = true
        _statusMessage.value = "Mengupload ke Cloudinary..."

        // 1. Upload ke Cloudinary
        // Menggunakan "public_id" yang sama agar foto lama otomatis tertimpa (Hemat storage!)
        MediaManager.get().upload(imageUri)
            .option("public_id", "profile_$username")
            .option("folder", "dermamind_profiles") // Folder di Cloudinary
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Upload dimulai
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Bisa update progress bar disini jika mau
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    // 2. SUKSES! Ambil URL HTTPS yang benar
                    val secureUrl = resultData["secure_url"] as String
                    Log.d("Cloudinary", "Upload Sukses: $secureUrl")

                    // 3. Simpan URL tersebut ke Firestore
                    savePhotoUrlToDatabase(username, secureUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    _isLoading.postValue(false)
                    _statusMessage.postValue("Gagal upload: ${error.description}")
                    Log.e("Cloudinary", "Error: ${error.description}")
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun savePhotoUrlToDatabase(username: String, url: String) {
        viewModelScope.launch {
            try {
                // Update kolom 'photoUrl' di Firestore
                usersCollection.document(username).update("photoUrl", url).await()

                // Update tampilan di aplikasi secara langsung (biar user lihat perubahannya)
                val updatedUser = _userProfile.value?.copy(photoUrl = url)
                    ?: User(id = username, photoUrl = url)

                _userProfile.value = updatedUser
                _statusMessage.value = "Foto profil berhasil diperbarui!"

            } catch (e: Exception) {
                _statusMessage.value = "Gagal simpan URL ke database: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- UPDATE DATA LAINNYA ---
    fun updateProfileData(updatedUser: User) {
        val storedUsername = prefsHelper.getString(PreferencesHelper.KEY_USER_ID) ?: updatedUser.id
        if (storedUsername.isEmpty()) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val finalUser = updatedUser.copy(id = storedUsername)
                usersCollection.document(storedUsername).set(finalUser).await()

                prefsHelper.saveString(PreferencesHelper.KEY_USER_NAME, finalUser.name)
                prefsHelper.saveString(PreferencesHelper.KEY_USER_AGE, finalUser.age)
                prefsHelper.saveString(PreferencesHelper.KEY_SKIN_TYPE, finalUser.skinType)
                prefsHelper.saveString(PreferencesHelper.KEY_PREFERENCES, finalUser.preferences)
                prefsHelper.saveString(PreferencesHelper.KEY_ROUTINES, finalUser.routines)

                _statusMessage.value = "Profil berhasil diperbarui!"
                _userProfile.value = finalUser
            } catch (e: Exception) {
                _statusMessage.value = "Gagal update: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- LOGOUT & DELETE ---
    fun logout() {
        auth.signOut()
        prefsHelper.clear()
        _navigateToLogin.value = true
    }

    fun deleteAccount() {
        val storedUsername = prefsHelper.getString(PreferencesHelper.KEY_USER_ID) ?: return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                usersCollection.document(storedUsername).delete().await()
                auth.currentUser?.delete()?.await()
                prefsHelper.clear()
                _statusMessage.value = "Akun dihapus."
                _navigateToLogin.value = true
            } catch (e: Exception) {
                _statusMessage.value = "Gagal hapus akun."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearStatus() { _statusMessage.value = null }
    fun resetNavigate() { _navigateToLogin.value = false }
}