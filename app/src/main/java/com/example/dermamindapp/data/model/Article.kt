package com.example.dermamindapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Implementasi Parcelable memungkinkan objek ini untuk dikirim antar komponen Android
// seperti Activity atau Fragment.
@Parcelize
data class Article(
    val id: Int,
    val title: String,
    val content: String,
    val imageUrl: String
) : Parcelable