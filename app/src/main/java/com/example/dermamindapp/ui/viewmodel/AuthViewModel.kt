package com.example.dermamindapp.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.data.model.User
// Import Firebase harus lengkap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // Inisialisasi Firebase Auth & Firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val prefsHelper = PreferencesHelper(application)

    // Domain palsu agar Firebase Auth mau terima username sebagai email
    private val DUMMY_DOMAIN = "@dermamind.local"

    // --- STATES (Kondisi UI) ---
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    // Khusus Register: Hasil cek username
    private val _usernameStatus = MutableLiveData<UsernameStatus>()
    val usernameStatus: LiveData<UsernameStatus> = _usernameStatus

    // Enum untuk status username
    sealed class UsernameStatus {
        object Available : UsernameStatus() // Tersedia
        data class Taken(val suggestions: List<String>) : UsernameStatus() // Terpakai
        object Checking : UsernameStatus() // Lagi loading cek
    }

    // =================================================================================
    // 1. FITUR LOGIN (USERNAME & PASSWORD)
    // =================================================================================
    fun login(username: String, pass: String) {
        if (username.isEmpty() || pass.isEmpty()) {
            _errorMessage.value = "Username dan Password wajib diisi!"
            return
        }

        _isLoading.value = true
        val emailFormat = username.trim().lowercase() + DUMMY_DOMAIN

        auth.signInWithEmailAndPassword(emailFormat, pass)
            .addOnSuccessListener { result ->
                // result adalah AuthResult
                val user: FirebaseUser? = result.user
                val uid = user?.uid

                if (uid != null) {
                    saveSessionAndFetchProfile(uid, username)
                } else {
                    _isLoading.value = false
                    _errorMessage.value = "Gagal mendapatkan User ID."
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                // Handle pesan error
                val msg = e.message ?: "Terjadi kesalahan"
                if (msg.contains("There is no user") || msg.contains("record")) {
                    _errorMessage.value = "Username tidak ditemukan."
                } else if (msg.contains("password")) {
                    _errorMessage.value = "Password salah."
                } else {
                    _errorMessage.value = "Login Gagal: $msg"
                }
            }
    }

    // =================================================================================
    // 2. FITUR CEK KETERSEDIAAN USERNAME
    // =================================================================================
    fun checkUsernameAvailability(rawUsername: String) {
        val username = rawUsername.trim().lowercase()
        if (username.length < 3) return

        _usernameStatus.value = UsernameStatus.Checking

        viewModelScope.launch {
            try {
                // Cek di Firestore apakah ada dokumen dengan ID = username
                val docSnapshot = usersCollection.document(username).get().await()

                if (!docSnapshot.exists()) {
                    _usernameStatus.value = UsernameStatus.Available
                } else {
                    val suggestions = generateSuggestions(username)
                    _usernameStatus.value = UsernameStatus.Taken(suggestions)
                }
            } catch (e: Exception) {
                Log.e("AuthCheck", "Error checking username", e)
            }
        }
    }

    private fun generateSuggestions(baseName: String): List<String> {
        val list = mutableListOf<String>()
        list.add("${baseName}${Random.nextInt(10, 99)}")
        list.add("${baseName}01")
        list.add("iam${baseName}")
        return list
    }

    // =================================================================================
    // 3. FITUR REGISTER BARU
    // =================================================================================
    fun register(username: String, pass: String) {
        val finalUsername = username.trim().lowercase()
        val emailFormat = finalUsername + DUMMY_DOMAIN

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 1. Double Check Username
                val checkDoc = usersCollection.document(finalUsername).get().await()
                if (checkDoc.exists()) {
                    _errorMessage.value = "Yah, username '$finalUsername' baru saja diambil orang lain!"
                    _isLoading.value = false
                    return@launch
                }

                // 2. Buat Akun di Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(emailFormat, pass).await()
                val uid = authResult.user?.uid ?: throw Exception("Gagal create user auth")

                // 3. Simpan data user dasar ke Firestore (ID Dokumen = Username)
                val newUser = User(
                    id = finalUsername, // PENTING: ID User di data adalah Username
                    name = finalUsername,
                    age = "",
                    skinType = "",
                    photoUrl = ""
                )

                usersCollection.document(finalUsername).set(newUser).await()

                // 4. Sukses
                saveSessionAndFetchProfile(uid, finalUsername)

            } catch (e: Exception) {
                _isLoading.value = false
                val msg = e.message ?: "Gagal daftar"
                _errorMessage.value = "Registrasi Gagal: $msg"
            }
        }
    }

    private fun saveSessionAndFetchProfile(uid: String, username: String) {
        viewModelScope.launch {
            try {
                // Simpan sesi ke HP
                // Kita simpan Username sebagai KEY_USER_ID karena itu ID dokumen kita
                prefsHelper.saveString(PreferencesHelper.KEY_USER_ID, username)
                prefsHelper.saveString(PreferencesHelper.KEY_USER_NAME, username)

                // Tandai sudah login
                prefsHelper.saveBoolean(PreferencesHelper.KEY_ONBOARDING_COMPLETED, true)

                _isLoading.value = false
                _loginSuccess.value = true
            } catch (e: Exception) {
                _isLoading.value = false
                _loginSuccess.value = true
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}