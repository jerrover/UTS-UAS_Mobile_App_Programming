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
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs // Import navArgs
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.db.DatabaseHelper
import com.example.dermamindapp.data.model.SkinAnalysis
import com.google.android.material.card.MaterialCardView

class AnalysisResultFragment : Fragment() {

    companion object {
        const val ARG_DESTINATION_ID = "destination_id"
    }

    private lateinit var dbHelper: DatabaseHelper

    // Gunakan navArgs untuk mendapatkan data yang dikirim
    private val args: AnalysisResultFragmentArgs by navArgs()

    private lateinit var imageUri: String
    private lateinit var analysisResult: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis_result, container, false)
        dbHelper = DatabaseHelper(requireContext())

        // Ambil data dari argumen
        imageUri = args.imageUri
        analysisResult = args.analysisResults

        val analysisImageView: ImageView = view.findViewById(R.id.imagePlaceholder)
        val seeRecommendationsButton: Button = view.findViewById(R.id.recommendationsButton)

        // Tampilkan gambar yang baru diambil/dipilih
        Glide.with(this)
            .load(Uri.parse(imageUri))
            .into(analysisImageView)

        // --- (Opsional) Tampilkan hasil dinamis di kartu ---
        // (Ini mengasumsikan Anda ingin memperbarui teks di kartu-kartu statis Anda)
        val cardLayout: ViewGroup = view.findViewById(R.id.analysisCards)
        val resultsList = analysisResult.split(", ").map { it.trim() }

        // Mengisi kartu pertama
        val card1 = cardLayout.getChildAt(0) as? MaterialCardView
        card1?.let {
            val title = it.findViewById<TextView>(R.id.analysis_card_acne_title) // ID dari XML Anda
            val desc = it.findViewById<TextView>(R.id.analysis_card_acne_description)
            if (resultsList.isNotEmpty()) {
                title.text = resultsList[0]
                desc.text = getDescriptionFor(resultsList[0]) // Buat fungsi helper untuk ini
            }
        }

        // Mengisi kartu kedua
        val card2 = cardLayout.getChildAt(1) as? MaterialCardView
        card2?.let {
            val title = it.findViewById<TextView>(R.id.analysis_card_finelines_title) // ID dari XML Anda
            val desc = it.findViewById<TextView>(R.id.analysis_card_finelines_description)
            if (resultsList.size > 1) {
                title.text = resultsList[1]
                desc.text = getDescriptionFor(resultsList[1])
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE // Sembunyikan jika hanya ada 1 hasil
            }
        }
        // --- Akhir bagian opsional ---


        seeRecommendationsButton.setOnClickListener {
            // Simpan hasil analisis ke database SEBELUM navigasi
            saveAnalysisToDatabase()

            val bundle = bundleOf(ARG_DESTINATION_ID to R.id.productRecommendationFragment)
            requireActivity().findNavController(R.id.nav_host_fragment)
                .navigate(R.id.action_analysisResultFragment_to_mainFragment, bundle)
        }

        return view
    }

    // Fungsi helper untuk deskripsi (bisa Anda kembangkan)
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

    // Fungsi ini SEKARANG MENGGUNAKAN data dari argumen
    private fun saveAnalysisToDatabase() {
        val timestamp = System.currentTimeMillis()

        val analysis = SkinAnalysis(
            id = 0,
            date = timestamp,
            imageUri = imageUri, // Menggunakan URI dari argumen
            result = analysisResult, // Menggunakan hasil dari argumen
            notes = ""
        )

        dbHelper.addAnalysis(analysis)
        Toast.makeText(requireContext(), "Hasil analisis disimpan!", Toast.LENGTH_SHORT).show()
    }
}