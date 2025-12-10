package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.Product
import com.example.dermamindapp.ui.adapter.ProductSelectionAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class ProductSelectionBottomSheet(
    private val allProducts: List<Product>,
    private val currentSelection: List<Product>,
    private val onSaveClick: (List<Product>) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var adapter: ProductSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Kita butuh layout khusus untuk bottom sheet ini
        return inflater.inflate(R.layout.bottom_sheet_product_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvProducts: RecyclerView = view.findViewById(R.id.rvProductSelection)
        val btnSave: MaterialButton = view.findViewById(R.id.btnSaveSelection)

        // Setup Adapter
        adapter = ProductSelectionAdapter(allProducts, currentSelection)
        rvProducts.layoutManager = LinearLayoutManager(requireContext())
        rvProducts.adapter = adapter

        btnSave.setOnClickListener {
            val result = adapter.getSelectedProducts()
            onSaveClick(result)
            dismiss()
        }
    }

    // Opsional: Bikin background transparan melengkung
    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
}