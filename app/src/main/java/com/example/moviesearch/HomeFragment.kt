package com.example.moviesearch

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Scene
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.example.moviesearch.databinding.FragmentHomeBinding
import java.util.Locale

class HomeFragment : Fragment() {

    // View Binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var filmsAdapter: FilmListRecyclerAdapter
    private val allFilms = Data.films

    // Элементы из отдельной разметки (merge_home_screen_content)
    private lateinit var searchView: SearchView
    private lateinit var mainRecycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Инициализация View Binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Создание сцены для анимации
        val scene = Scene.getSceneForLayout(
            binding.homeFragmentRoot,    // Корневой контейнер
            R.layout.merge_home_screen_content, // Разметка контента
            requireContext()
        )

        // 2. Создание кастомной анимации
        val searchSlide = Slide(Gravity.TOP).addTarget(R.id.search_view)    // Анимация поиска сверху
        val recyclerSlide = Slide(Gravity.BOTTOM).addTarget(R.id.main_recycler) // Анимация списка снизу

        val customTransition = TransitionSet().apply {
            duration = 500 // Длительность анимации
            addTransition(recyclerSlide)
            addTransition(searchSlide)
        }

        // 3. Запуск анимации сцены
        TransitionManager.go(scene, customTransition)

        // 4. Инициализация элементов после анимации
        searchView = binding.homeFragmentRoot.findViewById(R.id.search_view)
        mainRecycler = binding.homeFragmentRoot.findViewById(R.id.main_recycler)

        // Настройка цвета текста в поиске
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        // Настройка компонентов
        setupRecycler()
        setupSearchView()

        // Раскрытие поиска при клике
        searchView.setOnClickListener {
            searchView.isIconified = false
        }
    }

    private fun setupRecycler() {
        // Создание адаптера с обработчиком клика
        filmsAdapter = FilmListRecyclerAdapter { film ->
            (activity as? MainActivity)?.launchDetailsActivity(film)
        }

        // Настройка RecyclerView
        mainRecycler.apply {
            adapter = filmsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(TopSpacingItemDecoration(8)) // Добавление отступов

            // Обработчик скролла для скрытия/показа поиска
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val scrollThreshold = 20
                    when {
                        // Скролл вниз - скрыть поиск
                        dy > scrollThreshold -> hideSearchView()
                        // Скролл вверх - показать поиск
                        dy < -scrollThreshold -> showSearchView()
                    }
                }
            })
        }
        filmsAdapter.submitList(allFilms) // Установка данных
    }

    private fun hideSearchView() {
        // Анимация скрытия поиска
        searchView.animate()
            .translationY(-searchView.height.toFloat()) // Сдвиг вверх
            .alpha(0f)                                  // Исчезновение
            .setDuration(300)
            .withEndAction { searchView.visibility = View.GONE }
    }

    private fun showSearchView() {
        // Анимация показа поиска
        searchView.visibility = View.VISIBLE
        searchView.animate()
            .translationY(0f)   // Возврат на место
            .alpha(1f)          // Появление
            .setDuration(300)
    }

    private fun setupSearchView() {
        // Обработчик поисковых запросов
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // При отправке запроса
            override fun onQueryTextSubmit(query: String?): Boolean = true

            // При изменении текста
            override fun onQueryTextChange(newText: String?): Boolean {
                val searchText = newText ?: ""
                if (searchText.isEmpty()) {
                    // Показать все фильмы если запрос пустой
                    filmsAdapter.addItems(allFilms)
                } else {
                    // Фильтрация по названию (без учета регистра)
                    val result = allFilms.filter {
                        it.title.toLowerCase(Locale.getDefault())
                            .contains(searchText.toLowerCase(Locale.getDefault()))
                    }
                    filmsAdapter.addItems(result)
                }
                return true
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Очистка binding для предотвращения утечек памяти
    }
}