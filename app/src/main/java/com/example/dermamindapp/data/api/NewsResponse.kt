package com.example.dermamindapp.data.api

// Model untuk menampung respons keseluruhan dari API
data class NewsResponse(
    // status, totalResults (tidak dipakai)
    val articles: List<ApiArticle>? = null
)

// Model untuk menampung satu artikel dari API
data class ApiArticle(
    val title: String? = null,
    val description: String? = null,
    val urlToImage: String? = null // Link gambar
    // author, url, publishedAt (tidak dipakai)
)