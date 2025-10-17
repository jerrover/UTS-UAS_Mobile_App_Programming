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

class SplashFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        val prefsHelper = PreferencesHelper(requireContext())

        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded) {
                // Check if user has completed sign in
                if (prefsHelper.getString(PreferencesHelper.KEY_USER_NAME) != null) {
                    // Go to main page
                    findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
                } else {
                    // Start onboarding
                    findNavController().navigate(R.id.action_splashFragment_to_onboardingFragment)
                }
            }
        }, 2000)

        return view
    }
}