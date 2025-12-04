package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermamindapp.R
import com.example.dermamindapp.data.db.DatabaseHelper
import com.example.dermamindapp.data.model.SkinAnalysis
import com.example.dermamindapp.ui.adapter.SkinJourneyAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SkinJourneyFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: SkinJourneyAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoData: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_skin_journey, container, false)

        // Inisialisasi Helper
        dbHelper = DatabaseHelper(requireContext())

        recyclerView = view.findViewById(R.id.recyclerView)
        tvNoData = view.findViewById(R.id.tvNoData)

        // Setup RecyclerView
        adapter = SkinJourneyAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupSwipeToDelete()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadAnalyses()
    }

    private fun loadAnalyses() {
        // Gunakan lifecycleScope karena getAllAnalyses() itu suspend (async)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Tampilkan loading jika perlu (opsional)
                val analyses = dbHelper.getAllAnalyses()

                if (analyses.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    tvNoData.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    tvNoData.visibility = View.GONE
                    adapter.setData(analyses)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val analysisToDelete = adapter.getItemAt(position)
                    showDeleteConfirmationDialog(analysisToDelete)
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showDeleteConfirmationDialog(analysis: SkinAnalysis) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Riwayat")
            .setMessage("Apakah Anda yakin ingin menghapus riwayat ini?")
            .setNegativeButton("Batal") { dialog, _ ->
                adapter.notifyDataSetChanged() // Kembalikan item yang di-swipe
                dialog.dismiss()
            }
            .setPositiveButton("Hapus") { _, _ ->
                // Panggil fungsi delete di background thread
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        dbHelper.deleteAnalysis(analysis.id)
                        loadAnalyses() // Reload data setelah hapus
                        Snackbar.make(requireView(), "Riwayat dihapus", Snackbar.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setOnCancelListener {
                adapter.notifyDataSetChanged()
            }
            .show()
    }
}