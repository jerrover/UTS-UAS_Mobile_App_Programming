package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.ui.viewmodel.JourneyViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class SkinDetailFragment : Fragment() {

    private val args by navArgs<SkinDetailFragmentArgs>()
    private lateinit var viewModel: JourneyViewModel

    private lateinit var tvDate: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvNotes: TextView
    private lateinit var ivAnalysis: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_skin_detail, container, false)

        viewModel = ViewModelProvider(this)[JourneyViewModel::class.java]

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        toolbar.inflateMenu(R.menu.detail_menu)
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.menu_delete) {
                deleteAnalysis()
            }
            true
        }

        tvDate = view.findViewById(R.id.tvDateDetail)
        tvResult = view.findViewById(R.id.tvResultDetail)
        tvNotes = view.findViewById(R.id.tvNotesDetail)
        ivAnalysis = view.findViewById(R.id.ivAnalysisDetail)

        // Button Deklarasi
        val btnEditNotes: MaterialButton = view.findViewById(R.id.btnEditNotes)
        val btnRecommendation: MaterialButton = view.findViewById(R.id.btnRecommendation) // Tombol baru

        populateUI()

        // 1. Logic Tombol Edit Notes (Tetap sama)
        btnEditNotes.setOnClickListener {
            showEditNotesDialog()
        }

        // 2. Logic Tombol Rekomendasi (BARU)
        btnRecommendation.setOnClickListener {
            val analysisResultString = args.currentAnalysis.result

            // Pindah ke ProductRecommendationFragment bawa hasil analisisnya
            val action = SkinDetailFragmentDirections
                .actionSkinDetailFragmentToProductRecommendationFragment(analysisResultString)
            findNavController().navigate(action)
        }

        viewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearStatusMessage()
                if (it.contains("dihapus", ignoreCase = true)) {
                    findNavController().popBackStack()
                }
            }
        }

        return view
    }

    // ... (Sisa kode populateUI, showEditNotesDialog, deleteAnalysis biarkan sama seperti sebelumnya) ...

    private fun populateUI() {
        tvDate.text = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(args.currentAnalysis.date))
        tvResult.text = args.currentAnalysis.result
        tvNotes.text = args.currentAnalysis.notes?.ifEmpty { "No notes added." }
        Glide.with(this).load(args.currentAnalysis.imageUri).into(ivAnalysis)
    }

    private fun showEditNotesDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_notes, null)
        val editText = dialogView.findViewById<EditText>(R.id.etNotes)
        editText.setText(args.currentAnalysis.notes)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Notes")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedNotes = editText.text.toString()
                viewModel.updateNotes(args.currentAnalysis.id, updatedNotes)
                args.currentAnalysis.notes = updatedNotes
                populateUI()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAnalysis() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Analysis")
            .setMessage("Are you sure you want to delete this history?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAnalysis(args.currentAnalysis.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}