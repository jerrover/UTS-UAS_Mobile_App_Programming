package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.Product
import com.example.dermamindapp.ui.adapter.MyShelfAdapter
import com.example.dermamindapp.ui.viewmodel.MyShelfViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MyShelfFragment : Fragment() {

    private lateinit var viewModel: MyShelfViewModel
    private lateinit var adapter: MyShelfAdapter

    private lateinit var progressBar: ProgressBar
    private lateinit var emptyLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_shelf, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MyShelfViewModel::class.java]

        initViews(view)
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progressBar)
        emptyLayout = view.findViewById(R.id.layoutEmpty)
        recyclerView = view.findViewById(R.id.rvShelf)
        btnBack = view.findViewById(R.id.btnBack)
    }

    private fun setupRecyclerView() {
        // Init Adapter dengan 2 callback:
        // 1. onItemClick -> Untuk melihat detail (navigasi)
        // 2. onDeleteClick -> Untuk menghapus produk (dialog)
        adapter = MyShelfAdapter(
            productList = emptyList(),
            onItemClick = { product ->
                navigateToDetail(product)
            },
            onDeleteClick = { product ->
                showDeleteConfirmation(product)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@MyShelfFragment.adapter
        }
    }

    // Fungsi navigasi ke halaman Detail Produk
    private fun navigateToDetail(product: Product) {
        // Bungkus object product ke dalam Bundle
        // "product" adalah nama argument yang kita set di main_nav.xml untuk ProductDetailsFragment
        val bundle = Bundle().apply {
            putParcelable("product", product)
        }

        try {
            findNavController().navigate(
                R.id.action_myShelfFragment_to_productDetailsFragment,
                bundle
            )
        } catch (e: Exception) {
            android.util.Log.e("MyShelfFragment", "Nav Error: ${e.message}")
            Toast.makeText(context, "Gagal membuka detail: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.shelfProducts.observe(viewLifecycleOwner) { products ->
            if (products.isEmpty()) {
                emptyLayout.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.updateData(products)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearMessages()
            }
        }

        viewModel.deleteStatus.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearMessages()
            }
        }
    }

    private fun showDeleteConfirmation(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Yakin ingin menghapus ${product.name} dari rak kamu?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.removeProductFromShelf(product)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Reload data setiap kali halaman dibuka kembali (biar sinkron)
        viewModel.loadShelf()
    }
}