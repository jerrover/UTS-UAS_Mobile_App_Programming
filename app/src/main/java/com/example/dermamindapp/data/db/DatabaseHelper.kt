package com.example.dermamindapp.data.db

import android.content.Context
import android.util.Log
import com.example.dermamindapp.data.model.SkinAnalysis
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class DatabaseHelper(context: Context?) {

    // Inisialisasi Firestore
    private val firestore = FirebaseFirestore.getInstance()

    // Nama collection di Firebase (seperti nama Tabel di SQL)
    private val collectionRef = firestore.collection("skin_analyses")

    // CREATE: Simpan data ke Firestore
    suspend fun addAnalysis(analysis: SkinAnalysis) {
        try {
            // Minta Firebase buatkan ID dokumen baru yang unik
            val documentRef = collectionRef.document()

            // Set ID ke objek analysis kita supaya sinkron
            analysis.id = documentRef.id

            // Simpan data ke internet
            documentRef.set(analysis).await()
            Log.d("DatabaseHelper", "Data berhasil disimpan: ${analysis.id}")
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Gagal menyimpan data", e)
            throw e // Lempar error biar UI tahu kalau gagal
        }
    }

    // READ: Ambil semua data
    suspend fun getAllAnalyses(): List<SkinAnalysis> {
        return try {
            val snapshot = collectionRef
                .orderBy("date", Query.Direction.DESCENDING) // Urutkan dari yang terbaru
                .get()
                .await()

            // Ubah setiap dokumen JSON menjadi objek SkinAnalysis
            snapshot.toObjects(SkinAnalysis::class.java)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Gagal mengambil data", e)
            emptyList()
        }
    }

    // UPDATE: Update catatan (notes)
    suspend fun updateNotes(id: String, notes: String) {
        try {
            collectionRef.document(id)
                .update("notes", notes)
                .await()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Gagal update notes", e)
            throw e
        }
    }

    // DELETE: Hapus data
    suspend fun deleteAnalysis(id: String) {
        try {
            collectionRef.document(id)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Gagal hapus data", e)
            throw e
        }
    }
}