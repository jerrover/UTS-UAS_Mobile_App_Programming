package com.example.dermamindapp.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/top-headlines")
    suspend fun getHealthNews(
        @Query("country") country: String = "id", // Target Indonesia
        @Query("category") category: String = "health", // Kategori Kesehatan
        @Query("apiKey") apiKey: String // API Key kamu
    ): NewsResponse
}