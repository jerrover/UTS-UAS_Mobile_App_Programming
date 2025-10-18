package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.example.dermamindapp.R
import com.example.dermamindapp.data.PreferencesHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// Fragment ini menampilkan halaman profil pengguna.
class ProfileFragment : Fragment() {

    // Komponen UI untuk menampilkan informasi profil.
    private lateinit var tvUserName: TextView
    private lateinit var tvUserAge: TextView
    private lateinit var tvSkinType: TextView
    private lateinit var tvPreferences: TextView
    private lateinit var tvRoutines: TextView
    // Helper untuk mengakses data dari SharedPreferences.
    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = PreferencesHelper(requireContext())

        // Inisialisasi komponen UI dari layout.
        tvUserName = view.findViewById(R.id.profile_name)
        tvUserAge = view.findViewById(R.id.profile_age)
        tvSkinType = view.findViewById(R.id.tvSkinTypeValue)
        tvPreferences = view.findViewById(R.id.tvPreferencesValue)
        tvRoutines = view.findViewById(R.id.tvRoutinesValue)

        // Memuat dan menampilkan data profil.
        loadProfileData()

        // Menangani aksi klik pada tombol logout.
        val logoutButton: Button = view.findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    // Memuat data pengguna dari SharedPreferences dan menampilkannya di UI.
    private fun loadProfileData() {
        val userName = prefsHelper.getString(PreferencesHelper.KEY_USER_NAME)
        val userAge = prefsHelper.getString(PreferencesHelper.KEY_USER_AGE)
        val skinType = prefsHelper.getString(PreferencesHelper.KEY_SKIN_TYPE)
        val preferences = prefsHelper.getString(PreferencesHelper.KEY_PREFERENCES)
        val routines = prefsHelper.getString(PreferencesHelper.KEY_ROUTINES)

        tvUserName.text = userName ?: "User"
        tvUserAge.text = "Age ${userAge ?: "Not set"}"
        tvSkinType.text = skinType ?: "Not set"
        tvPreferences.text = preferences ?: "Not set"
        tvRoutines.text = routines ?: "Not set"
    }

    // Menampilkan dialog konfirmasi sebelum melakukan logout.
    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.logout_dialog_title))
            .setMessage(getString(R.string.logout_dialog_message))
            .setNegativeButton(getString(R.string.dialog_no)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.dialog_yes)) { _, _ ->
                // Menghapus semua data dari SharedPreferences saat logout.
                prefsHelper.clear()

                // Menampilkan pesan konfirmasi logout.
                Toast.makeText(requireContext(), getString(R.string.feedback_logged_out), Toast.LENGTH_SHORT).show()
                // Mengatur opsi navigasi untuk membersihkan back stack.
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.app_nav, true)
                    .build()
                // Navigasi kembali ke halaman onboarding.
                requireActivity().findNavController(R.id.nav_host_fragment)
                    .navigate(R.id.onboardingFragment, null, navOptions)
            }
            .show()
    }
}