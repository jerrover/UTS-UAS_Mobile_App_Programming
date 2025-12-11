package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermamindapp.R
import com.example.dermamindapp.data.PreferencesHelper
import com.example.dermamindapp.ui.adapter.SkinJourneyAdapter
import com.example.dermamindapp.ui.viewmodel.JourneyViewModel
import com.google.android.material.snackbar.Snackbar

class SkinJourneyFragment : Fragment() {

    private lateinit var viewModel: JourneyViewModel
    private lateinit var adapter: SkinJourneyAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_skin_journey, container, false)

        prefsHelper = PreferencesHelper(requireContext())
        viewModel = ViewModelProvider(this)[JourneyViewModel::class.java]

        initViews(view)
        setupRecyclerView()
        observeViewModel()

        val userId = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)
        if (!userId.isNullOrEmpty()) {
            viewModel.loadAnalyses(userId)
        } else {
            tvEmpty.text = "Silakan login untuk melihat riwayat."
            tvEmpty.visibility = View.VISIBLE
        }

        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmpty = view.findViewById(R.id.tvNoData)
    }

    private fun setupRecyclerView() {
        adapter = SkinJourneyAdapter(
            emptyList(),
            onItemClick = { analysis ->
                // AKSI 1: Klik Card -> Masuk ke Detail Riwayat
                try {
                    val action = SkinJourneyFragmentDirections
                        .actionSkinJourneyFragmentToSkinDetailFragment(analysis)
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    // Fallback manual
                    val bundle = Bundle().apply {
                        putParcelable("currentAnalysis", analysis)
                    }
                    findNavController().navigate(R.id.action_skinJourneyFragment_to_skinDetailFragment, bundle)
                }
            },
            onConsultClick = { analysis ->
                // AKSI 2: Klik Tombol -> Masuk ke Rekomendasi Produk
                Log.d("SkinJourney", "Cek Produk untuk: ${analysis.result}")
                try {
                    val action = SkinJourneyFragmentDirections
                        .actionSkinJourneyFragmentToAnalysisRecommendationFragment(analysis.result)
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    // Fallback manual
                    val bundle = Bundle().apply {
                        putString("analysisResult", analysis.result)
                    }
                    findNavController().navigate(
                        R.id.action_skinJourneyFragment_to_analysisRecommendationFragment,
                        bundle
                    )
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.analyses.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.updateData(list)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearStatusMessage()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload data saat kembali ke halaman ini (misal setelah hapus data di detail)
        val userId = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)
        if (!userId.isNullOrEmpty()) {
            viewModel.loadAnalyses(userId)
        }
    }
}