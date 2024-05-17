package com.example.thenewsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.webkit.WebViewClient
import com.example.thenewsapp.R
import com.example.thenewsapp.databinding.FragmentArticleBinding
import com.example.thenewsapp.ui.NewsActivity
import com.example.thenewsapp.ui.NewsViewModel
import com.example.thenewsapp.models.Article
import com.google.android.material.snackbar.Snackbar

class ArticleFragment : Fragment(R.layout.fragment_article) {
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var binding: FragmentArticleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Инициализация ViewModel
        newsViewModel = (requireActivity() as NewsActivity).newsViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArticleBinding.bind(view)

        // Извлечение аргумента из Bundle
        val article = arguments?.getSerializable("article") as Article?

        binding.webView.apply {
            webViewClient = WebViewClient()
            article?.url?.let {
                loadUrl(it)
            }
        }

        binding.fab.setOnClickListener {
            article?.let {
                newsViewModel.addToFavourites(it)
                Snackbar.make(view, "Added to Favourites", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
