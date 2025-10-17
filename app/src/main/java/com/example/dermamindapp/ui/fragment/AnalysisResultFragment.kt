package com.example.dermamindapp.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.db.DatabaseHelper
import com.example.dermamindapp.data.model.SkinAnalysis

class AnalysisResultFragment : Fragment() {

    companion object {
        const val ARG_DESTINATION_ID = "destination_id"
    }

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis_result, container, false)

        dbHelper = DatabaseHelper(requireContext())

        // --- PEMBARUAN DI SINI ---
        // Menampilkan gambar scan pada halaman hasil analisis
        val analysisImageView: ImageView = view.findViewById(R.id.imagePlaceholder)
        Glide.with(this)
            .load(R.drawable.scan)
            .into(analysisImageView)
        // --- AKHIR PEMBARUAN ---

        val seeRecommendationsButton: Button = view.findViewById(R.id.recommendationsButton)
        seeRecommendationsButton.setOnClickListener {
            saveAnalysisToDatabase()
            val bundle = bundleOf(ARG_DESTINATION_ID to R.id.productRecommendationFragment)
            requireActivity().findNavController(R.id.nav_host_fragment)
                .navigate(R.id.action_analysisResultFragment_to_mainFragment, bundle)
        }

        return view
    }

    private fun saveAnalysisToDatabase() {
        val imageUri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.scan}").toString()

        val result = "Acne Scars, Early Fine Lines"
        val timestamp = System.currentTimeMillis()

        val analysis = SkinAnalysis(
            id = 0,
            date = timestamp,
            imageUri = imageUri,
            result = result,
            notes = ""
        )

        dbHelper.addAnalysis(analysis)
        Toast.makeText(requireContext(), "Analysis saved to your journey!", Toast.LENGTH_SHORT).show()
    }
}
