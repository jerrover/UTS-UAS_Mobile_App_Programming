package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.dermamindapp.R
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.data.model.User
import com.example.dermamindapp.ui.viewmodel.ProfileViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ProfileSetupFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        prefsHelper = PreferencesHelper(requireContext())

        val completeButton: Button = view.findViewById(R.id.completeButton)
        val ageEditText: EditText = view.findViewById(R.id.ageEditText)
        val skinTypeChipGroup: ChipGroup = view.findViewById(R.id.skinTypeChipGroup)
        val preferencesChipGroup: ChipGroup = view.findViewById(R.id.preferencesChipGroup)
        val routinesChipGroup: ChipGroup = view.findViewById(R.id.routinesChipGroup)

        // Kita ambil username yang tadi disimpan saat Register
        val currentUsername = prefsHelper.getString(PreferencesHelper.KEY_USER_NAME) ?: ""

        // Observer Status Update dari ViewModel
        viewModel.statusMessage.observe(viewLifecycleOwner) { msg ->
            if (msg == "Profil berhasil diperbarui!") {
                // Kunci: Tandai onboarding selesai biar besok gak perlu isi ini lagi
                prefsHelper.saveBoolean(PreferencesHelper.KEY_ONBOARDING_COMPLETED, true)
                goToMainPage()
            } else if (msg != null) {
                // Tampilkan error jika ada
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                viewModel.clearStatus()
            }
        }

        completeButton.setOnClickListener {
            val age = ageEditText.text.toString().trim()

            // 1. Validasi Input
            if (age.isEmpty()) {
                Toast.makeText(requireContext(), "Umur wajib diisi untuk analisis AI!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedSkinTypeId = skinTypeChipGroup.checkedChipId
            if (selectedSkinTypeId == View.NO_ID) {
                Toast.makeText(requireContext(), "Pilih tipe kulitmu dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ambil text dari Chip Tipe Kulit (Single)
            val skinType = view.findViewById<Chip>(selectedSkinTypeId).text.toString()

            // Helper function ambil text dari Multi-Selection Chip
            fun getSelectedChips(group: ChipGroup): String {
                val ids = group.checkedChipIds
                if (ids.isEmpty()) return ""
                return ids.joinToString(", ") { id -> group.findViewById<Chip>(id).text.toString() }
            }

            val preferences = getSelectedChips(preferencesChipGroup)
            if (preferences.isEmpty()) {
                Toast.makeText(requireContext(), "Pilih minimal 1 masalah kulit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val routines = getSelectedChips(routinesChipGroup)
            if (routines.isEmpty()) {
                Toast.makeText(requireContext(), "Pilih waktu skincare rutinmu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. Simpan ke HP (SharedPreferences) - Biar aplikasi cepat baca data lokal
            prefsHelper.saveString(PreferencesHelper.KEY_USER_AGE, age)
            prefsHelper.saveString(PreferencesHelper.KEY_SKIN_TYPE, skinType)
            prefsHelper.saveString(PreferencesHelper.KEY_PREFERENCES, preferences)
            prefsHelper.saveString(PreferencesHelper.KEY_ROUTINES, routines)

            // 3. Simpan ke Firebase (Cloud) - Update user yang sudah ada
            // Kita pakai ID = currentUsername karena itulah ID dokumen kita
            val updatedUser = User(
                id = currentUsername,
                name = currentUsername, // Nama pakai username aja
                age = age,
                skinType = skinType,
                preferences = preferences,
                routines = routines
            )

            // Panggil fungsi update, BUKAN saveUserProfile (karena save bikin baru)
            viewModel.updateProfileData(updatedUser)
        }
    }

    private fun goToMainPage() {
        val bundle = bundleOf("show_snackbar" to true)
        // Pastikan ID action ini ada di navigation graph
        findNavController().navigate(R.id.action_profileSetupFragment_to_mainFragment, bundle)
    }
}