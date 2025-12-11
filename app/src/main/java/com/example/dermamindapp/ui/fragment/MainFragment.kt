package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
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

            // Hubungkan BottomNav dengan NavController (untuk navigasi dasar & update state item)
            bottomNav.setupWithNavController(navController)

            // =================================================================
            // >> LOGIKA CUSTOM: RESET STACK SAAT KLIK TOMBOL HOME <<
            // =================================================================
            // Kita timpa listener agar Home (R.id.homeFragment) selalu kembali ke root
            bottomNav.setOnItemSelectedListener { item ->
                val homeDestinationId = R.id.homeFragment // Pastikan ID ini sesuai dengan ID di bottom_nav_menu.xml

                if (item.itemId == homeDestinationId) {
                    // Konfigurasi NavOptions untuk mereset Home ke kondisi awal
                    val navOptions = NavOptions.Builder()
                        // Pop semua destinasi di stack sampai root Home, dan hapus Home itu sendiri (true)
                        .setPopUpTo(homeDestinationId, true)
                        // Pastikan hanya ada satu instance Home di stack
                        .setLaunchSingleTop(true)
                        .build()

                    navController.navigate(item.itemId, null, navOptions)
                    true
                } else {
                    // Untuk tombol lain: gunakan navigasi default
                    navController.navigate(item.itemId)
                    true
                }
            }

            // Tambahkan logika untuk pop stack saat item yang sama diklik ulang
            bottomNav.setOnItemReselectedListener { item ->
                val homeDestinationId = R.id.homeFragment

                if (item.itemId == homeDestinationId) {
                    // Pop semua destinasi di atas HomeFragment, tetapi TIDAK menghapus HomeFragment itu sendiri (false)
                    navController.popBackStack(homeDestinationId, false)
                }
                // Jika ada tab lain yang ingin direset saat klik ulang, tambahkan di sini
            }

            // 3. LOGIKA VISIBILITAS NAVBAR (yang Anda tambahkan)
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    // DAFTAR HALAMAN FULL SCREEN (TANPA NAVBAR):
                    R.id.productRecommendationFragment,
                    R.id.articlesFragment,
                    R.id.cameraFragment,
                    R.id.analysisResultFragment,
                    R.id.skinDetailFragment,
                    R.id.productDetailsFragment,
                    R.id.articleDetailFragment
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