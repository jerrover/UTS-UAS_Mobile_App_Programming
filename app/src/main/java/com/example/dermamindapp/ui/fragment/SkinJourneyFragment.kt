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

        dbHelper = DatabaseHelper(requireContext())
        recyclerView = view.findViewById(R.id.recyclerView)
        tvNoData = view.findViewById(R.id.tvNoData)

        adapter = SkinJourneyAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

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

        return view
    }

    override fun onResume() {
        super.onResume()
        loadAnalyses()
    }

    private fun loadAnalyses() {
        val analyses = dbHelper.getAllAnalyses()
        if (analyses.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvNoData.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvNoData.visibility = View.GONE
            adapter.setData(analyses)
        }
    }

    private fun showDeleteConfirmationDialog(analysis: SkinAnalysis) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Analysis")
            .setMessage("Are you sure you want to delete this history?")
            .setNegativeButton("Cancel") { dialog, _ ->
                adapter.notifyDataSetChanged() // Reset item yang di-swipe
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteAnalysis(analysis.id)
                loadAnalyses()
                Snackbar.make(requireView(), "Analysis deleted", Snackbar.LENGTH_SHORT).show()
            }
            .setOnCancelListener {
                adapter.notifyDataSetChanged()
            }
            .show()
    }
}