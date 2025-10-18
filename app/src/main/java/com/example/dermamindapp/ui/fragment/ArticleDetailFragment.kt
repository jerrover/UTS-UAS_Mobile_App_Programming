package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.dermamindapp.R

// Fragment ini bertanggung jawab untuk menampilkan detail dari sebuah artikel.
class ArticleDetailFragment : Fragment() {

    // Mengambil argumen (data artikel) yang dikirimkan melalui Navigasi Komponen.
    private val args: ArticleDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_article_detail, container, false)

        // Mengatur toolbar, termasuk tombol kembali.
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack() // Kembali ke layar sebelumnya.
        }

        // Inisialisasi komponen UI.
        val articleImage: ImageView = view.findViewById(R.id.articleImage)
        val articleTitle: TextView = view.findViewById(R.id.articleTitle)
        val articleContent: TextView = view.findViewById(R.id.articleContent)

        // Mengambil data artikel dari argumen.
        val article = args.article
        // Menampilkan data artikel ke UI.
        articleTitle.text = article.title
        articleContent.text = article.content
        // Memuat gambar artikel menggunakan Glide.
        Glide.with(this)
            .load(article.imageUrl)
            .into(articleImage)

        return view
    }
}