package com.example.thenewsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thenewsapp.R
import com.example.thenewsapp.adapter.NewsAdapter
import com.example.thenewsapp.databinding.FragmentFavouritesBinding
import com.example.thenewsapp.ui.NewsActivity
import com.example.thenewsapp.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar

class FavouritesFragment : Fragment(R.layout.fragment_favourites) {
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var binding: FragmentFavouritesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavouritesBinding.bind(view)
        newsViewModel = (requireActivity() as NewsActivity).newsViewModel
        setupFavouritesRecycler()
        setupObservers()
    }

    private fun setupObservers() {
        newsViewModel.getFavouriteNews().observe(viewLifecycleOwner) { articles ->
            newsAdapter.submitList(articles)
        }

        newsAdapter.setOnItemClickListener { article ->
            val bundle = Bundle().apply {
                putSerializable("article", article)
            }
            findNavController().navigate(R.id.action_favouritesFragment_to_articleFragment, bundle)
        }
    }

    private fun setupFavouritesRecycler() {
        newsAdapter = NewsAdapter().apply {
            setOnItemClickListener { article ->
                val bundle = Bundle().apply {
                    putSerializable("article", article)
                }
                findNavController().navigate(R.id.action_favouritesFragment_to_articleFragment, bundle)
            }
        }

        binding.recyclerFavourites.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = newsAdapter.currentList[position]
                newsViewModel.deleteArticle(article)
                Snackbar.make(requireView(), "Removed From Favourites", Snackbar.LENGTH_LONG).apply {
                    setAction("UNDO") {
                        newsViewModel.addToFavourites(article)
                    }
                    show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerFavourites)
    }
}
