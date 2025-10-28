package com.example.moviesearch.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviesearch.view.adapters.FilmListRecyclerAdapter
import com.example.moviesearch.view.MainActivity
import com.example.moviesearch.view.adapters.TopSpacingItemDecoration
import com.example.moviesearch.databinding.FragmentWatchLaterBinding
import com.example.moviesearch.domain.IInteractor
import com.example.moviesearch.utils.AnimationHelper
import javax.inject.Inject


class WatchLaterFragment : Fragment() {

    @Inject
    lateinit var interactor: IInteractor

    private var _binding: FragmentWatchLaterBinding? = null
    private val binding get() = _binding!!
    private lateinit var filmsAdapter: FilmListRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentWatchLaterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Запускаем анимацию circular reveal
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
        // Здесь можно добавить логику для списка "Посмотреть позже"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

