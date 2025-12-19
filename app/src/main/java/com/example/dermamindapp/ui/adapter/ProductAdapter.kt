package com.example.dermamindapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.Product
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var productList: List<Product>,
    // Callback 1: Klik detail produk
    private val onItemClick: (Product) -> Unit,
    // Callback 2: Klik tambah ke rak (Opsional, default null biar aman)
    private val onAddToShelfClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    fun updateData(newProducts: List<Product>) {
        productList = newProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = productList.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvProductCategory)
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvSuitability: TextView = itemView.findViewById(R.id.tvProductSuitability)
        private val btnAddToShelf: ImageButton = itemView.findViewById(R.id.btnAddToShelf)

        fun bind(product: Product) {
            tvName.text = product.name
            tvCategory.text = product.category
            tvSuitability.text = "Cocok: ${product.suitability}"

            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            tvPrice.text = formatRupiah.format(product.price)

            Glide.with(itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(ivImage)

            // Logic Tombol Add to Shelf
            btnAddToShelf.setOnClickListener {
                onAddToShelfClick?.invoke(product)
                // Visual feedback sederhana (Ganti warna jadi merah/aktif)
                btnAddToShelf.setColorFilter(android.graphics.Color.RED)
            }

            itemView.setOnClickListener {
                onItemClick(product)
            }
        }
    }
}