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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.ui.viewmodel.AnalysisViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText

class AnalysisResultFragment : Fragment() {

    private lateinit var viewModel: AnalysisViewModel
    private lateinit var imageUri: String
    private lateinit var analysisResult: String
    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var etNotes: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis_result, container, false)

        prefsHelper = PreferencesHelper(requireContext())
        viewModel = ViewModelProvider(this)[AnalysisViewModel::class.java]

        imageUri = arguments?.getString("imageUri") ?: ""
        analysisResult = arguments?.getString("analysisResults") ?: ""

        val analysisImageView: ImageView = view.findViewById(R.id.imagePlaceholder)
        val seeRecommendationsButton: Button = view.findViewById(R.id.recommendationsButton)
        val retakeButton: Button = view.findViewById(R.id.btnRetake)
        etNotes = view.findViewById(R.id.etNotes)

        // Load gambar
        try {
            if (imageUri.isNotEmpty()) {
                Glide.with(this)
                    .load(Uri.parse(imageUri))
                    .into(analysisImageView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setupResultCards(view)

        // 1. Logic Tombol Foto Ulang
        retakeButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // 2. Logic Tombol Simpan
        seeRecommendationsButton.setOnClickListener {
            saveAnalysisToCloud()
        }

        setupObservers(seeRecommendationsButton, retakeButton)

        return view
    }

    private fun setupObservers(saveButton: Button, retakeButton: Button) {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            saveButton.isEnabled = !isLoading
            retakeButton.isEnabled = !isLoading
            saveButton.text = if (isLoading) "Mengupload..." else "Simpan & Rekomendasi"
        }

        viewModel.statusMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        // --- BAGIAN INI YANG DIPERBAIKI ---
        viewModel.saveStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess == true) {
                Toast.makeText(requireContext(), "Hasil tersimpan di Cloud!", Toast.LENGTH_SHORT).show()

                try {
                    // KITA GUNAKAN CARA SAFE ARGS (Direkomendasikan)
                    // Pastikan Anda sudah Rebuild Project agar class ini muncul
                    val action = AnalysisResultFragmentDirections
                        .actionAnalysisResultFragmentToAnalysisRecommendationFragment(analysisResult)
                    findNavController().navigate(action)

                } catch (e: Exception) {
                    // Jika Safe Args error (karena belum rebuild), pakai cara manual sebagai cadangan:
                    val bundle = Bundle().apply {
                        putString("analysisResult", analysisResult)
                    }
                    // Pastikan ID ini ada di main_nav.xml (action yang baru dibuat)
                    findNavController().navigate(R.id.action_analysisResultFragment_to_analysisRecommendationFragment, bundle)
                }

                viewModel.resetSaveStatus()
            }
        }
    }

    private fun saveAnalysisToCloud() {
        val currentUserId = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)
        val notesText = etNotes.text.toString().trim()

        if (currentUserId.isNullOrEmpty()) {
            com.example.dermamindapp.data.db.DatabaseHelper(requireContext())
            val retryId = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)
            if (retryId.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Error User ID. Silakan restart aplikasi.", Toast.LENGTH_LONG).show()
                return
            }
            viewModel.uploadAndSaveAnalysis(imageUri, analysisResult, retryId, notesText)
        } else {
            viewModel.uploadAndSaveAnalysis(imageUri, analysisResult, currentUserId, notesText)
        }
    }

    private fun setupResultCards(view: View) {
        val cardLayout: ViewGroup = view.findViewById(R.id.analysisCards)
        if (analysisResult.isEmpty()) return

        val resultsList = analysisResult.split(", ").map { it.trim() }

        val card1 = cardLayout.getChildAt(0) as? MaterialCardView
        card1?.let {
            val title = it.findViewById<TextView>(R.id.analysis_card_acne_title)
            val desc = it.findViewById<TextView>(R.id.analysis_card_acne_description)
            if (resultsList.isNotEmpty()) {
                val condition = resultsList[0]
                title.text = condition.replace("_", " ")
                desc.text = getDescriptionFor(condition)
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }

        val card2 = cardLayout.getChildAt(1) as? MaterialCardView
        card2?.let {
            val title = it.findViewById<TextView>(R.id.analysis_card_finelines_title)
            val desc = it.findViewById<TextView>(R.id.analysis_card_finelines_description)
            if (resultsList.size > 1) {
                val condition = resultsList[1]
                title.text = condition.replace("_", " ")
                desc.text = getDescriptionFor(condition)
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }
    }

    private fun getDescriptionFor(condition: String): String {
        return when (condition) {
            "Jerawat_Aktif" -> "Terdeteksi jerawat meradang."
            "Kemerahan" -> "Tanda iritasi/sensitif."
            "Kulit_Berminyak" -> "Produksi sebum berlebih."
            "Tekstur_Pori_pori" -> "Pori-pori tampak besar."
            "Kulit_Sehat" -> "Kulit tampak sehat."
            else -> "Kondisi kulit terdeteksi."
        }
    }
}