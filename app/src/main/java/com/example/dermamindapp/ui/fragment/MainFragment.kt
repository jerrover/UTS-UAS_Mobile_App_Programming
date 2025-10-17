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

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        val destinationId = arguments?.getInt(AnalysisResultFragment.ARG_DESTINATION_ID, 0) ?: 0
        if (destinationId != 0) {
            bottomNav.selectedItemId = destinationId
        }

        bottomNav.setOnItemSelectedListener { item ->
            navController.navigate(item.itemId)
            true
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val showSnackbar = arguments?.getBoolean("show_snackbar") ?: false
        if (showSnackbar) {
            Snackbar.make(view, getString(R.string.feedback_profile_saved), Snackbar.LENGTH_SHORT).show()
            arguments?.remove("show_snackbar")
        }
    }
}

