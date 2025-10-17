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

class ProductDetailsFragment : Fragment() {

    private val args: ProductDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_details, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val productImage: ImageView = view.findViewById(R.id.ivProductImage)
        val productName: TextView = view.findViewById(R.id.tvProductName)
        val productSuitability: TextView = view.findViewById(R.id.tvProductSuitability)
        val productDescription: TextView = view.findViewById(R.id.tvProductDescription)

        val product = args.product

        productName.text = product.name
        productSuitability.text = product.suitability
        productDescription.text = product.description

        Glide.with(this)
            .load(product.imageUrl)
            .into(productImage)

        val buyNowButton: Button = view.findViewById(R.id.btnBuyNow)

        buyNowButton.setOnClickListener {
            showEcommerceChoiceDialog()
        }

        return view
    }

    private fun showEcommerceChoiceDialog() {
        val items = arrayOf("Tokopedia", "Shopee")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cari Produk di")
            .setItems(items) { dialog, which ->
                val url = when (which) {
                    0 -> args.product.tokopediaUrl
                    1 -> args.product.shopeeUrl
                    else -> ""
                }
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