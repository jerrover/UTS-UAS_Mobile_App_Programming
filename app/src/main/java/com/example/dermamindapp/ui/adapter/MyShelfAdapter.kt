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

class MyShelfAdapter(
    private var productList: List<Product>,
    // 1. Tambahkan parameter callback baru: onItemClick
    private val onItemClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<MyShelfAdapter.ShelfViewHolder>() {

    inner class ShelfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProduct: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvBrand: TextView = itemView.findViewById(R.id.tvBrand)
        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShelfViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shelf_product, parent, false)
        return ShelfViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShelfViewHolder, position: Int) {
        val product = productList[position]

        holder.tvBrand.text = product.brand
        holder.tvName.text = product.name
        holder.tvCategory.text = product.category

        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .placeholder(R.drawable.ic_product_placeholder)
            .into(holder.ivProduct)

        // 2. Set listener untuk Klik Kartu (Detail)
        holder.itemView.setOnClickListener {
            onItemClick(product)
        }

        // Listener untuk tombol Hapus (tetap sama)
        holder.btnDelete.setOnClickListener {
            onDeleteClick(product)
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateData(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}