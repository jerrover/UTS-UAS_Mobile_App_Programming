package com.example.dermamindapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val age: String = "",
    val skinType: String = "",
    val preferences: String = "", // Tambahan baru
    val routines: String = "",    // Tambahan baru
    val joinDate: Long = System.currentTimeMillis()
) : Parcelable