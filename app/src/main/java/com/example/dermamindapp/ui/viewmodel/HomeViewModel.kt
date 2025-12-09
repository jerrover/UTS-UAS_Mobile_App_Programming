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

    // TAMBAHAN PENTING: Variabel Loading & Error
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val API_KEY = "7c1be6ed60154a36bf8dd1966939161c"

    init {
        fetchArticles()
    }

    fun fetchArticles() {
        _isLoading.value = true // Mulai loading
        viewModelScope.launch(Dispatchers.IO) {
            checkAndSeedFallback()
            try {
                // Ambil dari API
                val response = ApiConfig.getApiService().getHealthNews(apiKey = API_KEY)

                if (response.articles.isNullOrEmpty()) {
                    Log.e("API_CACHE", "API kosong, pakai Firestore.")
                    _articles.postValue(getArticlesFromFirestore())
                } else {
                    val liveArticles = response.articles.mapIndexed { index, apiArticle ->
                        Article(
                            id = index + 1,
                            title = apiArticle.title ?: "Judul Tidak Tersedia",
                            content = apiArticle.description ?: "Baca selengkapnya...",
                            imageUrl = apiArticle.urlToImage ?: ""
                        )
                    }
                    _articles.postValue(liveArticles)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error: ${e.message}")
                _errorMessage.postValue("Gagal memuat berita: ${e.message}")
                _articles.postValue(getArticlesFromFirestore())
            } finally {
                _isLoading.postValue(false) // Stop loading
            }
        }
    }

    private suspend fun checkAndSeedFallback() {
        try {
            val snapshot = articlesCollection.get().await()
            if (snapshot.isEmpty) {
                val fallback = getFallbackArticlesList()
                fallback.forEach { article ->
                    articlesCollection.document(article.id.toString()).set(article)
                }
            }
        } catch (e: Exception) { Log.e("SeedError", e.message.toString()) }
    }

    private suspend fun getArticlesFromFirestore(): List<Article> {
        return try {
            val snapshot = articlesCollection.orderBy("id").get().await()
            val list = snapshot.toObjects(Article::class.java)
            if (list.isEmpty()) getFallbackArticlesList() else list
        } catch (e: Exception) { getFallbackArticlesList() }
    }

    private fun getFallbackArticlesList(): List<Article> {
        return listOf(
            Article(1, "Jangan Pencet Jerawat!", "Memencet jerawat bisa bikin bopeng.", "https://images.unsplash.com/photo-1512290923902-8a9f81dc236c"),
            Article(2, "Manfaat Retinol", "Anti-aging terbaik malam hari.", "https://images.unsplash.com/photo-1620916566398-39f1143ab7be"),
            Article(3, "Ciri Skin Barrier Rusak", "Perih dan kemerahan? Fokus hidrasi.", "https://images.unsplash.com/photo-1616394584738-fc6e612e71b9"),
            Article(4, "Kulit Berminyak Butuh Pelembab", "Pilih tekstur gel.", "https://images.unsplash.com/photo-1616683693504-3ea7e9ad6fec")
        )
    }
}