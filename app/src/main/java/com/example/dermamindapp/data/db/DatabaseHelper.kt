// File: src/main/java/com/example/dermamindapp/data/db/DatabaseHelper.kt
package com.example.dermamindapp.data.db

import android.content.Context
import android.util.Log
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.data.model.SkinAnalysis
import com.example.dermamindapp.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DatabaseHelper(context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val prefsHelper = PreferencesHelper(context)
    private val userId: String

    init {
        // 1. Cek apakah ID sudah ada dengan Key yang benar
        var id = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)

        // 2. Jika belum ada (User Baru), buat ID dan simpan
        if (id.isNullOrEmpty()) {
            id = UUID.randomUUID().toString()
            prefsHelper.saveString(PreferencesHelper.KEY_USER_ID, id)
            Log.d("DatabaseHelper", "New User ID Generated: $id")
        }

        // 3. Set variabel global userId
        userId = id!!
    }

    // References Path Firestore
    private val analysisCollection = firestore.collection("users").document(userId).collection("skin_analyses")
    private val userDocRef = firestore.collection("users").document(userId)

    // --- FITUR 1: RIWAYAT ANALISIS ---

    suspend fun addAnalysis(analysis: SkinAnalysis) {
        try {
            analysis.userId = userId
            val docRef = analysisCollection.document()
            analysis.id = docRef.id // Generate ID dokumen Firebase
            docRef.set(analysis).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getAllAnalyses(): List<SkinAnalysis> {
        return try {
            val snapshot = analysisCollection
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(SkinAnalysis::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteAnalysis(id: String) {
        try {
            analysisCollection.document(id).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateNotes(id: String, notes: String) {
        try {
            analysisCollection.document(id).update("notes", notes).await()
        } catch (e: Exception) {
            throw e
        }
    }

    // --- FITUR 2: USER PROFILE ---

    suspend fun saveUserProfile(userProfile: UserProfile) {
        try {
            // Pastikan ID di objek sama dengan ID sistem
            val fixedProfile = userProfile.copy(id = userId)
            userDocRef.set(fixedProfile).await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getUserProfile(): UserProfile? {
        return try {
            val snapshot = userDocRef.get().await()
            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserId(): String = userId
}