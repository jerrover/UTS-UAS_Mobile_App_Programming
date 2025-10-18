package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dermamindapp.R
import com.example.dermamindapp.data.PreferencesHelper

// Fragment ini berfungsi sebagai layar pembuka (splash screen) aplikasi.
class SplashFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        val prefsHelper = PreferencesHelper(requireContext())

        // Menggunakan Handler untuk menunda navigasi selama 2 detik.
        Handler(Looper.getMainLooper()).postDelayed({
            // Memastikan fragment masih terpasang (attached) sebelum melakukan navigasi
            // untuk menghindari NullPointerException.
            if (isAdded) {
                // Memeriksa apakah pengguna sudah pernah login (dengan asumsi nama pengguna disimpan).
                if (prefsHelper.getString(PreferencesHelper.KEY_USER_NAME) != null) {
                    // Jika sudah, langsung navigasi ke halaman utama.
                    findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
                } else {
                    // Jika belum, navigasi ke alur onboarding.
                    findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment)
                }
            }
        }, 2000)

        return view
    }
}