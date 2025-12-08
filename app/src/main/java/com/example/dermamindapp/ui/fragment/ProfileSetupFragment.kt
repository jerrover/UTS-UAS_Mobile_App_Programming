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
import com.example.dermamindapp.ui.viewmodel.ProfileViewModel // Pastikan import ini ada
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ProfileSetupFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Panggil ViewModel
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        val prefsHelper = PreferencesHelper(requireContext())

        val completeButton: Button = view.findViewById(R.id.completeButton)
        val nameEditText: EditText = view.findViewById(R.id.nameEditText)
        val ageEditText: EditText = view.findViewById(R.id.ageEditText)
        val skinTypeChipGroup: ChipGroup = view.findViewById(R.id.skinTypeChipGroup)
        val preferencesChipGroup: ChipGroup = view.findViewById(R.id.preferencesChipGroup)
        val routinesChipGroup: ChipGroup = view.findViewById(R.id.routinesChipGroup)

        viewModel.createdUserId.observe(viewLifecycleOwner) { userId ->
            userId?.let {
                // SIMPAN ID FIREBASE KE HP (PENTING!)
                prefsHelper.saveString("KEY_FIREBASE_USER_ID", it)
            }
        }

        // 2. Pasang 'Telinga' untuk mendengar kabar dari ViewModel (Sukses/Gagal)
        viewModel.saveStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess == true) {
                // Kalau sukses simpan online, lanjut masuk aplikasi
                goToMainPage()
            } else {
                Toast.makeText(requireContext(), "Gagal simpan online, masuk mode offline.", Toast.LENGTH_SHORT).show()
                // Tetap lanjut masuk aplikasi (biar user ga stuck)
                goToMainPage()
            }
        }

        completeButton.setOnClickListener {
            // --- BAGIAN VALIDASI INPUT (Sama kayak dulu) ---
            val name = nameEditText.text.toString().trim()
            val age = ageEditText.text.toString().trim()

            if (name.isEmpty()) { Toast.makeText(requireContext(), "Isi nama dulu!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (age.isEmpty()) { Toast.makeText(requireContext(), "Isi umur!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            val selectedSkinTypeId = skinTypeChipGroup.checkedChipId
            if (selectedSkinTypeId == View.NO_ID) { Toast.makeText(requireContext(), "Pilih tipe kulit!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            val selectedPreferencesIds = preferencesChipGroup.checkedChipIds
            if (selectedPreferencesIds.isEmpty()) { Toast.makeText(requireContext(), "Pilih preferensi!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            val selectedRoutinesIds = routinesChipGroup.checkedChipIds
            if (selectedRoutinesIds.isEmpty()) { Toast.makeText(requireContext(), "Pilih rutinitas!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            // --- AMBIL DATA DARI CHIP ---
            val skinType = view.findViewById<Chip>(selectedSkinTypeId).text.toString()
            val preferences = selectedPreferencesIds.joinToString(", ") { id -> view.findViewById<Chip>(id).text.toString() }
            val routines = selectedRoutinesIds.joinToString(", ") { id -> view.findViewById<Chip>(id).text.toString() }

            // 3. SIMPAN KE HP (Wajib biar aplikasi 'ingat' user)
            prefsHelper.saveString(PreferencesHelper.KEY_USER_NAME, name)
            prefsHelper.saveString(PreferencesHelper.KEY_USER_AGE, age)
            prefsHelper.saveString(PreferencesHelper.KEY_SKIN_TYPE, skinType)
            prefsHelper.saveString(PreferencesHelper.KEY_PREFERENCES, preferences)
            prefsHelper.saveString(PreferencesHelper.KEY_ROUTINES, routines)

            // Kunci Rahasia: Set ini jadi TRUE biar besok2 gak perlu isi data lagi
            prefsHelper.saveBoolean(PreferencesHelper.KEY_ONBOARDING_COMPLETED, true)

            // 4. SIMPAN KE FIREBASE (Syarat Nilai)
            val userUntukCloud = User(
                name = name,
                age = age,
                skinType = skinType,
                preferences = preferences,
                routines = routines
            )
            viewModel.saveUserProfile(userUntukCloud) // Kirim!
        }
    }

    private fun goToMainPage() {
        val bundle = bundleOf("show_snackbar" to true)
        findNavController().navigate(R.id.action_profileSetupFragment_to_mainFragment, bundle)
    }
}