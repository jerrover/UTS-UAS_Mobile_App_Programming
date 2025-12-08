package com.example.dermamindapp

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Konfigurasi Cloudinary
        val config = HashMap<String, String>()
        config["cloud_name"] = "djoesxqjo"
        config["api_key"] = "937392177566299"
        config["api_secret"] = "ATUY8DzXE2OQE7wRK44PFr80uaw"

        // Inisialisasi (hanya boleh dipanggil sekali seumur hidup aplikasi)
        MediaManager.init(this, config)
    }
}