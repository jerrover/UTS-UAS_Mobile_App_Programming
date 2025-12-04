package com.example.dermamindapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SkinAnalysis(
    var id: String = "",
    val date: Long = 0L,
    val imageUri: String = "",
    val result: String = "",
    var notes: String? = ""
) : Parcelable