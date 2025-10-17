package com.example.dermamindapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Int,
    val name: String,
    val suitability: String,
    val description: String,
    val imageUrl: String,
    val tokopediaUrl: String,
    val shopeeUrl: String
) : Parcelable