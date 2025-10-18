// Fragment untuk menampilkan detail, mengedit, dan menghapus data.
package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.db.DatabaseHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

// Fragment ini menampilkan detail dari satu entri riwayat analisis kulit.
class SkinDetailFragment : Fragment() {

    // Mengambil argumen (data analisis) yang dikirimkan melalui Navigasi Komponen.
    private val args by navArgs<SkinDetailFragmentArgs>()
    // Helper untuk interaksi dengan database.
    private lateinit var dbHelper: DatabaseHelper

    // Komponen UI untuk menampilkan detail.
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

        // Mengatur toolbar dengan tombol kembali dan menu hapus.
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        toolbar.inflateMenu(R.menu.detail_menu)
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.menu_delete) {
                deleteAnalysis()
            }
            true
        }

        // Inisialisasi komponen UI.
        tvDate = view.findViewById(R.id.tvDateDetail)
        tvResult = view.findViewById(R.id.tvResultDetail)
        tvNotes = view.findViewById(R.id.tvNotesDetail)
        ivAnalysis = view.findViewById(R.id.ivAnalysisDetail)
        val btnEditNotes: MaterialButton = view.findViewById(R.id.btnEditNotes)

        // Mengisi UI dengan data dari argumen navigasi.
        populateUI()

        // Menangani aksi klik pada tombol "Edit Notes".
        btnEditNotes.setOnClickListener {
            showEditNotesDialog()
        }

        return view
    }

    // Mengisi komponen UI dengan data analisis yang diterima.
    private fun populateUI() {
        tvDate.text = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(args.currentAnalysis.date))
        tvResult.text = args.currentAnalysis.result
        tvNotes.text = args.currentAnalysis.notes?.ifEmpty { "No notes added." }
        Glide.with(this).load(args.currentAnalysis.imageUri).into(ivAnalysis)
    }

    // Menampilkan dialog untuk mengedit catatan.
    private fun showEditNotesDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_notes, null)
        val editText = dialogView.findViewById<EditText>(R.id.etNotes)
        editText.setText(args.currentAnalysis.notes)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Notes")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedNotes = editText.text.toString()
                // Memperbarui catatan di database.
                dbHelper.updateNotes(args.currentAnalysis.id, updatedNotes)
                args.currentAnalysis.notes = updatedNotes // Memperbarui objek di memori.
                populateUI() // Memuat ulang UI dengan data baru.
                Snackbar.make(requireView(), "Notes updated!", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Menampilkan dialog konfirmasi dan menghapus data analisis jika dikonfirmasi.
    private fun deleteAnalysis() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Analysis")
            .setMessage("Are you sure you want to delete this history?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteAnalysis(args.currentAnalysis.id)
                Snackbar.make(requireActivity().findViewById(android.R.id.content), "Successfully deleted.", Snackbar.LENGTH_SHORT).show()
                findNavController().popBackStack() // Kembali ke layar sebelumnya setelah hapus.
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}