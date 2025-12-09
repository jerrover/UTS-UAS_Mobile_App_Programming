package com.example.dermamindapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.Article

// Menggunakan ListAdapter + Lambda onClick
class ArticleAdapter(private val onClick: (Article) -> Unit) :
    ListAdapter<Article, ArticleAdapter.ArticleViewHolder>(ArticleDiffCallback) {

    object ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Article, newItem: Article) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val articleImage: ImageView = itemView.findViewById(R.id.articleImage)
        private val articleTitle: TextView = itemView.findViewById(R.id.articleTitle)

        fun bind(article: Article) {
            articleTitle.text = article.title
            Glide.with(itemView.context)
                .load(article.imageUrl)
                .placeholder(R.drawable.ic_article)
                .into(articleImage)

            // Panggil aksi klik ke Fragment
            itemView.setOnClickListener { onClick(article) }
        }
    }
}