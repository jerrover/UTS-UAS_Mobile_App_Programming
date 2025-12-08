package com.example.dermamindapp.ui.viewmodel

import android.app.Application
import android.util.Log // Import Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dermamindapp.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    private val _saveStatus = MutableLiveData<Boolean?>()
    val saveStatus: LiveData<Boolean?> = _saveStatus

    fun saveUserProfile(user: User) {
        viewModelScope.launch {
            try {
                Log.d("FIREBASE_DEBUG", "Mencoba menyimpan user: ${user.name}...")

                val docRef = usersCollection.document()
                val finalUser = user.copy(id = docRef.id)

                // Proses kirim data
                docRef.set(finalUser).await()

                Log.d("FIREBASE_DEBUG", "BERHASIL! Data user tersimpan dengan ID: ${docRef.id}")
                _saveStatus.value = true
            } catch (e: Exception) {
                // INI YANG PENTING: Kita cetak errornya apa
                Log.e("FIREBASE_DEBUG", "GAGAL SIMPAN! Error: ${e.message}", e)
                _saveStatus.value = false
            }
        }
    }
}