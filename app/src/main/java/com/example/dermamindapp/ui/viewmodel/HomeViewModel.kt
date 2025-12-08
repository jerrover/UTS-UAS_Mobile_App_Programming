package com.example.dermamindapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dermamindapp.data.model.Article
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val articlesCollection = db.collection("articles")

    private val _articles = MutableLiveData<List<Article>>()
    val articles: LiveData<List<Article>> = _articles

    fun fetchArticles() {
        viewModelScope.launch {
            try {
                // LOGIKA: Langsung timpa data lama dengan data baru biar gambar update
                seedSkincareArticles()

                // Ambil data yang baru saja di-upload
                val snapshot = articlesCollection.orderBy("id").get().await()
                val list = snapshot.toObjects(Article::class.java)
                _articles.value = list

            } catch (e: Exception) {
                Log.e("FIREBASE_TEST", "Error: ${e.message}")
                _articles.value = emptyList()
            }
        }
    }

    private suspend fun seedSkincareArticles() {
        val skincareArticles = listOf(
            Article(
                id = 1,
                title = "Jangan Pencet Jerawat!",
                content = "Bisa bikin bopeng dan infeksi menyebar. Gunakan obat totol.",
                // Gambar Perawatan Wajah (Unsplash)
                imageUrl = "https://images.unsplash.com/photo-1512290923902-8a9f81dc236c?auto=format&fit=crop&w=800&q=80"
            ),
            Article(
                id = 2,
                title = "Urutan Skincare Pagi",
                content = "Facial Wash -> Toner -> Serum -> Moisturizer -> Sunscreen.",
                // Gambar Wanita Cuci Muka (Unsplash)
                imageUrl = "https://images.unsplash.com/photo-1556228552-523d4b4e09d2?auto=format&fit=crop&w=800&q=80"
            ),
            Article(
                id = 3,
                title = "Manfaat Retinol",
                content = "Anti-aging terbaik. Pakai malam hari saja dan wajib sunscreen paginya.",
                // Gambar Botol Serum (Unsplash)
                imageUrl = "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=800&q=80"
            ),
            Article(
                id = 4,
                title = "Ciri Skin Barrier Rusak",
                content = "Perih, merah, dan bruntusan? Stop eksfoliasi, fokus hidrasi.",
                // Gambar Wajah Sehat/Natural (Unsplash)
                imageUrl = "https://images.unsplash.com/photo-1616394584738-fc6e612e71b9?auto=format&fit=crop&w=800&q=80"
            ),
            Article(
                id = 5,
                title = "Mitos Kulit Berminyak",
                content = "Kulit berminyak tetap butuh pelembab (pilih tekstur Gel).",
                // Gambar Produk Skincare (Unsplash)
                imageUrl = "https://images.unsplash.com/photo-1616683693504-3ea7e9ad6fec?auto=format&fit=crop&w=800&q=80"
            )
        )

        // Upload (Timpa) ke Firebase
        skincareArticles.forEach { article ->
            articlesCollection.document(article.id.toString()).set(article)
        }
    }
}