package com.example.dermamindapp.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.dermamindapp.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore

class ProductRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productsCollection = db.collection("products")

    fun getProducts(): LiveData<Result<List<Product>>> {
        val result = MutableLiveData<Result<List<Product>>>()

        productsCollection.get()
            .addOnSuccessListener { documents ->
                val productList = ArrayList<Product>()
                for (document in documents) {
                    try {
                        // Mengubah dokumen Firestore menjadi objek Product
                        val product = document.toObject(Product::class.java)
                        // Pastikan ID terisi (jika kosong di dokumen, pakai ID dokumen)
                        if (product.id.isEmpty()) {
                            product.id = document.id
                        }
                        productList.add(product)
                    } catch (e: Exception) {
                        Log.e("ProductRepo", "Error parsing product: ${e.message}")
                    }
                }
                result.value = Result.success(productList)
            }
            .addOnFailureListener { exception ->
                result.value = Result.failure(exception)
            }

        return result
    }
}