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

// Fragment ini menampilkan hasil dari analisis kulit yang telah dilakukan.
class AnalysisResultFragment : Fragment() {

    // Companion object untuk menyimpan argumen kunci yang akan dikirimkan
    // melalui navigation component.
    companion object {
        const val ARG_DESTINATION_ID = "destination_id"
    }

    // Inisialisasi helper untuk berinteraksi dengan database.
    private lateinit var dbHelper: DatabaseHelper

    // Membuat dan mengembalikan tampilan (view) untuk fragment.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis_result, container, false)

        // Inisialisasi DatabaseHelper.
        dbHelper = DatabaseHelper(requireContext())

        // Menampilkan gambar hasil scan pada halaman ini.
        val analysisImageView: ImageView = view.findViewById(R.id.imagePlaceholder)
        Glide.with(this)
            .load(R.drawable.scan)
            .into(analysisImageView)

        // Menangani klik pada tombol untuk melihat rekomendasi produk.
        val seeRecommendationsButton: Button = view.findViewById(R.id.recommendationsButton)
        seeRecommendationsButton.setOnClickListener {
            // Menyimpan hasil analisis ke database sebelum navigasi.
            saveAnalysisToDatabase()
            // Membuat bundle untuk mengirim argumen ke fragment tujuan.
            val bundle = bundleOf(ARG_DESTINATION_ID to R.id.productRecommendationFragment)
            // Navigasi ke MainFragment dengan argumen untuk membuka tab rekomendasi produk.
            requireActivity().findNavController(R.id.nav_host_fragment)
                .navigate(R.id.action_analysisResultFragment_to_mainFragment, bundle)
        }

        return view
    }

    // Fungsi untuk menyimpan data analisis ke dalam database SQLite.
    private fun saveAnalysisToDatabase() {
        // Mendapatkan URI dari gambar statis sebagai placeholder.
        val imageUri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.scan}").toString()

        // Data hasil analisis (saat ini masih statis).
        val result = "Acne Scars, Early Fine Lines"
        val timestamp = System.currentTimeMillis()

        // Membuat objek SkinAnalysis untuk disimpan.
        val analysis = SkinAnalysis(
            id = 0, // ID akan di-generate otomatis oleh database.
            date = timestamp,
            imageUri = imageUri,
            result = result,
            notes = "" // Catatan awal kosong.
        )

        // Memanggil fungsi dari DatabaseHelper untuk menambahkan data.
        dbHelper.addAnalysis(analysis)
        // Menampilkan pesan konfirmasi kepada pengguna.
        Toast.makeText(requireContext(), "Analysis saved to your journey!", Toast.LENGTH_SHORT).show()
    }
}