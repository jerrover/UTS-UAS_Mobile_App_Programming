package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermamindapp.R
import com.example.dermamindapp.data.db.DatabaseHelper
import com.example.dermamindapp.data.model.SkinAnalysis
import com.example.dermamindapp.ui.adapter.SkinJourneyAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

// Fragment ini menampilkan riwayat analisis kulit pengguna dalam sebuah daftar (RecyclerView).
class SkinJourneyFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: SkinJourneyAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoData: TextView // Teks yang muncul jika tidak ada data

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_skin_journey, container, false)

        // Inisialisasi helper dan komponen UI.
        dbHelper = DatabaseHelper(requireContext())
        recyclerView = view.findViewById(R.id.recyclerView)
        tvNoData = view.findViewById(R.id.tvNoData)

        // Mengatur adapter dan layout manager untuk RecyclerView.
        adapter = SkinJourneyAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Mengimplementasikan fungsionalitas swipe-to-delete pada item RecyclerView.
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false // Aksi drag-and-drop tidak diaktifkan.
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Menampilkan dialog konfirmasi sebelum menghapus item.
                    val analysisToDelete = adapter.getItemAt(position)
                    showDeleteConfirmationDialog(analysisToDelete)
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        return view
    }

    // Memuat ulang data setiap kali fragment ini ditampilkan.
    override fun onResume() {
        super.onResume()
        loadAnalyses()
    }

    // Memuat data riwayat analisis dari database dan menampilkannya di RecyclerView.
    private fun loadAnalyses() {
        val analyses = dbHelper.getAllAnalyses()
        if (analyses.isEmpty()) {
            // Jika tidak ada data, tampilkan pesan.
            recyclerView.visibility = View.GONE
            tvNoData.visibility = View.VISIBLE
        } else {
            // Jika ada data, tampilkan RecyclerView dan perbarui datanya.
            recyclerView.visibility = View.VISIBLE
            tvNoData.visibility = View.GONE
            adapter.setData(analyses)
        }
    }

    // Menampilkan dialog konfirmasi untuk menghapus item riwayat.
    private fun showDeleteConfirmationDialog(analysis: SkinAnalysis) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Analysis")
            .setMessage("Are you sure you want to delete this history?")
            .setNegativeButton("Cancel") { dialog, _ ->
                adapter.notifyDataSetChanged() // Mengembalikan item yang di-swipe ke posisi semula.
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { _, _ ->
                // Menghapus data dari database dan memuat ulang daftar.
                dbHelper.deleteAnalysis(analysis.id)
                loadAnalyses()
                Snackbar.make(requireView(), "Analysis deleted", Snackbar.LENGTH_SHORT).show()
            }
            .setOnCancelListener {
                adapter.notifyDataSetChanged() // Juga mengembalikan item jika dialog dibatalkan.
            }
            .show()
    }
}