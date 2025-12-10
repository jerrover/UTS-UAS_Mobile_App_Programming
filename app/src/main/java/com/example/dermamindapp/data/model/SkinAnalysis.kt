package com.example.dermamindapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SkinAnalysis(
    var id: String = "",
    var userId: String = "",
    val date: Long = 0L,
    val imageUri: String = "",
    val result: String = "",
    var notes: String? = "",
    var usedProducts: ArrayList<Product> = ArrayList()
) : Parcelable