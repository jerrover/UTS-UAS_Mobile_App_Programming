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

class ProfileFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvSkinType: TextView
    private lateinit var tvPreferences: TextView
    private lateinit var tvRoutines: TextView
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

        // Initialize TextViews from layout
        tvUserName = view.findViewById(R.id.profile_name)
        tvSkinType = view.findViewById(R.id.tvSkinTypeValue)
        tvPreferences = view.findViewById(R.id.tvPreferencesValue)
        tvRoutines = view.findViewById(R.id.tvRoutinesValue)

        // Load and display data
        loadProfileData()

        val logoutButton: Button = view.findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun loadProfileData() {
        val userName = prefsHelper.getString(PreferencesHelper.KEY_USER_NAME)
        val skinType = prefsHelper.getString(PreferencesHelper.KEY_SKIN_TYPE)
        val preferences = prefsHelper.getString(PreferencesHelper.KEY_PREFERENCES)
        val routines = prefsHelper.getString(PreferencesHelper.KEY_ROUTINES)

        tvUserName.text = userName ?: "User"
        tvSkinType.text = skinType ?: "Not set"
        tvPreferences.text = preferences ?: "Not set"
        tvRoutines.text = routines ?: "Not set"
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.logout_dialog_title))
            .setMessage(getString(R.string.logout_dialog_message))
            .setNegativeButton(getString(R.string.dialog_no)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.dialog_yes)) { _, _ ->
                // Clear data on logout
                prefsHelper.clear()

                Toast.makeText(requireContext(), getString(R.string.feedback_logged_out), Toast.LENGTH_SHORT).show()
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.app_nav, true)
                    .build()
                requireActivity().findNavController(R.id.nav_host_fragment)
                    .navigate(R.id.onboardingFragment, null, navOptions)
            }
            .show()
    }
}