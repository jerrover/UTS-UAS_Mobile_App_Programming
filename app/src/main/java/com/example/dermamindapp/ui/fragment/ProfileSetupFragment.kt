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
import androidx.navigation.fragment.findNavController
import com.example.dermamindapp.R
import com.example.dermamindapp.data.PreferencesHelper
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ProfileSetupFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val completeButton: Button = view.findViewById(R.id.completeButton)
        val nameEditText: EditText = view.findViewById(R.id.nameEditText)
        val ageEditText: EditText = view.findViewById(R.id.ageEditText)
        val skinTypeChipGroup: ChipGroup = view.findViewById(R.id.skinTypeChipGroup)
        val preferencesChipGroup: ChipGroup = view.findViewById(R.id.preferencesChipGroup)
        val routinesChipGroup: ChipGroup = view.findViewById(R.id.routinesChipGroup)
        val prefsHelper = PreferencesHelper(requireContext())

        completeButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val age = ageEditText.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your name.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (age.isEmpty() || age.toIntOrNull() ?: 0 <= 0) {
                Toast.makeText(requireContext(), "Please enter a valid age.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedSkinTypeId = skinTypeChipGroup.checkedChipId
            if (selectedSkinTypeId == View.NO_ID) {
                Toast.makeText(requireContext(), getString(R.string.validation_skin_type_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedPreferencesIds = preferencesChipGroup.checkedChipIds
            if (selectedPreferencesIds.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.validation_preferences_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRoutinesIds = routinesChipGroup.checkedChipIds
            if (selectedRoutinesIds.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one routine.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simpan Data Pengguna
            prefsHelper.saveString(PreferencesHelper.KEY_USER_NAME, name)
            prefsHelper.saveString(PreferencesHelper.KEY_USER_AGE, age)

            val selectedSkinTypeChip: Chip = view.findViewById(selectedSkinTypeId)
            prefsHelper.saveString(PreferencesHelper.KEY_SKIN_TYPE, selectedSkinTypeChip.text.toString())

            val selectedPreferences = selectedPreferencesIds.joinToString(", ") { id ->
                view.findViewById<Chip>(id).text.toString()
            }
            prefsHelper.saveString(PreferencesHelper.KEY_PREFERENCES, selectedPreferences)

            val selectedRoutinesTexts = selectedRoutinesIds.map { id ->
                view.findViewById<Chip>(id).text.toString()
            }

            val routinesText = when {
                selectedRoutinesTexts.size > 2 -> {
                    val lastItem = selectedRoutinesTexts.last()
                    val otherItems = selectedRoutinesTexts.dropLast(1).joinToString(", ")
                    "$otherItems, & $lastItem"
                }
                selectedRoutinesTexts.size == 2 -> {
                    selectedRoutinesTexts.joinToString(" & ")
                }
                else -> {
                    selectedRoutinesTexts.joinToString("")
                }
            }
            prefsHelper.saveString(PreferencesHelper.KEY_ROUTINES, routinesText)

            prefsHelper.saveBoolean(PreferencesHelper.KEY_ONBOARDING_COMPLETED, true)

            val bundle = bundleOf("show_snackbar" to true)
            findNavController().navigate(R.id.action_profileSetupFragment_to_mainFragment, bundle)
        }
    }
}