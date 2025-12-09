package com.example.dermamindapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermamindapp.R
import com.example.dermamindapp.ui.adapter.ArticleAdapter
import com.example.dermamindapp.ui.viewmodel.HomeViewModel

class ArticlesFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var articleAdapter: ArticleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Pastikan nama layout sesuai (fragment_article_list atau fragment_articles)
        val view = inflater.inflate(R.layout.fragment_article_list, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val articlesRecyclerView: RecyclerView = view.findViewById(R.id.articlesRecyclerView)
        articlesRecyclerView.layoutManager = LinearLayoutManager(context)

        // 1. SETUP ADAPTER (Perbaikan Utama)
        // Kita masukkan fungsi "onClick" di sini untuk menangani klik item
        articleAdapter = ArticleAdapter { article ->
            try {
                // Navigasi ke Detail Artikel menggunakan Safe Args
                val action = ArticlesFragmentDirections.actionArticlesFragmentToArticleDetailFragment(article)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e("ArticlesFragment", "Navigasi error: ${e.message}")
            }
        }
        articlesRecyclerView.adapter = articleAdapter

        // 2. INISIALISASI VIEWMODEL
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // 3. OBSERVE DATA
        viewModel.articles.observe(viewLifecycleOwner) { articleList ->
            // Gunakan submitList() karena sekarang memakai ListAdapter
            articleAdapter.submitList(articleList)
        }

        // viewModel.fetchArticles() // Biasanya init block di ViewModel sudah memanggil ini

        return view
    }
}