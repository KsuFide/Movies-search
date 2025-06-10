package com.example.moviesearch

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesearch.databinding.FragmentHomeBinding
import java.util.Locale

// Фрагмент для отображения главного экрана с списком фильмов
class HomeFragment : Fragment() {

    // View Binding переменные (нужны для безопасного доступа к view)
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!! // Геттер для non-null binding

    // Адаптер для RecyclerView
    private lateinit var filmsAdapter: FilmListRecyclerAdapter

    private val allFilms = Data.films // Полный список фильмов

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Находим EditText внутри SearchView
        val searchEditText =
            binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)

        // Устанавливаем цвета
        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        setupRecycler() // Настравиаем RecyclerView
        setupSearchView() // Настраиваем поиск

        // Нажатие на поле, а не только на иконку
        binding.searchView.setOnClickListener {
            binding.searchView.isIconified = false
        }
    }


    // Настройка RecyclerView и его адаптера
    private fun setupRecycler() {
        // Создаём адаптер с лямдой-обработчиком кликов
        filmsAdapter = FilmListRecyclerAdapter { film ->
            // При клике на фильм запускаем DetailsActivity через MaimActivity
            (activity as? MainActivity)?.launchDetailsActivity(film)
        }

        // Настраиваем RecyclerView
        binding.mainRecycler.apply {
            adapter = filmsAdapter // Устанавливаем адаптер
            layoutManager = LinearLayoutManager(requireContext()) // Линейный макет
            addItemDecoration(TopSpacingItemDecoration(8)) // Декоратор для отступов

            // Добавляем  слушатель скролла
            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                    val scrollThreshold = 20

                    when {
                        dy > scrollThreshold -> hideSearchView() // Скролл вниз
                        dy < -scrollThreshold -> showSearchView() // Скролл вверх
                    }
                }
            })
        }
        // Загружаем данные в адаптер
        filmsAdapter.submitList(allFilms)
    }

    private fun hideSearchView() {
        binding.searchView.animate()
            .translationY(-binding.searchView.height.toFloat())
            .alpha(0f)
            .setDuration(300)
            .withEndAction { binding.searchView.visibility = View.GONE }
    }

    private fun showSearchView() {
        binding.searchView.visibility = View.VISIBLE
        binding.searchView.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(300)
    }

    private fun setupSearchView() {

        // Получаем доступ к внутреннему EditText
        val searchEditText =
            binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Этот метод отрабатывает при нажатии на "поиск" на софт на клавиатуре
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            // Этот метод отрабатывает на каждое изменение текста
            override fun onQueryTextChange(newText: String?): Boolean {
                // Проверяем на null
                val searchText = newText ?: ""

                // Если ввод пуст то вставляем в адаптер всю БД
                if (searchText.isEmpty()) {
                    filmsAdapter.addItems(allFilms)
                    return true
                }
                // Фильтруем список на поиск подходящих сочетаний
                val result = allFilms.filter {
                    // Чтобы всё работало правильно, нужно и запрос, и имя фильма приводить к нижнему регистру
                    it.title.toLowerCase(Locale.getDefault())
                        .contains(searchText.toLowerCase(Locale.getDefault()))
                }
                // Добавляем адаптер
                filmsAdapter.addItems(result)
                return true
            }
        })
    }

    // Очистка binding при уничтожении view для избежания утечек памяти
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}