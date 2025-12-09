package com.example.dermamindapp.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val prefsHelper = PreferencesHelper(application)

    // [BARU] LiveData untuk User Profile
    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    // [BARU] LiveData untuk Status Loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // [BARU] LiveData untuk Pesan Status (Toast)
    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    private val _saveStatus = MutableLiveData<Boolean?>()
    val saveStatus: LiveData<Boolean?> = _saveStatus

    private val _createdUserId = MutableLiveData<String?>()
    val createdUserId: LiveData<String?> = _createdUserId

    // [BARU] Fungsi Load Profile
    fun loadProfile() {
        val userId = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)
        if (userId.isNullOrEmpty()) {
            _statusMessage.value = "User ID tidak ditemukan. Silakan login ulang."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val document = usersCollection.document(userId).get().await()
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    _userProfile.value = user
                } else {
                    _statusMessage.value = "Profil tidak ditemukan di server."
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Gagal load profile", e)
                _statusMessage.value = "Gagal memuat profil: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // [BARU] Fungsi Upload Profile Picture (Placeholder)
    // Catatan: Idealnya menggunakan Firebase Storage. Ini implementasi dasar update URL di Firestore.
    fun uploadProfilePicture(uri: Uri) {
        val userId = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)
        if (userId.isNullOrEmpty()) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // TODO: Di sini idealnya upload foto ke Firebase Storage, dapatkan URL-nya.
                // Untuk sekarang, kita anggap URI lokal sebagai string (ini hanya akan bekerja lokal sementara)
                // Atau simpan string URI ke field photoUrl di Firestore

                val photoUriString = uri.toString()

                // Update field photoUrl di Firestore
                usersCollection.document(userId)
                    .update("photoUrl", photoUriString)
                    .await()

                _statusMessage.value = "Foto profil diperbarui!"

                // Refresh data user lokal
                val currentUser = _userProfile.value
                if (currentUser != null) {
                    _userProfile.value = currentUser.copy(photoUrl = photoUriString)
                }

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Gagal update foto", e)
                _statusMessage.value = "Gagal mengupdate foto."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // [BARU] Membersihkan pesan status setelah ditampilkan
    fun clearStatus() {
        _statusMessage.value = null
    }

    fun saveUserProfile(user: User) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val docRef = usersCollection.document()
                val finalUser = user.copy(id = docRef.id)

                docRef.set(finalUser).await()

                // Simpan ID ke Prefs agar sesi terjaga
                prefsHelper.saveString(PreferencesHelper.KEY_USER_ID, docRef.id)
                prefsHelper.saveString(PreferencesHelper.KEY_USER_NAME, finalUser.name)

                _createdUserId.value = docRef.id
                _saveStatus.value = true
            } catch (e: Exception) {
                Log.e("FIREBASE_DEBUG", "GAGAL SIMPAN! Error: ${e.message}", e)
                _saveStatus.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun updateProfileData(updatedUser: User) {
        val userId = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)
        if (userId.isNullOrEmpty()) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Update data di Firestore
                usersCollection.document(userId)
                    .set(updatedUser) // set() akan menimpa/menggabungkan data jika ID sama
                    .await()

                // Simpan nama baru ke SharedPreferences juga agar konsisten
                prefsHelper.saveString(PreferencesHelper.KEY_USER_NAME, updatedUser.name)

                _statusMessage.value = "Profil berhasil diperbarui!"

                // Update LiveData agar UI langsung berubah tanpa perlu reload
                _userProfile.value = updatedUser

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Gagal update profil", e)
                _statusMessage.value = "Gagal memperbarui profil."
            } finally {
                _isLoading.value = false
            }
        }
    }
}