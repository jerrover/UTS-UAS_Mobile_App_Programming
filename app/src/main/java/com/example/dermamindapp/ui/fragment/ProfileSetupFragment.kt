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
// Ganti import User ke UserProfile
import com.example.dermamindapp.data.model.UserProfile
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

        val currentUsername = prefsHelper.getString(PreferencesHelper.KEY_USER_NAME) ?: ""

        // Observer Status
        viewModel.statusMessage.observe(viewLifecycleOwner) { msg ->
            if (msg == "Profil berhasil diperbarui" || msg == "Profil berhasil diperbarui!") {
                prefsHelper.saveBoolean(PreferencesHelper.KEY_ONBOARDING_COMPLETED, true)
                goToMainPage()
            } else if (msg != null) {
                // Jangan tampilkan Toast error jika msg berisi "Mengupload..." atau status progress
                if (!msg.contains("Mengupload")) {
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
                // viewModel.clearStatus() // Opsional: biarkan agar user bisa baca
            }
        }

        completeButton.setOnClickListener {
            val age = ageEditText.text.toString().trim()

            if (age.isEmpty()) {
                Toast.makeText(requireContext(), "Umur wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedSkinTypeId = skinTypeChipGroup.checkedChipId
            if (selectedSkinTypeId == View.NO_ID) {
                Toast.makeText(requireContext(), "Pilih tipe kulitmu dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val skinType = view.findViewById<Chip>(selectedSkinTypeId).text.toString()

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

            // Simpan Preference Lokal
            prefsHelper.saveString(PreferencesHelper.KEY_USER_AGE, age)
            prefsHelper.saveString(PreferencesHelper.KEY_SKIN_TYPE, skinType)
            prefsHelper.saveString(PreferencesHelper.KEY_PREFERENCES, preferences)
            prefsHelper.saveString(PreferencesHelper.KEY_ROUTINES, routines)

            // --- PERBAIKAN DISINI ---
            // Gunakan UserProfile, bukan User
            val updatedProfile = UserProfile(
                id = currentUsername,
                name = currentUsername,
                age = age,
                skinType = skinType,
                preferences = preferences,
                routines = routines,
                photoUrl = "" // Default kosong dulu
            )

            // Sekarang tipe datanya sudah cocok dengan ViewModel
            viewModel.updateProfileData(updatedProfile)
        }
    }

    private fun goToMainPage() {
        val bundle = bundleOf("show_snackbar" to true)
        try {
            findNavController().navigate(R.id.action_profileSetupFragment_to_mainFragment, bundle)
        } catch (e: Exception) {
            // Fallback jika action ID salah
            Toast.makeText(requireContext(), "Navigasi ke Main gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}