package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.dermamindapp.R
import com.example.dermamindapp.data.model.Article
import com.example.dermamindapp.ui.adapter.ArticleAdapter

// Fragment ini menampilkan daftar semua artikel dalam sebuah RecyclerView.
class ArticlesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_article_list, container, false)

        // Inisialisasi dan pengaturan toolbar dengan tombol kembali.
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Inisialisasi RecyclerView dan adapter-nya.
        val articlesRecyclerView: RecyclerView = view.findViewById(R.id.articlesRecyclerView)
        // Data artikel (saat ini masih statis/hardcoded).
        val articles = listOf(
            Article(1, "5 Mitos Skincare yang Salah Kaprah", "Konten artikel 1...", "https://www.beauty-heroes.com/wp-content/uploads/As-organic-skincares-1150x912.jpg.webp"),
            Article(2, "Kenali Tipe Kulitmu", "Konten artikel 2...", "https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEiPekzufeCYmTDOuTkVNGHKH1qHPkuG6nPNxjYeHT8aMhTIJt6Hoc3I5eT12Qd4RShChna6pf3f9Qj3PCzYJwObwjmdq6j-amYZn8p8qpBVJBVFK93wgVcHTpFZbIN5qKX1m4OvbLhmo4oc3xYrvu2VrqpX5gl04ZEaEzzhXXVb9Tc6-gTUPSmU0IOv_q5E/w518-h518-rw/gambar-lima-jenis-kulit-wajah.webp"),
            Article(3, "Bahan Aktif untuk Jerawat", "Konten artikel 3...", "https://www.mitrakeluarga.com/_next/image?url=https%3A%2F%2Fd3uhejzrzvtlac.cloudfront.net%2Fcompro%2FarticleDesktop%2Fe5eb00ff-0dff-4319-bf11-3f8ce7b73e8e.webp&w=1920&q=100")
        )
        // Mengatur adapter untuk RecyclerView.
        articlesRecyclerView.adapter = ArticleAdapter(articles)

        return view
    }
}