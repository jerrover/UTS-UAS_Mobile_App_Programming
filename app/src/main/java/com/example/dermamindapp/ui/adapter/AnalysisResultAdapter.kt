package com.example.dermamindapp.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermamindapp.R
import com.google.android.material.progressindicator.LinearProgressIndicator

data class AnalysisItem(val label: String, val score: Float)

class AnalysisResultAdapter(private val items: List<AnalysisItem>) :
    RecyclerView.Adapter<AnalysisResultAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvConditionName)
        val tvPercentage: TextView = view.findViewById(R.id.tvPercentage)
        val progressBar: LinearProgressIndicator = view.findViewById(R.id.progressBar)
        val tvSeverity: TextView = view.findViewById(R.id.tvSeverity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_analysis_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Format Label (Hapus underscore)
        val cleanLabel = item.label.replace("_", " ")
        holder.tvName.text = cleanLabel

        // Format Persentase
        val percentage = (item.score * 100).toInt()
        holder.tvPercentage.text = "$percentage%"
        holder.progressBar.setProgress(percentage, true)

        // Logika Warna & Status
        val isHealthyType = item.label.equals("Kulit_Sehat", ignoreCase = true)

        if (isHealthyType) {
            holder.progressBar.setIndicatorColor(Color.parseColor("#4CAF50")) // Hijau
            holder.tvSeverity.text = if (percentage > 80) "Sangat Bagus" else "Perlu Ditingkatkan"
        } else {
            // Penyakit (Semakin tinggi %, semakin merah)
            if (percentage < 30) {
                holder.progressBar.setIndicatorColor(Color.parseColor("#FFC107")) // Kuning
                holder.tvSeverity.text = "Tingkat: Rendah"
            } else if (percentage < 70) {
                holder.progressBar.setIndicatorColor(Color.parseColor("#FF9800")) // Oranye
                holder.tvSeverity.text = "Tingkat: Sedang"
            } else {
                holder.progressBar.setIndicatorColor(Color.parseColor("#F44336")) // Merah
                holder.tvSeverity.text = "Tingkat: Tinggi"
            }
        }
    }

    override fun getItemCount() = items.size
}