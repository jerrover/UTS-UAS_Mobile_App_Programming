package com.example.dermamindapp

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// MainActivity adalah satu-satunya Activity dalam aplikasi ini dan berfungsi sebagai
// host untuk semua fragment. Pola ini dikenal sebagai Single-Activity Architecture.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Menambahkan callback khusus untuk menangani penekanan tombol kembali (back button).
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navController = findNavController(R.id.nav_host_fragment)
                // Jika pengguna berada di halaman utama (MainFragment), tampilkan dialog konfirmasi keluar.
                if (navController.currentDestination?.id == R.id.mainFragment) {
                    showExitConfirmationDialog()
                } else {
                    // Jika tidak, biarkan sistem menangani navigasi kembali seperti biasa.
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    // Fungsi untuk menampilkan dialog konfirmasi saat pengguna akan keluar dari aplikasi.
    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.exit_dialog_title))
            .setMessage(getString(R.string.exit_dialog_message))
            .setNegativeButton(getString(R.string.dialog_no)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.dialog_yes)) { _, _ ->
                finish() // Menutup aplikasi jika pengguna menekan "Yes".
            }
            .show()
    }
}