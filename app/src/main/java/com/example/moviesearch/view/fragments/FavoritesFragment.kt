package com.example.moviesearch.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviesearch.data.Database
import com.example.moviesearch.view.adapters.FilmListRecyclerAdapter
import com.example.moviesearch.view.MainActivity
import com.example.moviesearch.view.adapters.TopSpacingItemDecoration
import com.example.moviesearch.databinding.FragmentFavoritesBinding
import com.example.moviesearch.utils.AnimationHelper

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var filmsAdapter: FilmListRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Запускаем анимацию circular reveal
        AnimationHelper.performFragmentCircularRevealAnimation(binding.homeFragmentRoot, requireActivity(), 2)

        binding.homeFragmentRoot.visibility = View.VISIBLE

        filmsAdapter = FilmListRecyclerAdapter { film ->
            (activity as? MainActivity)?.launchDetailsActivity(film)
        }

        // Настройка RecyclerView
        binding.favoritesRecycler.apply {
            adapter = filmsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(TopSpacingItemDecoration(8))
        }

        loadFavorites()
    }

    private fun loadFavorites() {
        val favorites = Database.getFavoriteFilms()
        filmsAdapter.submitList(favorites)

        if (favorites.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.favoritesRecycler.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.favoritesRecycler.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}