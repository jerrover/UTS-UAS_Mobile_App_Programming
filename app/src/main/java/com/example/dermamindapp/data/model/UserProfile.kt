// File: src/main/java/com/example/dermamindapp/data/model/UserProfile.kt
package com.example.dermamindapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",    // Menyimpan URL Cloudinary
    val age: String = "",
    val skinType: String = "",
    val joinDate: Long = System.currentTimeMillis()
) : Parcelable