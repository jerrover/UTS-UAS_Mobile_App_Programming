package com.example.dermamindapp.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.ui.adapter.AnalysisItem
import com.example.dermamindapp.ui.adapter.AnalysisResultAdapter
import com.example.dermamindapp.ui.viewmodel.AnalysisViewModel
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class AnalysisResultFragment : Fragment() {

    private lateinit var viewModel: AnalysisViewModel
    private lateinit var imageUri: String
    private lateinit var analysisScoresJson: String
    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var etNotes: TextInputEditText
    private lateinit var rvResults: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_analysis_result, container, false)

        prefsHelper = PreferencesHelper(requireContext())
        viewModel = ViewModelProvider(this)[AnalysisViewModel::class.java]

        imageUri = arguments?.getString("imageUri") ?: ""
        analysisScoresJson = arguments?.getString("analysisResults") ?: "{}"

        val analysisImageView: ImageView = view.findViewById(R.id.imagePlaceholder)
        val saveButton: Button = view.findViewById(R.id.recommendationsButton)
        val retakeButton: Button = view.findViewById(R.id.btnRetake)
        etNotes = view.findViewById(R.id.etNotes)
        rvResults = view.findViewById(R.id.rvAnalysisResults)

        if (imageUri.isNotEmpty()) {
            Glide.with(this).load(Uri.parse(imageUri)).into(analysisImageView)
        }

        setupModernResultList()

        retakeButton.setOnClickListener { findNavController().popBackStack() }
        saveButton.setOnClickListener { saveAnalysisToCloud() }

        setupObservers(saveButton, retakeButton)

        return view
    }

    private fun setupModernResultList() {
        val items = ArrayList<AnalysisItem>()

        try {
            // Parsing JSON String menjadi List Item
            val jsonObject = JSONObject(analysisScoresJson)
            val keys = jsonObject.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                val score = jsonObject.getDouble(key).toFloat()
                items.add(AnalysisItem(key, score))
            }
        } catch (e: Exception) {
            // Error handling
        }

        // Urutkan skor tertinggi di atas
        items.sortByDescending { it.score }

        rvResults.layoutManager = LinearLayoutManager(requireContext())
        rvResults.adapter = AnalysisResultAdapter(items)
        rvResults.isNestedScrollingEnabled = false
    }

    private fun setupObservers(saveButton: Button, retakeButton: Button) {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            saveButton.isEnabled = !isLoading
            retakeButton.isEnabled = !isLoading
            saveButton.text = if (isLoading) "Mengupload..." else "Simpan Hasil"
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess == true) {
                Toast.makeText(requireContext(), "Data tersimpan!", Toast.LENGTH_SHORT).show()
                try {
                    val action = AnalysisResultFragmentDirections
                        .actionAnalysisResultFragmentToAnalysisRecommendationFragment(analysisScoresJson)
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    // Fallback jika safeargs error, gunakan ID
                    val bundle = Bundle()
                    bundle.putString("analysisResults", analysisScoresJson)
                    findNavController().navigate(R.id.action_analysisResultFragment_to_analysisRecommendationFragment, bundle)
                }
                viewModel.resetSaveStatus()
            } else if (isSuccess == false) {
                // Saat terjadi kegagalan, pesan error sudah diurus oleh statusMessage observer
                viewModel.resetSaveStatus()
            }
        }

        // Menambahkan observer untuk pesan status, termasuk pesan error yang di-catch oleh ViewModel
        viewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            // Pastikan pesan yang ditampilkan adalah pesan error/informasi selain pesan loading awal.
            message?.let {
                if (it != "Mengupload foto...") {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveAnalysisToCloud() {
        val currentUserId = prefsHelper.getString(PreferencesHelper.KEY_USER_ID) ?: ""
        val notesText = etNotes.text.toString().trim()
        viewModel.uploadAndSaveAnalysis(imageUri, analysisScoresJson, currentUserId, notesText)
    }
}