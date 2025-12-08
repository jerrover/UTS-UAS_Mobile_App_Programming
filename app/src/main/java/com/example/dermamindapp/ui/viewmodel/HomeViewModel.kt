package com.example.dermamindapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dermamindapp.data.api.ApiConfig
import com.example.dermamindapp.data.model.Article
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import retrofit2.HttpException
import com.google.firebase.firestore.FirebaseFirestore // Import ini masih diperlukan untuk fitur lain, tapi kita tidak pakai untuk artikel.

class HomeViewModel : ViewModel() {

    // Kita tidak perlu FirebaseFirestore untuk Artikel lagi, tapi biarkan import di atas.
    // Variabel ini diperlukan jika kamu masih menggunakan Firebase untuk fitur lain di HomeViewModel.
    private val db = FirebaseFirestore.getInstance()
    private val articlesCollection = db.collection("articles")

    private val _articles = MutableLiveData<List<Article>>()
    val articles: LiveData<List<Article>> = _articles

    // API Key dari user
    private val API_KEY = "7c1be6ed60154a36bf8dd1966939161c"

    fun fetchArticles() {
        // Jalankan di Coroutine Scope (Wajib untuk API)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. COBA AMBIL DARI API (Online)
                val response = ApiConfig.getApiService().getHealthNews(apiKey = API_KEY)

                if (response.articles.isNullOrEmpty()) {
                    // Jika API tidak error tapi mengembalikan list kosong, pakai fallback
                    Log.e("API_STATUS", "API returned empty list, using fallback.")
                    _articles.postValue(getFallbackArticles())
                    return@launch
                }

                // Konversi model API ke model lokal (Article)
                val newArticles = response.articles?.mapIndexed { index, apiArticle ->
                    Article(
                        id = index + 1,
                        title = apiArticle.title ?: "Judul Tidak Tersedia",
                        content = apiArticle.description ?: "Deskripsi tidak tersedia, baca selengkapnya...",
                        imageUrl = apiArticle.urlToImage ?: "https://via.placeholder.com/150"
                    )
                }?.take(5) ?: getFallbackArticles()

                Log.d("API_SUCCESS", "Berhasil ambil ${newArticles.size} artikel dari API.")
                _articles.postValue(newArticles)

            } catch (e: HttpException) {
                // 2. CATCH ERROR API (Contoh: 403 Forbidden, 401 Unauthorized)
                Log.e("API_FALLBACK", "HTTP Error (${e.code()}), menggunakan data fallback. Pesan: ${e.message()}")
                _articles.postValue(getFallbackArticles())
            } catch (e: Exception) {
                // 3. CATCH ERROR KONEKSI/UMUM (Offline)
                Log.e("API_FALLBACK", "Koneksi Error (${e.message}), menggunakan data fallback.")
                _articles.postValue(getFallbackArticles())
            }
        }
    }

    // Fungsi untuk data Hardcode (Fallback)
    private fun getFallbackArticles(): List<Article> {
        Log.d("API_FALLBACK", "Menampilkan data artikel offline.")
        return listOf(
            Article(
                id = 1,
                title = "Urutan Skincare Pagi",
                content = "Facial Wash -> Toner -> Serum -> Moisturizer -> Sunscreen. Wajib sunscreen!",
                imageUrl = "https://images.unsplash.com/photo-1556228552-523d4b4e09d2?auto=format&fit=crop&w=800&q=80"
            ),
            Article(
                id = 2,
                title = "Jangan Pencet Jerawat!",
                content = "Memencet jerawat bisa bikin bopeng dan infeksi menyebar.",
                imageUrl = "https://images.unsplash.com/photo-1512290923902-8a9f81dc236c?auto=format&fit=crop&w=800&q=80"
            ),
            Article(
                id = 3,
                title = "Manfaat Retinol",
                content = "Zat anti-aging terbaik. Gunakan malam hari dan wajib sunscreen paginya.",
                imageUrl = "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=800&q=80"
            ),
            Article(
                id = 4,
                title = "Ciri Skin Barrier Rusak",
                content = "Kulit perih dan kemerahan? Stop eksfoliasi, fokus hidrasi dengan Ceramide.",
                imageUrl = "https://images.unsplash.com/photo-1616394584738-fc6e612e71b9?auto=format&fit=crop&w=800&q=80"
            ),
            Article(
                id = 5,
                title = "Mitos Kulit Berminyak",
                content = "Kulit berminyak tetap butuh pelembab (pilih tekstur Gel).",
                imageUrl = "https://images.unsplash.com/photo-1616683693504-3ea7e9ad6fec?auto=format&fit=crop&w=800&q=80"
            )
        )
    }
}