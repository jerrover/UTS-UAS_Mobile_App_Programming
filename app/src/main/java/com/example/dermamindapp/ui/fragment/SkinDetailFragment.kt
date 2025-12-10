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
import com.example.dermamindapp.data.model.Product
import com.example.dermamindapp.ui.viewmodel.JourneyViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SkinDetailFragment : Fragment() {

    private val args by navArgs<SkinDetailFragmentArgs>()
    private lateinit var viewModel: JourneyViewModel

    // UI Components Lama
    private lateinit var tvDate: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvNotes: TextView
    private lateinit var ivAnalysis: ImageView

    // UI Components Baru (Routine/Skincare)
    private lateinit var tvRoutineList: TextView
    private lateinit var btnEditRoutine: ImageView

    // Master Data Produk
    private var allProducts: List<Product> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_skin_detail, container, false)

        viewModel = ViewModelProvider(this)[JourneyViewModel::class.java]

        setupToolbar(view)
        initViews(view)

        // Load data produk untuk persiapan dialog pilihan
        loadAllProductsFromAssets()

        // Tampilkan data ke layar
        populateUI()

        // Setup Listeners (Termasuk Perbaikan Tombol Rekomendasi)
        setupListeners(view)

        // Observer untuk status pesan dari ViewModel
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

    private fun setupToolbar(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        toolbar.inflateMenu(R.menu.detail_menu)
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.menu_delete) {
                deleteAnalysis()
            }
            true
        }
    }

    private fun initViews(view: View) {
        tvDate = view.findViewById(R.id.tvDateDetail)
        tvResult = view.findViewById(R.id.tvResultDetail)
        tvNotes = view.findViewById(R.id.tvNotesDetail)
        ivAnalysis = view.findViewById(R.id.ivAnalysisDetail)

        // Init View Baru
        tvRoutineList = view.findViewById(R.id.tvRoutineList)
        btnEditRoutine = view.findViewById(R.id.btnEditRoutine)
    }

    private fun setupListeners(view: View) {
        val btnEditNotes: MaterialButton = view.findViewById(R.id.btnEditNotes)
        // Pastikan ID ini sesuai dengan XML (btnRecommendation)
        val btnRecommendation: MaterialButton = view.findViewById(R.id.btnRecommendation)

        // Listener Edit Notes
        btnEditNotes.setOnClickListener {
            showEditNotesDialog()
        }

        // Listener Edit Routine (Skincare)
        btnEditRoutine.setOnClickListener {
            showProductSelectionDialog()
        }

        // --- PERBAIKAN TOMBOL REKOMENDASI DI SINI ---
        btnRecommendation.setOnClickListener {
            val analysisResultString = args.currentAnalysis.result

            try {
                // CARA 1: Paling Aman (Manual Bundle + Resource ID)
                val bundle = Bundle().apply {
                    putString("analysisResult", analysisResultString)
                }

                findNavController().navigate(
                    R.id.action_skinDetailFragment_to_analysisRecommendationFragment,
                    bundle
                )

            } catch (e: Exception) {
                // CARA 2: Fallback pakai SafeArgs jika cara 1 gagal
                try {
                    val action = SkinDetailFragmentDirections
                        .actionSkinDetailFragmentToAnalysisRecommendationFragment(analysisResultString)
                    findNavController().navigate(action)
                } catch (e2: Exception) {
                    // Jika gagal total, tampilkan error
                    e2.printStackTrace()
                    Snackbar.make(view, "Navigasi Gagal: ${e2.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun populateUI() {
        // 1. Set Basic Info
        tvDate.text = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(args.currentAnalysis.date))
        tvResult.text = args.currentAnalysis.result
        tvNotes.text = args.currentAnalysis.notes?.ifEmpty { "No notes added." }

        // 2. Load Image
        try {
            Glide.with(this).load(args.currentAnalysis.imageUri).into(ivAnalysis)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Set Routine Info (Bagian Baru)
        val products = args.currentAnalysis.usedProducts
        if (products.isNullOrEmpty()) {
            tvRoutineList.text = "No products selected yet."
        } else {
            // Format list produk menjadi bullet points
            val formattedList = products.joinToString(separator = "\n") { "• ${it.name}" }
            tvRoutineList.text = formattedList
        }
    }

    // --- LOGIC BARU: LOAD & SELECT PRODUCTS ---

    private fun loadAllProductsFromAssets() {
        try {
            val jsonString = requireContext().assets.open("products.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Product>>() {}.type
            allProducts = Gson().fromJson(jsonString, listType)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback kosong jika gagal
            allProducts = listOf()
        }
    }

    private fun showProductSelectionDialog() {
        if (allProducts.isEmpty()) {
            Snackbar.make(requireView(), "Product data not loaded", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Panggil BottomSheet Keren Kita
        val bottomSheet = ProductSelectionBottomSheet(
            allProducts = allProducts,
            currentSelection = args.currentAnalysis.usedProducts
        ) { selectedProducts ->
            // Callback saat tombol Save ditekan
            saveRoutine(ArrayList(selectedProducts))
        }

        bottomSheet.show(parentFragmentManager, "ProductSelectionBottomSheet")
    }

    private fun saveRoutine(newProducts: ArrayList<Product>) {
        // Panggil ViewModel untuk update ke database
        viewModel.updateSkincareRoutine(args.currentAnalysis.id, newProducts)

        // Update UI lokal & Argument saat ini agar langsung berubah tanpa reload layar
        args.currentAnalysis.usedProducts = newProducts
        populateUI()
    }

    // --- LOGIC LAMA: NOTES & DELETE ---

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