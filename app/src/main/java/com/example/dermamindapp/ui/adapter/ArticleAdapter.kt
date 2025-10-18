package com.example.dermamindapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.Article
import com.example.dermamindapp.ui.fragment.ArticlesFragmentDirections

// Adapter untuk menampilkan daftar artikel dalam RecyclerView.
class ArticleAdapter(private val articles: List<Article>) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    // Membuat ViewHolder baru saat RecyclerView membutuhkannya.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    // Menghubungkan data artikel pada posisi tertentu dengan ViewHolder.
    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        holder.bind(article)
    }

    // Mengembalikan jumlah total item dalam daftar artikel.
    override fun getItemCount(): Int = articles.size

    // ViewHolder untuk setiap item artikel dalam RecyclerView.
    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val articleImage: ImageView = itemView.findViewById(R.id.articleImage)
        private val articleTitle: TextView = itemView.findViewById(R.id.articleTitle)

        // Mengikat data artikel ke tampilan (UI) dalam item.
        fun bind(article: Article) {
            articleTitle.text = article.title
            // Menggunakan Glide untuk memuat gambar dari URL ke ImageView.
            Glide.with(itemView.context)
                .load(article.imageUrl)
                .into(articleImage)

            // Menangani klik pada item untuk navigasi ke halaman detail artikel.
            itemView.setOnClickListener {
                val action = ArticlesFragmentDirections.actionArticlesFragmentToArticleDetailFragment(article)
                it.findNavController().navigate(action)
            }
        }
    }
}