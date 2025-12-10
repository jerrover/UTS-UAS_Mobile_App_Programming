package com.example.dermamindapp.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.Product
import com.google.android.material.card.MaterialCardView

class ProductSelectionAdapter(
    private val allProducts: List<Product>,
    private val initiallySelected: List<Product>
) : RecyclerView.Adapter<ProductSelectionAdapter.ViewHolder>() {

    // Simpan produk yang dipilih di dalam HashSet agar pencarian cepat & unik
    private val selectedItems = HashSet<String>() // Simpan Nama atau ID

    init {
        // Masukkan data awal yang sudah terpilih
        initiallySelected.forEach { selectedItems.add(it.name) }
    }

    // Fungsi untuk mengambil hasil akhir
    fun getSelectedProducts(): List<Product> {
        return allProducts.filter { selectedItems.contains(it.name) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = allProducts[position]
        val isSelected = selectedItems.contains(product.name)
        holder.bind(product, isSelected)
    }

    override fun getItemCount(): Int = allProducts.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardProductSelection)
        private val ivCheck: ImageView = itemView.findViewById(R.id.ivCheckIndicator)
        private val ivThumb: ImageView = itemView.findViewById(R.id.ivProductThumb)
        private val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvBrand: TextView = itemView.findViewById(R.id.tvProductBrand)

        fun bind(product: Product, isSelected: Boolean) {
            tvName.text = product.name
            tvBrand.text = product.brand

            // Load Image
            Glide.with(itemView.context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_product_placeholder) // Ganti sesuai asetmu
                .into(ivThumb)

            // Styling State: Selected vs Unselected
            if (isSelected) {
                card.strokeColor = ContextCompat.getColor(itemView.context, R.color.blue_500) // Warna Primer
                card.strokeWidth = 4 // Tebal agar kelihatan jelas
                card.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.blue_50)) // Background tipis
                ivCheck.visibility = View.VISIBLE
                ivCheck.setColorFilter(Color.WHITE)
                (ivCheck.parent as View).setBackgroundResource(R.drawable.bg_circle_active) // Lingkaran biru
            } else {
                card.strokeColor = ContextCompat.getColor(itemView.context, R.color.gray_300)
                card.strokeWidth = 2
                card.setCardBackgroundColor(Color.WHITE)
                ivCheck.visibility = View.GONE
                (ivCheck.parent as View).setBackgroundResource(R.drawable.bg_circle_inactive) // Lingkaran abu
            }

            // Click Logic
            itemView.setOnClickListener {
                if (selectedItems.contains(product.name)) {
                    selectedItems.remove(product.name)
                } else {
                    selectedItems.add(product.name)
                }
                notifyItemChanged(adapterPosition)
            }
        }
    }
}