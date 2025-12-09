package com.example.dermamindapp.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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

    fun loadProfile() {
        // PERBAIKAN DISINI:
        // Prioritaskan ambil ID dari Username yang tersimpan di HP (karena Register pakai Username)
        // Jangan pakai auth.currentUser.uid dulu karena itu beda dengan Username
        val storedUsername = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)

        if (storedUsername.isNullOrEmpty()) {
            _statusMessage.value = "User ID (Username) tidak ditemukan di HP."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Cari dokumen yang ID-nya adalah Username
                val document = usersCollection.document(storedUsername).get().await()

                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    _userProfile.value = user
                } else {
                    _statusMessage.value = "Profil '$storedUsername' tidak ditemukan di server."
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Gagal load profile", e)
                _statusMessage.value = "Gagal memuat profil: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        try {
            auth.signOut()
            prefsHelper.clear()
            _navigateToLogin.value = true
        } catch (e: Exception) {
            _statusMessage.value = "Gagal logout: ${e.message}"
        }
    }

    fun deleteAccount() {
        // Ambil ID dokumen (Username)
        val storedUsername = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)
        val userAuth = auth.currentUser

        if (storedUsername == null) {
            _statusMessage.value = "Gagal: Data user tidak ditemukan."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 1. Hapus data di Firestore (Pakai Username)
                usersCollection.document(storedUsername).delete().await()

                // 2. Hapus User Login (Auth)
                userAuth?.delete()?.await()

                // 3. Bersihkan Lokal
                prefsHelper.clear()

                _statusMessage.value = "Akun berhasil dihapus."
                _navigateToLogin.value = true

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Gagal hapus akun", e)
                _statusMessage.value = "Gagal hapus akun. Coba login ulang dulu."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfileData(updatedUser: User) {
        // Pastikan ID dokumennya adalah Username yang benar
        val storedUsername = prefsHelper.getString(PreferencesHelper.KEY_USER_ID) ?: updatedUser.id

        if (storedUsername.isEmpty()) {
            _statusMessage.value = "Error: Username hilang."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Pastikan user object punya ID yang benar sebelum dikirim
                val finalUser = updatedUser.copy(id = storedUsername)

                usersCollection.document(storedUsername)
                    .set(finalUser)
                    .await()

                prefsHelper.saveString(PreferencesHelper.KEY_USER_NAME, finalUser.name)
                prefsHelper.saveString(PreferencesHelper.KEY_USER_AGE, finalUser.age)
                prefsHelper.saveString(PreferencesHelper.KEY_SKIN_TYPE, finalUser.skinType)
                prefsHelper.saveString(PreferencesHelper.KEY_PREFERENCES, finalUser.preferences)
                prefsHelper.saveString(PreferencesHelper.KEY_ROUTINES, finalUser.routines)

                _statusMessage.value = "Profil berhasil diperbarui!"
                _userProfile.value = finalUser

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Gagal update profil", e)
                _statusMessage.value = "Gagal memperbarui profil."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }

    fun resetNavigate() {
        _navigateToLogin.value = false
    }
}