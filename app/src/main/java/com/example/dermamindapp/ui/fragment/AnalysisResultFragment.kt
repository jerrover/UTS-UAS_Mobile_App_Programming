package com.example.dermamindapp.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider // Import ViewModel
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.SkinAnalysis
import com.example.dermamindapp.ui.viewmodel.AnalysisViewModel // Import ViewModel baru
import com.google.android.material.card.MaterialCardView

class AnalysisResultFragment : Fragment() {

    companion object {
        const val ARG_DESTINATION_ID = "destination_id"
    }

    // Ganti DatabaseHelper dengan ViewModel
    private lateinit var viewModel: AnalysisViewModel

    private val args: AnalysisResultFragmentArgs by navArgs()

    private lateinit var imageUri: String
    private lateinit var analysisResult: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis_result, container, false)

        // 1. Inisialisasi ViewModel
        viewModel = ViewModelProvider(this)[AnalysisViewModel::class.java]

        imageUri = args.imageUri
        analysisResult = args.analysisResults

        val analysisImageView: ImageView = view.findViewById(R.id.imagePlaceholder)
        val seeRecommendationsButton: Button = view.findViewById(R.id.recommendationsButton)

        Glide.with(this)
            .load(Uri.parse(imageUri))
            .into(analysisImageView)

        // Setup kartu hasil (Visual)
        setupResultCards(view)

        // 2. Setup Tombol Simpan
        seeRecommendationsButton.setOnClickListener {
            saveAnalysisToDatabase()
        }

        // 3. Pantau status simpan dari ViewModel
        viewModel.saveStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess == true) {
                Toast.makeText(requireContext(), "Hasil tersimpan di Cloud!", Toast.LENGTH_SHORT).show()

                // Navigasi ke halaman rekomendasi
                val bundle = bundleOf(ARG_DESTINATION_ID to R.id.productRecommendationFragment)
                requireActivity().findNavController(R.id.nav_host_fragment)
                    .navigate(R.id.action_analysisResultFragment_to_mainFragment, bundle)

                viewModel.resetSaveStatus() // Reset status
            } else if (isSuccess == false) {
                Toast.makeText(requireContext(), "Gagal menyimpan data.", Toast.LENGTH_SHORT).show()
                viewModel.resetSaveStatus()
            }
        }

        return view
    }

    private fun setupResultCards(view: View) {
        val cardLayout: ViewGroup = view.findViewById(R.id.analysisCards)
        val resultsList = analysisResult.split(", ").map { it.trim() }

        val card1 = cardLayout.getChildAt(0) as? MaterialCardView
        card1?.let {
            val title = it.findViewById<TextView>(R.id.analysis_card_acne_title)
            val desc = it.findViewById<TextView>(R.id.analysis_card_acne_description)
            if (resultsList.isNotEmpty()) {
                title.text = resultsList[0]
                desc.text = getDescriptionFor(resultsList[0])
            }
        }

        val card2 = cardLayout.getChildAt(1) as? MaterialCardView
        card2?.let {
            val title = it.findViewById<TextView>(R.id.analysis_card_finelines_title)
            val desc = it.findViewById<TextView>(R.id.analysis_card_finelines_description)
            if (resultsList.size > 1) {
                title.text = resultsList[1]
                desc.text = getDescriptionFor(resultsList[1])
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }

    private fun getDescriptionFor(condition: String): String {
        return when (condition) {
            "Jerawat_Aktif" -> "Terdeteksi adanya jerawat yang meradang..."
            "Kemerahan" -> "Kulit menunjukkan area kemerahan atau iritasi..."
            "Kulit_Berminyak" -> "Terdeteksi produksi sebum berlebih..."
            "Tekstur_Pori_pori" -> "Pori-pori tampak membesar atau tekstur tidak merata..."
            "Kulit_Sehat" -> "Kulit Anda tampak sehat dan terhidrasi!"
            else -> "Kondisi kulit terdeteksi."
        }
    }

    private fun saveAnalysisToDatabase() {
        val timestamp = System.currentTimeMillis()

        // Ambil ID User yang lagi login
        // (Pastikan kamu sudah inisialisasi prefsHelper)
        val prefsHelper = com.example.dermamindapp.data.PreferencesHelper(requireContext())
        val currentUserId = prefsHelper.getString("KEY_FIREBASE_USER_ID") ?: "unknown_user"

        val analysis = SkinAnalysis(
            userId = currentUserId, // <--- MASUKKAN ID SINI
            date = timestamp,
            imageUri = imageUri,
            result = analysisResult,
            notes = ""
        )

        viewModel.saveAnalysis(analysis)
    }
}