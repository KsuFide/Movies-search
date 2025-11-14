package com.example.moviesearch.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviesearch.data.api.Database
import com.example.moviesearch.view.adapters.FilmListRecyclerAdapter
import com.example.moviesearch.view.MainActivity
import com.example.moviesearch.view.adapters.TopSpacingItemDecoration
import com.example.moviesearch.databinding.FragmentWatchLaterBinding
import com.example.moviesearch.utils.AnimationHelper

class WatchLaterFragment : Fragment() {

    private var _binding: FragmentWatchLaterBinding? = null
    private val binding get() = _binding!!
    private lateinit var filmsAdapter: FilmListRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWatchLaterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AnimationHelper.performFragmentCircularRevealAnimation(binding.homeFragmentRoot, requireActivity(), 3)
        binding.homeFragmentRoot.visibility = View.VISIBLE

        filmsAdapter = FilmListRecyclerAdapter { film ->
            (activity as? MainActivity)?.launchDetailsActivity(film)
        }

        binding.watchLaterRecycler.apply {
            adapter = filmsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(TopSpacingItemDecoration(8))
        }

        loadFilms()
    }

    private fun loadFilms() {
        // Теперь показываем фильмы "Посмотреть позже"
        val films = Database.getWatchLaterFilms()
        filmsAdapter.submitList(films)

        if (films.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.watchLaterRecycler.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.watchLaterRecycler.visibility = View.VISIBLE
            Log.d("WatchLaterFragment", "⏰ Загружено фильмов 'Посмотреть позже': ${films.size}")
        }
    }

    override fun onResume() {
        super.onResume()
        loadFilms()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}