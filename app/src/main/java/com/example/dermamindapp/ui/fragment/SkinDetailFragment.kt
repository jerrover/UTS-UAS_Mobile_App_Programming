package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope // Penting: Import ini untuk Coroutine
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.db.DatabaseHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch // Penting: Import ini untuk launch
import java.text.SimpleDateFormat
import java.util.*

class SkinDetailFragment : Fragment() {

    private val args by navArgs<SkinDetailFragmentArgs>()
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var tvDate: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvNotes: TextView
    private lateinit var ivAnalysis: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_skin_detail, container, false)

        dbHelper = DatabaseHelper(requireContext())

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
        val btnEditNotes: MaterialButton = view.findViewById(R.id.btnEditNotes)

        populateUI()

        btnEditNotes.setOnClickListener {
            showEditNotesDialog()
        }

        return view
    }

    private fun populateUI() {
        // Konversi tanggal dari Long ke format String yang mudah dibaca
        tvDate.text = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(args.currentAnalysis.date))
        tvResult.text = args.currentAnalysis.result
        tvNotes.text = args.currentAnalysis.notes?.ifEmpty { "No notes added." }

        // Load gambar
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

                // PERBAIKAN: Gunakan Coroutine untuk Update data ke Firebase
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        dbHelper.updateNotes(args.currentAnalysis.id, updatedNotes)

                        // Update tampilan UI setelah sukses simpan di database
                        args.currentAnalysis.notes = updatedNotes
                        populateUI()
                        Snackbar.make(requireView(), "Notes updated!", Snackbar.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Gagal update notes: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAnalysis() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Analysis")
            .setMessage("Are you sure you want to delete this history?")
            .setPositiveButton("Delete") { _, _ ->

                // PERBAIKAN: Gunakan Coroutine untuk Delete data di Firebase
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        dbHelper.deleteAnalysis(args.currentAnalysis.id)

                        Snackbar.make(requireActivity().findViewById(android.R.id.content), "Successfully deleted.", Snackbar.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Gagal menghapus data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}