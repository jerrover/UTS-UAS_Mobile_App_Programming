package com.example.dermamindapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // Fungsi untuk update data saat filter berubah nanti
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

        fun bind(product: Product) {
            tvName.text = product.name
            tvCategory.text = product.category
            tvSuitability.text = "Cocok: ${product.suitability}"

            // Format Harga ke Rupiah
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            tvPrice.text = formatRupiah.format(product.price)

            // Load Gambar dengan Glide
            Glide.with(itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_launcher_background) // Gambar loading sementara
                .error(R.drawable.ic_launcher_background) // Gambar jika error
                .into(ivImage)

            // Klik item
            itemView.setOnClickListener {
                onItemClick(product)
            }
        }
    }
}