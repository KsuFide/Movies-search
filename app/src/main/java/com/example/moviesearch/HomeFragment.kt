package com.example.moviesearch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviesearch.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    // доступ к binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var filmsAdapter: FilmListRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Инициализация View Binding для фрагмента
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Настройка RecyclerView с лямбдой-обработчиком кликов
        filmsAdapter =
            FilmListRecyclerAdapter(object : FilmListRecyclerAdapter.OnItemClickListener {

                // Создание интента для перехода на экран деталей
                override fun click(film: Film) {
                    (activity as MainActivity).launchDetailsFragment(film)  // Передача объекта Film
                }
            })

        // Конфигурация RecyclerView
        binding.mainRecycler.apply {
            adapter = filmsAdapter
            layoutManager = LinearLayoutManager(requireContext()) // Линейный макет
            addItemDecoration(TopSpacingItemDecoration(8)) // Декоратор для отступов
        }
        loadData()
    }

    // Загрузка данных в RecyclerView
    private fun loadData() {
        filmsAdapter.submitList(Data.films)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}