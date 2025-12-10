package com.example.dermamindapp.data.db

import android.content.Context
import android.util.Log
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.data.model.Product
import com.example.dermamindapp.data.model.SkinAnalysis
import com.example.dermamindapp.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class DatabaseHelper(context: Context) {

    private val TAG = "DB_HELPER_DEBUG"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val prefsHelper = PreferencesHelper(context)

    // --- FUNGSI PENTING: Selalu cek ID terbaru (Dinamis) ---
    private fun getCurrentUserId(): String {
        // 1. Prioritas Utama: Username yang disimpan di HP saat Login
        val storedId = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)

        if (!storedId.isNullOrEmpty()) {
            Log.d(TAG, "Menggunakan ID dari HP (Username): $storedId")
            return storedId
        }

        // 2. Fallback: Cek Firebase Auth (kalau ada)
        val authUser = auth.currentUser
        if (authUser != null) {
            Log.d(TAG, "Menggunakan UID Firebase: ${authUser.uid}")
            return authUser.uid
        }

        Log.e(TAG, "GAWAT! ID User Kosong. Data tidak akan tersimpan.")
        return ""
    }

    // --- FITUR 1: RIWAYAT ANALISIS ---

    suspend fun addAnalysis(analysis: SkinAnalysis) {
        val uid = getCurrentUserId()

        if (uid.isEmpty()) {
            throw Exception("Gagal: User belum login.")
        }

        try {
            Log.d(TAG, "Menyimpan data untuk User: $uid")

            // Set pemilik data
            analysis.userId = uid

            // Path: users/{username}/skin_analyses
            // JANGAN pakai variabel global, buat path baru setiap kali simpan
            val docRef = firestore.collection("users").document(uid).collection("skin_analyses").document()

            analysis.id = docRef.id
            docRef.set(analysis).await()

            Log.d(TAG, "SUKSES SIMPAN! Cek di Firestore: users/$uid/skin_analyses/${docRef.id}")

        } catch (e: Exception) {
            Log.e(TAG, "Error Simpan: ${e.message}")
            throw e
        }
    }

    suspend fun getAllAnalyses(): List<SkinAnalysis> {
        val uid = getCurrentUserId()
        if (uid.isEmpty()) return emptyList()

        return try {
            Log.d(TAG, "Mengambil data dari: users/$uid/skin_analyses")
            val snapshot = firestore.collection("users").document(uid)
                .collection("skin_analyses")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            Log.d(TAG, "Jumlah data ditemukan: ${snapshot.size()}")
            snapshot.toObjects(SkinAnalysis::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error Load: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteAnalysis(analysisId: String) {
        val uid = getCurrentUserId()
        if (uid.isEmpty()) return

        try {
            firestore.collection("users").document(uid)
                .collection("skin_analyses")
                .document(analysisId)
                .delete()
                .await()
            Log.d(TAG, "Data $analysisId berhasil dihapus")
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateNotes(analysisId: String, notes: String) {
        val uid = getCurrentUserId()
        if (uid.isEmpty()) return

        try {
            firestore.collection("users").document(uid)
                .collection("skin_analyses")
                .document(analysisId)
                .update("notes", notes)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    // --- FITUR 2: USER PROFILE ---

    suspend fun saveUserProfile(userProfile: UserProfile) {
        val uid = getCurrentUserId()
        if (uid.isEmpty()) return

        try {
            val fixedProfile = userProfile.copy(id = uid)
            firestore.collection("users").document(uid).set(fixedProfile).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getUserProfile(): UserProfile? {
        val uid = getCurrentUserId()
        if (uid.isEmpty()) return null

        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserPhoto(photoUrl: String?) {
        val uid = getCurrentUserId()
        if (uid.isEmpty()) return

        try {
            // Jika photoUrl null (dihapus), kita simpan string kosong ""
            // agar field di database tidak hilang, cuma isinya kosong
            val urlToSave = photoUrl ?: ""

            firestore.collection("users").document(uid)
                .update("photoUrl", urlToSave) // Update hanya field 'photoUrl'
                .await()

            Log.d(TAG, "Foto profil berhasil diupdate di Database: $urlToSave")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal update foto di Database: ${e.message}")
            throw e
        }
    }

    // --- FITUR UPDATE SKINCARE ROUTINE ---
    suspend fun updateAnalysisProducts(analysisId: String, products: List<Product>) {
        val uid = getCurrentUserId()
        if (uid.isEmpty()) return

        try {
            firestore.collection("users").document(uid)
                .collection("skin_analyses")
                .document(analysisId)
                .update("usedProducts", products) // Firestore otomatis meng-handle List object
                .await()
            Log.d(TAG, "Skincare routine berhasil diupdate untuk analisis: $analysisId")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal update skincare routine: ${e.message}")
            throw e
        }
    }
}