package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.Product
import com.example.dermamindapp.ui.adapter.ProductSelectionAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class ProductSelectionBottomSheet(
    private val myShelfProducts: List<Product>, // Nama variabel diubah biar jelas
    private val currentSelection: List<Product>,
    private val onSaveClick: (List<Product>) -> Unit,
    private val onEmptyShelfAction: () -> Unit // Callback jika rak kosong
) : BottomSheetDialogFragment() {

    private lateinit var adapter: ProductSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_product_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvProducts: RecyclerView = view.findViewById(R.id.rvProductSelection)
        val btnSave: MaterialButton = view.findViewById(R.id.btnSaveSelection)

        // --- LOGIC BARU: CEK KEKOSONGAN ---
        if (myShelfProducts.isEmpty()) {
            // Jika kosong, ubah teks tombol jadi "Cari Produk Dulu"
            btnSave.text = "Cari Produk di Katalog"
            btnSave.setOnClickListener {
                onEmptyShelfAction() // Arahkan user ke katalog
                dismiss()
            }
            // Bisa tambahkan TextView "Rak Kosong" secara programmatically atau via Layout XML
        } else {
            // Setup Adapter Normal
            adapter = ProductSelectionAdapter(myShelfProducts, currentSelection)
            rvProducts.layoutManager = LinearLayoutManager(requireContext())
            rvProducts.adapter = adapter

            btnSave.setOnClickListener {
                val result = adapter.getSelectedProducts()
                onSaveClick(result)
                dismiss()
            }
        }
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
}