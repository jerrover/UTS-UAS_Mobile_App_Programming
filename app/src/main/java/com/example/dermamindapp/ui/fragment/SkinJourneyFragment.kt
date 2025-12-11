package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
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
import java.util.Locale

class SkinJourneyFragment : Fragment() {

    private lateinit var viewModel: JourneyViewModel
    private lateinit var adapter: SkinJourneyAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var prefsHelper: PreferencesHelper

    // View baru
    private lateinit var calendarView: CalendarView
    private lateinit var tvFilterInfo: TextView
    private lateinit var btnResetFilter: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_skin_journey, container, false)

        prefsHelper = PreferencesHelper(requireContext())
        viewModel = ViewModelProvider(this)[JourneyViewModel::class.java]

        initViews(view)
        setupRecyclerView()
        setupCalendar() // Setup Kalender
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

        // Init View Baru
        calendarView = view.findViewById(R.id.calendarView)
        tvFilterInfo = view.findViewById(R.id.tvFilterInfo)
        btnResetFilter = view.findViewById(R.id.btnResetFilter)

        btnResetFilter.setOnClickListener {
            viewModel.showAllData()
            tvFilterInfo.text = "Menampilkan semua riwayat"
            btnResetFilter.visibility = View.GONE
            // 1. Reset tanggal kalender ke hari ini
            calendarView.setDate(System.currentTimeMillis(), true, true)
            // 2. Terapkan style netral (tanpa highlight) pada tanggal
            calendarView.setDateTextAppearance(R.style.TextAppearance_App_Body)
        }
    }

    private fun setupCalendar() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Panggil filter di ViewModel
            viewModel.filterByDate(year, month, dayOfMonth)

            // Update UI Teks
            val selectedDateStr = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month + 1, year)
            tvFilterInfo.text = "Hasil pada: $selectedDateStr"
            btnResetFilter.visibility = View.VISIBLE

            // KEMBALIKAN STYLE KE DEFAULT (0) untuk menampilkan highlight seleksi tanggal yang baru
            calendarView.setDateTextAppearance(0)
        }
    }

    private fun setupRecyclerView() {
        adapter = SkinJourneyAdapter(
            emptyList(),
            onItemClick = { analysis ->
                try {
                    val action = SkinJourneyFragmentDirections
                        .actionSkinJourneyFragmentToSkinDetailFragment(analysis)
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    val bundle = Bundle().apply {
                        putParcelable("currentAnalysis", analysis)
                    }
                    findNavController().navigate(R.id.action_skinJourneyFragment_to_skinDetailFragment, bundle)
                }
            },
            onConsultClick = { analysis ->
                try {
                    val action = SkinJourneyFragmentDirections
                        .actionSkinJourneyFragmentToAnalysisRecommendationFragment(analysis.result)
                    findNavController().navigate(action)
                } catch (e: Exception) {
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
            // Update logic untuk handle empty state yang lebih baik
            if (list.isNullOrEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE

                // Ubah pesan kosong tergantung konteks (Filtered atau Emang kosong semua)
                if (btnResetFilter.visibility == View.VISIBLE) {
                    tvEmpty.text = "Tidak ada analisis pada tanggal ini."
                } else {
                    tvEmpty.text = "Belum ada riwayat analisis.\nMulai scan wajah Anda!"
                }

            } else {
                tvEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.updateData(list)
            }
        }

        // ... (observe isLoading dan statusMessage sama seperti sebelumnya) ...
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
        val userId = prefsHelper.getString(PreferencesHelper.KEY_USER_ID)
        if (!userId.isNullOrEmpty()) {
            // Kita reset filter saat masuk ulang agar data sinkron
            viewModel.loadAnalyses(userId)
            btnResetFilter.visibility = View.GONE
            tvFilterInfo.text = "Menampilkan semua riwayat"
            // FIX: Atur ulang tanggal kalender ke hari ini agar highlight tanggal yang sebelumnya dipilih hilang
            calendarView.setDate(System.currentTimeMillis(), true, true)
            // Terapkan style netral (tanpa highlight) saat dimuat ulang
            calendarView.setDateTextAppearance(R.style.TextAppearance_App_Body)
        }
    }
}