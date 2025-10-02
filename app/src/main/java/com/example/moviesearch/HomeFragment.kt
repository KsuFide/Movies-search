package com.example.moviesearch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesearch.databinding.FragmentHomeBinding
import java.util.Locale

class HomeFragment : Fragment() {

    // View Binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var filmsAdapter: FilmListRecyclerAdapter
    private val allFilms = Data.films

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Запускаем анимацию circular reveal
        AnimationHelper.performFragmentCircularRevealAnimation(binding.homeFragmentRoot, requireActivity(), 1)

        // Убеждаемся, что корневое view видимо
        binding.homeFragmentRoot.visibility = View.VISIBLE

        // Настройка цвета текста в поиске
        val searchEditText = binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        // Настройка компонентов
        setupRecycler()
        setupSearchView()

        // Раскрытие поиска при клике
        binding.searchView.setOnClickListener {
            binding.searchView.isIconified = false
        }
    }

    private fun setupRecycler() {
        // Создание адаптера с обработчиком клика
        filmsAdapter = FilmListRecyclerAdapter { film ->
            (activity as? MainActivity)?.launchDetailsActivity(film)
        }

        // Настройка RecyclerView
        binding.mainRecycler.apply {
            adapter = filmsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(TopSpacingItemDecoration(8))

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
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchText = newText ?: ""
                if (searchText.isEmpty()) {
                    filmsAdapter.addItems(allFilms)
                } else {
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
        _binding = null
    }
}