package com.example.moviesearch

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviesearch.databinding.FragmentFavoritesBinding


class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    // Объявляем адаптер как свойство фрагмента
    private lateinit var filmsAdapter: FilmListRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инициализация View Binding для фрагмента
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filmsAdapter = FilmListRecyclerAdapter (
            { film -> (activity as? MainActivity)?.launchDetailsActivity(film) }
        )

        // Настройка RecyclerView
        binding.favoritesRecycler.apply {
            // Присваиваем адаптер
            adapter = filmsAdapter
            // Присвоим layoutManager
            layoutManager = LinearLayoutManager(requireContext())
            // Применяем декоратор для отступов
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
        loadFavorites() // Обновляем список при возвращении на фрагмент
    }

    // Обнуляем binding
    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем binding при уничтожении View для избежания утечек памяти
        _binding = null
    }
}