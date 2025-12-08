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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.ui.viewmodel.AnalysisViewModel
import com.google.android.material.card.MaterialCardView

class AnalysisResultFragment : Fragment() {

    companion object {
        const val ARG_DESTINATION_ID = "destination_id"
    }

    private lateinit var viewModel: AnalysisViewModel
    private val args: AnalysisResultFragmentArgs by navArgs()
    private lateinit var imageUri: String
    private lateinit var analysisResult: String
    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis_result, container, false)

        // 1. Inisialisasi
        prefsHelper = PreferencesHelper(requireContext())
        viewModel = ViewModelProvider(this)[AnalysisViewModel::class.java]

        imageUri = args.imageUri
        analysisResult = args.analysisResults

        val analysisImageView: ImageView = view.findViewById(R.id.imagePlaceholder)
        val seeRecommendationsButton: Button = view.findViewById(R.id.recommendationsButton)

        // Load gambar
        try {
            Glide.with(this)
                .load(Uri.parse(imageUri))
                .into(analysisImageView)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setupResultCards(view)

        // 2. Setup Tombol Simpan
        seeRecommendationsButton.setOnClickListener {
            saveAnalysisToCloud()
        }

        setupObservers(seeRecommendationsButton)

        return view
    }

    private fun setupObservers(button: Button) {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            button.isEnabled = !isLoading
            button.text = if (isLoading) "Mengupload..." else "Lihat Rekomendasi"
        }

        viewModel.statusMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess == true) {
                Toast.makeText(requireContext(), "Hasil tersimpan di Cloud!", Toast.LENGTH_SHORT).show()
                try {
                    val bundle = bundleOf(ARG_DESTINATION_ID to R.id.productRecommendationFragment)
                    requireActivity().findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.action_analysisResultFragment_to_mainFragment, bundle)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Navigasi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                viewModel.resetSaveStatus()
            }
        }
    }

    private fun saveAnalysisToCloud() {
        // PERBAIKAN: Gunakan konstanta KEY_USER_ID yang benar
        val currentUserId = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)

        if (currentUserId.isNullOrEmpty()) {
            // Jika masih null, pancing inisialisasi DatabaseHelper untuk membuat ID baru
            com.example.dermamindapp.data.db.DatabaseHelper(requireContext())

            // Coba ambil lagi
            val retryId = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)
            if (retryId.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Gagal: User ID error. Coba restart aplikasi.", Toast.LENGTH_LONG).show()
                return
            }
            viewModel.uploadAndSaveAnalysis(imageUri, analysisResult, retryId)
        } else {
            viewModel.uploadAndSaveAnalysis(imageUri, analysisResult, currentUserId)
        }
    }

    private fun setupResultCards(view: View) {
        val cardLayout: ViewGroup = view.findViewById(R.id.analysisCards)
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
            "Jerawat_Aktif" -> "Terdeteksi adanya jerawat yang meradang. Disarankan menggunakan bahan aktif seperti Salicylic Acid."
            "Kemerahan" -> "Kulit menunjukkan tanda iritasi atau sensitif. Gunakan produk yang menenangkan seperti Centella Asiatica."
            "Kulit_Berminyak" -> "Produksi sebum berlebih terdeteksi. Gunakan pembersih wajah yang lembut dan oil-free."
            "Tekstur_Pori_pori" -> "Pori-pori tampak membesar. Eksfoliasi rutin (AHA/BHA) dapat membantu menghaluskan tekstur."
            "Kulit_Sehat" -> "Selamat! Kulit Anda tampak sehat dan terawat. Pertahankan rutinitas Anda."
            else -> "Kondisi kulit terdeteksi. Konsultasikan dengan ahli jika perlu."
        }
    }
}