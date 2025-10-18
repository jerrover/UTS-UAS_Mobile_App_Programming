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

// Adapter ini bertanggung jawab untuk menampilkan daftar riwayat analisis kulit
// dalam sebuah RecyclerView.
class SkinJourneyAdapter(private var analysisList: MutableList<SkinAnalysis>) : RecyclerView.Adapter<SkinJourneyAdapter.MyViewHolder>() {

    // ViewHolder untuk setiap item dalam RecyclerView.
    // Ini menyimpan referensi ke view dari setiap item.
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val summary: TextView = itemView.findViewById(R.id.tvResultSummary)
    }

    // Membuat instance ViewHolder baru.
    // Metode ini dipanggil oleh RecyclerView saat perlu membuat item baru.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_skin_journey, parent, false)
        return MyViewHolder(view)
    }

    // Mengikat data dari `analysisList` pada posisi tertentu ke ViewHolder.
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = analysisList[position]

        // Menggunakan Glide untuk memuat gambar dari URI ke ImageView.
        Glide.with(holder.itemView.context)
            .load(Uri.parse(currentItem.imageUri))
            .into(holder.thumbnail)

        // Memformat dan menampilkan tanggal analisis.
        holder.date.text = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(currentItem.date))
        holder.summary.text = currentItem.result

        // Menangani aksi klik pada item, yaitu navigasi ke halaman detail analisis.
        holder.itemView.setOnClickListener {
            val action = SkinJourneyFragmentDirections.actionSkinJourneyFragmentToSkinDetailFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }
    }

    // Mengembalikan jumlah total item dalam dataset.
    override fun getItemCount(): Int {
        return analysisList.size
    }

    // Memperbarui data dalam adapter dan memberi tahu RecyclerView untuk me-refresh tampilan.
    @SuppressLint("NotifyDataSetChanged")
    fun setData(analyses: List<SkinAnalysis>) {
        this.analysisList = analyses.toMutableList()
        notifyDataSetChanged()
    }

    // Mendapatkan item pada posisi tertentu, digunakan untuk aksi swipe-to-delete.
    fun getItemAt(position: Int): SkinAnalysis {
        return analysisList[position]
    }
}