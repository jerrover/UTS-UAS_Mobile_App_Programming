package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider // Import ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.SkinAnalysis
import com.example.dermamindapp.ui.adapter.SkinJourneyAdapter
import com.example.dermamindapp.ui.viewmodel.JourneyViewModel // Import ViewModel kita
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SkinJourneyFragment : Fragment() {

    private lateinit var viewModel: JourneyViewModel // Deklarasi ViewModel
    private lateinit var adapter: SkinJourneyAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoData: TextView
    // Tambahkan ProgressBar di XML nanti (opsional), kalau ga ada hapus aja baris ini
    // private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_skin_journey, container, false)

        // 1. Inisialisasi ViewModel
        viewModel = ViewModelProvider(this)[JourneyViewModel::class.java]

        recyclerView = view.findViewById(R.id.recyclerView)
        tvNoData = view.findViewById(R.id.tvNoData)

        setupRecyclerView()
        setupSwipeToDelete()

        // 2. Observe (Pantau) Data dari ViewModel
        // Setiap kali data di database berubah, kode di dalam { ... } ini akan jalan otomatis!
        viewModel.analyses.observe(viewLifecycleOwner) { list ->
            adapter.setData(list)

            if (list.isEmpty()) {
                recyclerView.visibility = View.GONE
                tvNoData.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                tvNoData.visibility = View.GONE
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Panggil fungsi load di ViewModel
        viewModel.loadAnalyses()
    }

    private fun setupRecyclerView() {
        adapter = SkinJourneyAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
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
                adapter.notifyDataSetChanged()
                dialog.dismiss()
            }
            .setPositiveButton("Hapus") { _, _ ->
                // Panggil fungsi delete di ViewModel
                viewModel.deleteAnalysis(analysis.id)
                Snackbar.make(requireView(), "Riwayat dihapus", Snackbar.LENGTH_SHORT).show()
            }
            .setOnCancelListener {
                adapter.notifyDataSetChanged()
            }
            .show()
    }
}