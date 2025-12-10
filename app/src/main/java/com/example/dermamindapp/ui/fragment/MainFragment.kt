package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.dermamindapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi Bottom Navigation
        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // 2. Ambil NavController dari Inner Nav Host
        val navHostFragment = childFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as? NavHostFragment

        if (navHostFragment != null) {
            val navController = navHostFragment.navController

            // Hubungkan BottomNav dengan NavController
            bottomNav.setupWithNavController(navController)

            // 3. LOGIKA VISIBILITAS NAVBAR
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    // DAFTAR HALAMAN FULL SCREEN (TANPA NAVBAR):
                    R.id.productRecommendationFragment, // Katalog Produk Umum (Tetap muncul navbar)
                    R.id.articlesFragment,
                    R.id.cameraFragment,                // Kamera
                    R.id.analysisResultFragment,        // <--- TAMBAHAN: Hasil Analisis
                    R.id.skinDetailFragment,            // Detail Riwayat
                    R.id.productDetailsFragment,        // Detail Produk
                    R.id.articleDetailFragment          // Baca Artikel
                        -> {
                        bottomNav.visibility = View.GONE
                    }
                    else -> {
                        // Halaman Utama (Home, Journey, Profile) -> Navbar Muncul
                        bottomNav.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}