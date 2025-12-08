package com.example.dermamindapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Article(
    val id: Int = 0,             // TAMBAHKAN "= 0"
    val title: String = "",      // TAMBAHKAN "= """
    val content: String = "",    // TAMBAHKAN "= """
    val imageUrl: String = ""    // TAMBAHKAN "= """
) : Parcelable