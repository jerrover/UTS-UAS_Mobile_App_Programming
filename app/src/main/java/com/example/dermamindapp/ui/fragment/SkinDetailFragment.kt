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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.db.DatabaseHelper
import com.example.dermamindapp.data.model.Product
import com.example.dermamindapp.ui.adapter.AnalysisItem
import com.example.dermamindapp.ui.adapter.AnalysisResultAdapter
import com.example.dermamindapp.ui.fragment.ProductSelectionBottomSheet
import com.example.dermamindapp.ui.viewmodel.JourneyViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SkinDetailFragment : Fragment() {

    private val args by navArgs<SkinDetailFragmentArgs>()
    private lateinit var viewModel: JourneyViewModel

    private lateinit var tvDate: TextView
    private lateinit var rvResult: RecyclerView
    private lateinit var tvNotes: TextView
    private lateinit var ivAnalysis: ImageView
    private lateinit var tvRoutineList: TextView
    private lateinit var btnEditRoutine: ImageView
    private lateinit var dbHelper: DatabaseHelper
    private var myShelfProducts: List<Product> = emptyList()

    // --- PERBAIKAN: Hapus duplikasi di sini, cukup satu deklarasi saja ---
    private var allProducts: List<Product> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_skin_detail, container, false)
        viewModel = ViewModelProvider(this)[JourneyViewModel::class.java]

        dbHelper = DatabaseHelper(requireContext())

        setupToolbar(view)
        initViews(view)

        // Panggil fetch data rak saat fragment dibuat
        fetchMyShelfData()

        populateUI()
        setupListeners(view)

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

    private fun initViews(view: View) {
        tvDate = view.findViewById(R.id.tvDateDetail)
        rvResult = view.findViewById(R.id.rvResultDetail)
        tvNotes = view.findViewById(R.id.tvNotesDetail)
        ivAnalysis = view.findViewById(R.id.ivAnalysisDetail)
        tvRoutineList = view.findViewById(R.id.tvRoutineList)
        btnEditRoutine = view.findViewById(R.id.btnEditRoutine)
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

    private fun setupListeners(view: View) {
        val btnEditNotes: MaterialButton = view.findViewById(R.id.btnEditNotes)
        val btnRecommendation: MaterialButton = view.findViewById(R.id.btnRecommendation)

        btnEditNotes.setOnClickListener { showEditNotesDialog() }
        btnEditRoutine.setOnClickListener { showProductSelectionDialog() }

        btnRecommendation.setOnClickListener {
            val analysisResultString = args.currentAnalysis.result
            try {
                val bundle = Bundle().apply {
                    putString("analysisResult", analysisResultString)
                }
                findNavController().navigate(
                    R.id.action_skinDetailFragment_to_analysisRecommendationFragment,
                    bundle
                )
            } catch (e: Exception) {
                // Error handling...
            }
        }
    }

    private fun populateUI() {
        tvDate.text = SimpleDateFormat(
            "dd MMMM yyyy, HH:mm",
            Locale.getDefault()
        ).format(Date(args.currentAnalysis.date))

        setupResultList(args.currentAnalysis.result)

        tvNotes.text = args.currentAnalysis.notes?.ifEmpty { "No notes added." }

        try {
            Glide.with(this).load(args.currentAnalysis.imageUri).into(ivAnalysis)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val products = args.currentAnalysis.usedProducts
        if (products.isNullOrEmpty()) {
            tvRoutineList.text = "No products selected yet."
        } else {
            val formattedList = products.joinToString(separator = "\n") { "• ${it.name}" }
            tvRoutineList.text = formattedList
        }
    }

    private fun setupResultList(jsonString: String) {
        val items = ArrayList<AnalysisItem>()

        try {
            val jsonObject = JSONObject(jsonString)
            val keys = jsonObject.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                val score = jsonObject.getDouble(key).toFloat()
                items.add(AnalysisItem(key, score))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        items.sortByDescending { it.score }

        rvResult.layoutManager = LinearLayoutManager(requireContext())
        rvResult.adapter = AnalysisResultAdapter(items)
        rvResult.isNestedScrollingEnabled = false
    }

    private fun loadAllProductsFromAssets() {
        try {
            val jsonString =
                requireContext().assets.open("products.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Product>>() {}.type
            allProducts = Gson().fromJson(jsonString, listType)
        } catch (e: Exception) {
            e.printStackTrace()
            allProducts = listOf()
        }
    }

    private fun showProductSelectionDialog() {
        val bottomSheet = ProductSelectionBottomSheet(
            myShelfProducts = myShelfProducts,
            currentSelection = args.currentAnalysis.usedProducts,
            onSaveClick = { selectedProducts ->
                saveRoutine(ArrayList(selectedProducts))
            },
            onEmptyShelfAction = {
                try {
                    findNavController().navigate(R.id.action_skinDetailFragment_to_productRecommendationFragment)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Silakan buka menu Produk untuk menambah stok.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        bottomSheet.show(parentFragmentManager, "ProductSelectionBottomSheet")
    }

    private fun saveRoutine(newProducts: ArrayList<Product>) {
        viewModel.updateSkincareRoutine(args.currentAnalysis.id, newProducts)
        args.currentAnalysis.usedProducts = newProducts
        populateUI()
    }

    private fun showEditNotesDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_notes, null)
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

    private fun fetchMyShelfData() {
        CoroutineScope(Dispatchers.IO).launch {
            val products = dbHelper.getMyShelfProducts()
            withContext(Dispatchers.Main) {
                myShelfProducts = products
            }
        }
    }
}