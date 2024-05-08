package com.example.thenewsapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.thenewsapp.R
import com.example.thenewsapp.databinding.ActivityNewsBinding
import com.example.thenewsapp.db.ArticleDatabase
import com.example.thenewsapp.repository.NewsRepository
import com.example.thenewsapp.ui.fragments.ArticleFragment

class NewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsBinding
    internal val newsViewModel: NewsViewModel by viewModels {
        NewsViewModelProviderFactory(application, NewsRepository(ArticleDatabase(this)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    fun openArticleFragment(bundle: Bundle) {
        val articleFragment = ArticleFragment()
        articleFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.newsNavHostFragment, articleFragment)
            .addToBackStack(null)
            .commit()
    }
}