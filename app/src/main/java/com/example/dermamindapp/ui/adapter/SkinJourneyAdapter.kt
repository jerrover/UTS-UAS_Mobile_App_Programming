package com.example.dermamindapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.SkinAnalysis
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class SkinJourneyAdapter(
    private var list: List<SkinAnalysis>,
    private val onItemClick: (SkinAnalysis) -> Unit,
    private val onConsultClick: (SkinAnalysis) -> Unit
) : RecyclerView.Adapter<SkinJourneyAdapter.ViewHolder>() {

    fun updateData(newList: List<SkinAnalysis>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_skin_journey, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // --- PERBAIKAN: ID DISAMAKAN DENGAN XML item_skin_journey.xml ---

        private val ivImage: ImageView = itemView.findViewById(R.id.ivThumbnail) // XML: ivThumbnail
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate) // XML: tvDate
        private val tvResult: TextView = itemView.findViewById(R.id.tvResultSummary) // XML: tvResultSummary
        private val btnConsult: MaterialButton = itemView.findViewById(R.id.btnCheckRecommendation) // XML: btnCheckRecommendation

        fun bind(item: SkinAnalysis) {
            tvDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(item.date))
            tvResult.text = item.result

            Glide.with(itemView.context)
                .load(item.imageUri)
                .placeholder(R.drawable.scan) // Pastikan drawable 'scan' ada, atau ganti default lain
                .into(ivImage)

            // Klik Item (Pindah ke Detail)
            itemView.setOnClickListener {
                onItemClick(item)
            }

            // Klik Tombol "Lihat Produk Cocok"
            btnConsult.setOnClickListener {
                onConsultClick(item)
            }
        }
    }
}