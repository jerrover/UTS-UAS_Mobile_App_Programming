package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider // Tambahan
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager // Tambahan
import androidx.recyclerview.widget.RecyclerView
import com.example.dermamindapp.R
import com.example.dermamindapp.ui.adapter.ArticleAdapter
import com.example.dermamindapp.ui.viewmodel.HomeViewModel // Panggil ViewModel

class ArticlesFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel // Deklarasi ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Pastikan nama layout ini sesuai dengan yang kamu punya (fragment_article_list atau fragment_articles)
        val view = inflater.inflate(R.layout.fragment_article_list, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val articlesRecyclerView: RecyclerView = view.findViewById(R.id.articlesRecyclerView)
        articlesRecyclerView.layoutManager = LinearLayoutManager(context) // Pastikan ada LayoutManager

        // 1. Inisialisasi ViewModel
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // 2. Dengarkan Data dari Firebase (lewat ViewModel)
        viewModel.articles.observe(viewLifecycleOwner) { articleList ->
            // Update RecyclerView dengan data baru
            articlesRecyclerView.adapter = ArticleAdapter(articleList)
        }

        // 3. Minta Data (Kalau belum ada)
        viewModel.fetchArticles()

        return view
    }
}