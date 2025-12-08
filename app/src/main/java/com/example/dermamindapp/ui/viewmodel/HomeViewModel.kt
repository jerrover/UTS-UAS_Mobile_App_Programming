package com.example.dermamindapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dermamindapp.data.api.ApiConfig
import com.example.dermamindapp.data.model.Article
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val articlesCollection = db.collection("articles")

    private val _articles = MutableLiveData<List<Article>>()
    val articles: LiveData<List<Article>> = _articles

    private val API_KEY = "7c1be6ed60154a36bf8dd1966939161c"

    init {
        // Panggil fetchArticles saat ViewModel pertama kali dibuat
        fetchArticles()
    }

    fun fetchArticles() {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Pastikan dulu data fallback (cache) ada di Firebase (Hanya jalan sekali saat kosong)
            checkAndSeedFallback()

            try {
                // 2. COBA AMBIL DARI API (Online)
                val response = ApiConfig.getApiService().getHealthNews(apiKey = API_KEY)

                if (response.articles.isNullOrEmpty()) {
                    // Jika API tidak error tapi kosong, pakai data cache Firestore
                    Log.e("API_CACHE", "API returned empty list, using Firestore cache.")
                    _articles.postValue(getArticlesFromFirestore())
                    return@launch
                }

                // Konversi model API ke model lokal (Article)
                val liveArticles = response.articles?.mapIndexed { index, apiArticle ->
                    Article(
                        id = index + 1,
                        title = apiArticle.title ?: "Judul Tidak Tersedia",
                        content = apiArticle.description ?: "Deskripsi tidak tersedia, baca selengkapnya...",
                        imageUrl = apiArticle.urlToImage ?: "https://via.placeholder.com/150"
                    )
                }?.take(5) ?: getFallbackArticlesList()

                Log.d("API_SUCCESS", "Berhasil ambil ${liveArticles.size} artikel LIVE dari API.")
                _articles.postValue(liveArticles)

            } catch (e: HttpException) {
                // 3. ERROR API (e.g., 403 Forbidden) -> Gunakan Cache Firestore
                Log.e("API_CACHE", "HTTP Error (${e.code()}). Menggunakan data cache Firestore. Pesan: ${e.message()}")
                _articles.postValue(getArticlesFromFirestore())
            } catch (e: IOException) {
                // 4. ERROR KONEKSI/UMUM (Offline) -> Gunakan Cache Firestore
                Log.e("API_CACHE", "Koneksi Error. Menggunakan data cache Firestore. Pesan: ${e.message}")
                _articles.postValue(getArticlesFromFirestore())
            } catch (e: Exception) {
                Log.e("API_CACHE", "Error umum. Menggunakan data cache Firestore. Pesan: ${e.message}")
                _articles.postValue(getArticlesFromFirestore())
            }
        }
    }

    // Fungsi Kunci: Memeriksa dan mengisi data fallback ke Firestore (Hanya jalan sekali)
    private suspend fun checkAndSeedFallback() {
        // Mengecek apakah collection sudah terisi
        val snapshot = articlesCollection.get().await()
        if (snapshot.isEmpty) {
            Log.d("FIREBASE_SEED", "Collection 'articles' kosong. Memulai Seeding data fallback...")
            val fallback = getFallbackArticlesList()
            // Menggunakan .set() dengan ID untuk memastikan data tidak duplikat dan mudah ditimpa
            fallback.forEach { article ->
                articlesCollection.document(article.id.toString()).set(article)
            }
        } else {
            Log.d("FIREBASE_SEED", "Collection 'articles' sudah ada, Seeding dilewati.")
        }
    }

    // Fungsi Kunci: Membaca data dari Firestore (Data Cache)
    private suspend fun getArticlesFromFirestore(): List<Article> {
        return try {
            val snapshot = articlesCollection.orderBy("id").get().await()
            snapshot.toObjects(Article::class.java)
        } catch (e: Exception) {
            Log.e("FIREBASE_CACHE_ERROR", "Gagal mengambil data dari Firestore: ${e.message}")
            getFallbackArticlesList() // Kalau Firestore-nya juga error, kasih hardcode terakhir
        }
    }

    // Data Hardcode yang akan dijadikan Cache Fallback
    private fun getFallbackArticlesList(): List<Article> {
        return listOf(
            Article(1, "Jangan Pencet Jerawat!", "Memencet jerawat bisa bikin bopeng dan infeksi menyebar.", "https://images.unsplash.com/photo-1512290923902-8a9f81dc236c?auto=format&fit=crop&w=800&q=80"),
            Article(2, "Manfaat Retinol", "Zat anti-aging terbaik. Gunakan malam hari dan wajib sunscreen paginya.", "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=800&q=80"),
            Article(3, "Ciri Skin Barrier Rusak", "Kulit perih dan kemerahan? Stop eksfoliasi, fokus hidrasi dengan Ceramide.", "https://images.unsplash.com/photo-1616394584738-fc6e612e71b9?auto=format&fit=crop&w=800&q=80"),
            Article(4, "Mitos Kulit Berminyak", "Kulit berminyak tetap butuh pelembab (pilih tekstur Gel).", "https://images.unsplash.com/photo-1616683693504-3ea7e9ad6fec?auto=format&fit=crop&w=800&q=80")
        )
    }
}