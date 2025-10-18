package com.example.dermamindapp.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

// Fragment ini menampilkan informasi detail dari produk yang dipilih.
class ProductDetailsFragment : Fragment() {

    // Mengambil argumen (data produk) yang dikirimkan melalui Navigasi Komponen.
    private val args: ProductDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_details, container, false)

        // Mengatur toolbar dengan tombol kembali.
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Inisialisasi komponen UI.
        val productImage: ImageView = view.findViewById(R.id.ivProductImage)
        val productName: TextView = view.findViewById(R.id.tvProductName)
        val productSuitability: TextView = view.findViewById(R.id.tvProductSuitability)
        val productDescription: TextView = view.findViewById(R.id.tvProductDescription)

        // Mengambil data produk dari argumen.
        val product = args.product

        // Menampilkan data produk ke UI.
        productName.text = product.name
        productSuitability.text = product.suitability
        productDescription.text = product.description

        // Memuat gambar produk menggunakan Glide.
        Glide.with(this)
            .load(product.imageUrl)
            .into(productImage)

        // Menangani aksi klik pada tombol "Buy Now".
        val buyNowButton: Button = view.findViewById(R.id.btnBuyNow)
        buyNowButton.setOnClickListener {
            showEcommerceChoiceDialog()
        }

        return view
    }

    // Menampilkan dialog untuk memilih platform e-commerce.
    private fun showEcommerceChoiceDialog() {
        val items = arrayOf("Tokopedia", "Shopee")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cari Produk di")
            .setItems(items) { _, which ->
                // Menentukan URL berdasarkan pilihan pengguna.
                val url = when (which) {
                    0 -> args.product.tokopediaUrl
                    1 -> args.product.shopeeUrl
                    else -> ""
                }
                // Membuka URL di browser eksternal jika URL valid.
                if (url.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(url)
                    }
                    startActivity(intent)
                }
            }
            .show()
    }
}