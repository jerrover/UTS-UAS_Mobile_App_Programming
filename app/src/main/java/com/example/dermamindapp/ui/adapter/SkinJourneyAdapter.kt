// Adapter untuk RecyclerView.
package com.example.dermamindapp.ui.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.SkinAnalysis
import com.example.dermamindapp.ui.fragment.SkinJourneyFragmentDirections
import java.text.SimpleDateFormat
import java.util.*

class SkinJourneyAdapter(private var analysisList: MutableList<SkinAnalysis>) : RecyclerView.Adapter<SkinJourneyAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val summary: TextView = itemView.findViewById(R.id.tvResultSummary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_skin_journey, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = analysisList[position]

        // Menggunakan Glide untuk memuat gambar dari URI (sesuai materi M05)
        Glide.with(holder.itemView.context)
            .load(Uri.parse(currentItem.imageUri))
            .into(holder.thumbnail)

        holder.date.text = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(currentItem.date))
        holder.summary.text = currentItem.result

        // Navigasi ke detail saat item diklik (sesuai materi M04)
        holder.itemView.setOnClickListener {
            val action = SkinJourneyFragmentDirections.actionSkinJourneyFragmentToSkinDetailFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return analysisList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(analyses: List<SkinAnalysis>) {
        this.analysisList = analyses.toMutableList()
        notifyDataSetChanged()
    }

    fun getItemAt(position: Int): SkinAnalysis {
        return analysisList[position]
    }
}