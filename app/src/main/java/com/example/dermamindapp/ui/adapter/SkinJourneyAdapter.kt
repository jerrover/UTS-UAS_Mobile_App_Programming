package com.example.dermamindapp.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Color
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
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class SkinJourneyAdapter(
    private var list: List<SkinAnalysis>,
    private val onItemClick: (SkinAnalysis) -> Unit,       // Klik Card (ke Detail)
    private val onConsultClick: (SkinAnalysis) -> Unit     // Klik Tombol (ke Rekomendasi)
) : RecyclerView.Adapter<SkinJourneyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val tvDate: TextView = view.findViewById(R.id.tvDate)

        // Chart Components
        val tvTopCondition: TextView = view.findViewById(R.id.tvTopCondition)
        val pbScore: LinearProgressIndicator = view.findViewById(R.id.pbScore)
        val tvPercentage: TextView = view.findViewById(R.id.tvPercentage)

        // Button
        val btnCheckRecommendation: MaterialButton = view.findViewById(R.id.btnCheckRecommendation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_skin_journey, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // 1. Basic Info
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        holder.tvDate.text = dateFormat.format(Date(item.date))

        Glide.with(holder.itemView.context)
            .load(item.imageUri)
            .placeholder(R.drawable.ic_product_placeholder)
            .centerCrop()
            .into(holder.ivThumbnail)

        // 2. Chart Logic
        setupTopConditionChart(holder, item.result)

        // 3. Click Listener: Card -> Detail History
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        // 4. Click Listener: Button -> Rekomendasi Produk
        holder.btnCheckRecommendation.setOnClickListener {
            onConsultClick(item)
        }
    }

    private fun setupTopConditionChart(holder: ViewHolder, jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)
            val keys = jsonObject.keys()

            var topLabel = "Tidak ada data"
            var topScore = 0f

            while (keys.hasNext()) {
                val key = keys.next()
                val score = jsonObject.getDouble(key).toFloat()
                if (score > topScore) {
                    topScore = score
                    topLabel = key
                }
            }

            val cleanLabel = topLabel.replace("_", " ")
            holder.tvTopCondition.text = cleanLabel

            val percentage = (topScore * 100).toInt()
            holder.pbScore.setProgress(percentage, true)
            holder.tvPercentage.text = "$percentage%"

            // Warna
            val isHealthy = topLabel.equals("Kulit_Sehat", ignoreCase = true)
            if (isHealthy) {
                holder.pbScore.setIndicatorColor(Color.parseColor("#4CAF50"))
                holder.pbScore.trackColor = Color.parseColor("#E8F5E9")
            } else {
                if (percentage < 30) {
                    holder.pbScore.setIndicatorColor(Color.parseColor("#FFC107"))
                    holder.pbScore.trackColor = Color.parseColor("#FFF8E1")
                } else if (percentage < 70) {
                    holder.pbScore.setIndicatorColor(Color.parseColor("#FF9800"))
                    holder.pbScore.trackColor = Color.parseColor("#FFF3E0")
                } else {
                    holder.pbScore.setIndicatorColor(Color.parseColor("#F44336"))
                    holder.pbScore.trackColor = Color.parseColor("#FFEBEE")
                }
            }

        } catch (e: Exception) {
            holder.tvTopCondition.text = "Error"
            holder.pbScore.progress = 0
            holder.tvPercentage.text = "-"
        }
    }

    override fun getItemCount() = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<SkinAnalysis>) {
        list = newList
        notifyDataSetChanged()
    }
}