package com.example.dermamindapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    // ID unik untuk database (String lebih fleksibel untuk Firebase)
    var id: String = "",

    val name: String = "",
    val brand: String = "",

    // Kategori: misal "Serum", "Moisturizer", "Toner" (Penting untuk filter)
    val category: String = "",

    // Suitability: "Acne", "Oily", "Dry", "All Skin Type"
    // (Penting untuk rekomendasi logic AI kamu)
    val suitability: String = "",

    val description: String = "",

    // Harga untuk filter range harga (Default 0.0 biar ga error kalau kosong)
    val price: Double = 0.0,

    // URL Gambar (bisa dari Cloudinary atau link gambar online)
    val imageUrl: String = "",

    // Link E-commerce
    val tokopediaUrl: String = "",
    val shopeeUrl: String = ""
) : Parcelable {
    // Constructor kosong (Wajib untuk Firebase agar bisa convert data otomatis)
    constructor() : this("", "", "", "", "", "", 0.0, "", "", "")
}