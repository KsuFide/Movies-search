package com.example.moviesearch.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviesearch.data.api.Database
import com.example.moviesearch.databinding.FragmentHomeBinding
import com.example.moviesearch.domain.Film
import com.example.moviesearch.domain.Interactor
import com.example.moviesearch.utils.AnimationHelper
import com.example.moviesearch.utils.PaginationScrollListener
import com.example.moviesearch.view.MainActivity
import com.example.moviesearch.view.adapters.FilmListRecyclerAdapter
import com.example.moviesearch.view.adapters.TopSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var interactor: Interactor

    private lateinit var filmsAdapter: FilmListRecyclerAdapter
    private lateinit var paginationScrollListener: PaginationScrollListener

    // Переменные для пагинации и поиска
    private var currentPage = 1
    private var totalPages = 1
    private var isLoading = false
    private val allFilms = mutableListOf<Film>()

    // Переменные для поиска
    private var isSearchMode = false
    private var currentSearchQuery = ""

    // Добавляем отслеживание текущей категории
    private var currentCategory = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        testPreferences()

        AnimationHelper.performFragmentCircularRevealAnimation(binding.homeFragmentRoot, requireActivity(), 1)
        binding.homeFragmentRoot.visibility = View.VISIBLE

        // Получаем текущую категорию при создании
        currentCategory = interactor.getDefaultCategoryFromPreferences()
        Log.d("HomeFragment", "🎯 Текущая категория: $currentCategory")

        // Инициализация адаптера
        filmsAdapter = FilmListRecyclerAdapter { film ->
            (activity as? MainActivity)?.launchDetailsActivity(film)
        }

        // Настройка RecyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        binding.mainRecycler.apply {
            adapter = filmsAdapter
            this.layoutManager = layoutManager
            addItemDecoration(TopSpacingItemDecoration(8))
        }

        // Инициализация пагинации
        paginationScrollListener = PaginationScrollListener(layoutManager) {
            loadNextPage()
        }
        binding.mainRecycler.addOnScrollListener(paginationScrollListener)

        // Настройка SearchView
        setupSearchView()

        // Настройка Swipe to Refresh
        setupSwipeRefresh()

        // Загрузка первой страницы
        loadFirstPage()
    }

    override fun onResume() {
        super.onResume()
        Log.d("HomeFragment", "🔄 onResume вызван")
        checkCategoryChange()
    }

    private fun checkCategoryChange() {
        val newCategory = interactor.getDefaultCategoryFromPreferences()
        Log.d("HomeFragment", "🔍 Проверка категории: текущая=$currentCategory, новая=$newCategory")

        if (newCategory != currentCategory && !isSearchMode) {
            Log.d("HomeFragment", "🔄 Категория изменилась! Перезагружаем данные")
            currentCategory = newCategory
            loadFirstPage()
        } else {
            Log.d("HomeFragment", "ℹ️ Категория не изменилась или режим поиска")
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Обновляем данные
            loadFirstPage()
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.length >= 2) {
                        performSearch(it)
                    } else {
                        Toast.makeText(requireContext(), "Введите минимум 2 символа", Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    when {
                        it.length >= 3 -> {
                            binding.searchView.postDelayed({
                                if (binding.searchView.query?.toString() == it) {
                                    performSearch(it)
                                }
                            }, 500)
                        }
                        it.isEmpty() -> {
                            resetToPopular()
                        }
                        else -> {
                            // Для строк длиной 1-2 символа ничего не делаем
                        }
                    }
                }
                return true
            }
        })
    }

    private fun performSearch(query: String) {
        if (query == currentSearchQuery) return

        currentSearchQuery = query
        isSearchMode = true
        loadFirstPage()

        // Показываем индикатор поиска
        binding.mainRecycler.visibility = View.GONE
    }

    private fun resetToPopular() {
        isSearchMode = false
        currentSearchQuery = ""
        // При сбросе поиска обновляем текущую категорию
        currentCategory = interactor.getDefaultCategoryFromPreferences()
        loadFirstPage()
    }

    private fun loadFirstPage() {
        currentPage = 1
        allFilms.clear()
        isLoading = true
        binding.mainRecycler.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = true

        if (isSearchMode && currentSearchQuery.isNotEmpty()) {
            Log.d("HomeFragment", "🔍 Начинаем поиск: '$currentSearchQuery'")
            interactor.searchFilms(currentSearchQuery, currentPage, object : Interactor.ApiCallback {
                override fun onSuccess(films: List<Film>, currentPage: Int, totalPages: Int) {
                    handleSuccess(films, currentPage, totalPages)
                }

                override fun onFailure(errorMessage: String?) {
                    handleFailure(errorMessage)
                }
            })
        } else {
            val category = interactor.getDefaultCategoryFromPreferences()
            Log.d("HomeFragment", "🎬 Загружаем фильмы категории: $category")
            interactor.getFilmsFromApi(currentPage, object : Interactor.ApiCallback {
                override fun onSuccess(films: List<Film>, currentPage: Int, totalPages: Int) {
                    handleSuccess(films, currentPage, totalPages)
                }

                override fun onFailure(errorMessage: String?) {
                    handleFailure(errorMessage)
                }
            })
        }
    }

    private fun handleSuccess(films: List<Film>, currentPage: Int, totalPages: Int) {
        isLoading = false
        paginationScrollListener.setLoading(false)
        binding.swipeRefreshLayout.isRefreshing = false

        this.currentPage = currentPage
        this.totalPages = totalPages

        // Очищаем список только при загрузке первой страницы
        if (currentPage == 1) {
            allFilms.clear()
        }
        allFilms.addAll(films)
        filmsAdapter.submitList(allFilms.toList())

        // Сохраняем фильмы в Database для избранного
        Database.addFilmsFromApi(films)

        // Показываем RecyclerView после загрузки
        binding.mainRecycler.visibility = View.VISIBLE

        val mode = if (isSearchMode) "поиска" else "категории ${interactor.getDefaultCategoryFromPreferences()}"
        Log.d("HomeFragment", "Загружена страница $currentPage из $totalPages ($mode), фильмов: ${allFilms.size}")
    }

    private fun handleFailure(errorMessage: String?) {
        isLoading = false
        paginationScrollListener.setLoading(false)
        binding.swipeRefreshLayout.isRefreshing = false

        Log.e("HomeFragment", "Ошибка загрузки: $errorMessage")
        binding.mainRecycler.visibility = View.VISIBLE
        Toast.makeText(requireContext(), "Ошибка загрузки: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    private fun loadNextPage() {
        if (isLoading || currentPage >= totalPages) return

        isLoading = true
        currentPage++
        binding.loadingProgress.visibility = View.VISIBLE

        Log.d("HomeFragment", "Загрузка страницы $currentPage...")

        val callback = object : Interactor.ApiCallback {
            override fun onSuccess(films: List<Film>, currentPage: Int, totalPages: Int) {
                isLoading = false
                paginationScrollListener.setLoading(false)
                binding.loadingProgress.visibility = View.GONE

                this@HomeFragment.currentPage = currentPage
                this@HomeFragment.totalPages = totalPages

                allFilms.addAll(films)
                Database.addFilmsFromApi(films)
                filmsAdapter.submitList(allFilms.toList())

                Log.d("HomeFragment", "Добавлено ${films.size} фильмов. Всего: ${allFilms.size}")
            }

            override fun onFailure(errorMessage: String?) {
                isLoading = false
                paginationScrollListener.setLoading(false)
                binding.loadingProgress.visibility = View.GONE
                currentPage-- // Откатываем номер страницы при ошибке

                Log.e("HomeFragment", "Ошибка загрузки страницы $currentPage: $errorMessage")
                Toast.makeText(requireContext(), "Ошибка загрузки: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }

        if (isSearchMode && currentSearchQuery.isNotEmpty()) {
            interactor.searchFilms(currentSearchQuery, currentPage, callback)
        } else {
            interactor.getFilmsFromApi(currentPage, callback)
        }
    }

    private fun testPreferences() {
        val testCategory = interactor.getDefaultCategoryFromPreferences()
        Log.d("HomeFragment", "🧪 Тест настроек: текущая категория = $testCategory")

        // Протестируем сохранение/чтение
        interactor.saveDefaultCategoryToPreferences("test_category")
        val savedCategory = interactor.getDefaultCategoryFromPreferences()
        Log.d("HomeFragment", "🧪 Тест сохранения: сохраненная категория = $savedCategory")

        // Вернем обратно
        interactor.saveDefaultCategoryToPreferences(testCategory)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mainRecycler.removeOnScrollListener(paginationScrollListener)
        _binding = null
    }
}