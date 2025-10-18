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
import com.google.android.material.snackbar.Snackbar

// Fragment utama yang berfungsi sebagai container untuk navigasi utama aplikasi
// (Home, Journey, Products, Profile) menggunakan BottomNavigationView.
class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        // Mendapatkan NavHostFragment yang merupakan container untuk fragment-fragment
        // dalam grafik navigasi utama (main_nav).
        val navHostFragment = childFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Menghubungkan BottomNavigationView dengan NavController agar item menu
        // dapat secara otomatis menangani navigasi antar fragment.
        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        // Mengecek apakah ada argumen destinasi yang dikirimkan, misalnya dari AnalysisResultFragment.
        // Jika ada, item menu yang sesuai akan dipilih secara otomatis.
        val destinationId = arguments?.getInt(AnalysisResultFragment.ARG_DESTINATION_ID, 0) ?: 0
        if (destinationId != 0) {
            bottomNav.selectedItemId = destinationId
        }

        // Listener kustom untuk menangani navigasi saat item menu dipilih.
        // Ini memastikan navigasi terjadi sesuai dengan ID item menu.
        bottomNav.setOnItemSelectedListener { item ->
            navController.navigate(item.itemId)
            true
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mengecek apakah ada argumen untuk menampilkan Snackbar,
        // misalnya setelah profil berhasil disimpan.
        val showSnackbar = arguments?.getBoolean("show_snackbar") ?: false
        if (showSnackbar) {
            Snackbar.make(view, getString(R.string.feedback_profile_saved), Snackbar.LENGTH_SHORT).show()
            // Menghapus argumen setelah Snackbar ditampilkan agar tidak muncul lagi.
            arguments?.remove("show_snackbar")
        }
    }
}