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
import com.example.moviesearch.domain.IInteractor
import com.example.moviesearch.domain.Interactor
import com.example.moviesearch.utils.AnimationHelper
import com.example.moviesearch.utils.PaginationScrollListener
import com.example.moviesearch.view.MainActivity
import com.example.moviesearch.view.adapters.FilmListRecyclerAdapter
import com.example.moviesearch.view.adapters.TopSpacingItemDecoration
import javax.inject.Inject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var interactor: IInteractor

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity().application as com.example.moviesearch.App).appComponent.inject(this)

        AnimationHelper.performFragmentCircularRevealAnimation(binding.homeFragmentRoot, requireActivity(), 1)
        binding.homeFragmentRoot.visibility = View.VISIBLE

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

        // Загрузка первой страницы
        loadFirstPage()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            private var currentQuery: String? = null

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
                // Убираем лишние запросы - обрабатываем только реальные изменения
                if (newText == currentQuery) return false

                currentQuery = newText

                newText?.let {
                    when {
                        it.length >= 3 -> {
                            // Убираем задержку для более отзывчивого поиска
                            if (binding.searchView.query?.toString() == it) {
                                performSearch(it)
                            }
                        }
                        it.isEmpty() && isSearchMode -> {
                            // Только если был режим поиска и очистили
                            resetToPopular()
                        }
                    }
                }
                return true
            }
        })
    }

    private fun performSearch(query: String) {
        if (query == currentSearchQuery) return // Не ищем тот же запрос повторно

        currentSearchQuery = query
        isSearchMode = true
        loadFirstPage()

        // Показываем индикатор поиска
        binding.mainRecycler.visibility = View.GONE
    }

    private fun resetToPopular() {
        isSearchMode = false
        currentSearchQuery = ""
        loadFirstPage()
    }

    private fun loadFirstPage() {
        currentPage = 1
        allFilms.clear()
        isLoading = true
        binding.mainRecycler.visibility = View.GONE

        if (isSearchMode && currentSearchQuery.isNotEmpty()) {
            // Режим поиска
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
            // Режим популярных фильмов
            Log.d("HomeFragment", "🎬 Загружаем популярные фильмы")
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

        this.currentPage = currentPage
        this.totalPages = totalPages

        allFilms.addAll(films)
        filmsAdapter.submitList(allFilms.toList())

        // Сохраняем фильмы в Database для избранного
        Database.addFilmsFromApi(films)

        // Показываем RecyclerView после загрузки
        binding.mainRecycler.visibility = View.VISIBLE

        val mode = if (isSearchMode) "поиска" else "популярных"
        Log.d("HomeFragment", "Загружена страница $currentPage из $totalPages ($mode), фильмов: ${allFilms.size}")
    }

    private fun handleFailure(errorMessage: String?) {
        isLoading = false
        paginationScrollListener.setLoading(false)

        Log.e("HomeFragment", "Ошибка загрузки: $errorMessage")
        binding.mainRecycler.visibility = View.VISIBLE
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
            }
        }

        if (isSearchMode && currentSearchQuery.isNotEmpty()) {
            interactor.searchFilms(currentSearchQuery, currentPage, callback)
        } else {
            interactor.getFilmsFromApi(currentPage, callback)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mainRecycler.removeOnScrollListener(paginationScrollListener)
        _binding = null
    }
}