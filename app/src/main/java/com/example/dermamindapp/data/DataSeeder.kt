package com.example.dermamindapp.data

import android.content.Context
import android.util.Log
import com.example.dermamindapp.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

object DataSeeder {

    fun seedProducts(context: Context) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("products")

        // 1. Cek dulu apakah data sudah ada agar tidak duplikat/double
        collectionRef.get().addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                // Jika kosong, mulai upload
                Log.d("DataSeeder", "Database kosong, memulai seeding...")
                uploadData(context, db)
            } else {
                Log.d("DataSeeder", "Data produk sudah ada. Seeding dilewati.")
            }
        }.addOnFailureListener { exception ->
            Log.e("DataSeeder", "Gagal mengecek database", exception)
        }
    }

    private fun uploadData(context: Context, db: FirebaseFirestore) {
        val jsonString = getJsonDataFromAsset(context, "products.json")
        if (jsonString != null) {
            val gson = Gson()
            val listType = object : TypeToken<List<Product>>() {}.type
            val products: List<Product> = gson.fromJson(jsonString, listType)

            for (product in products) {
                // Menggunakan 'add' agar ID digenerate otomatis oleh Firestore,
                // atau gunakan 'document(product.id).set(product)' jika ingin ID dari JSON
                db.collection("products")
                    .add(product)
                    .addOnSuccessListener { documentReference ->
                        // Update ID di dalam dokumen agar sama dengan ID Firestore (Opsional tapi bagus)
                        documentReference.update("id", documentReference.id)
                        Log.d("DataSeeder", "Berhasil upload: ${product.name}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("DataSeeder", "Gagal upload: ${product.name}", e)
                    }
            }
        }
    }

    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }
}