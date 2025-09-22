package com.example.moviesearch

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviesearch.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    // View Binding для доступа к элементам интерфейса
    private lateinit var binding: ActivityMainBinding

    // Адаптер для RecyclerView
    private lateinit var filmsAdapter: FilmListRecyclerAdapter

    // Флаги состояний для управления избранным и отложенным просмотром
    private var isFavorite = false
    private var isWatchLater = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка RecyclerView с лямбдой-обработчиком кликов
        filmsAdapter = FilmListRecyclerAdapter { film ->
            // Создание интента для перехода на экран деталей
            val intent = Intent(this, DetailsActivity::class.java).apply {
                putExtra("film", film) // Передача объекта Film
            }
            startActivity(intent)
        }

        // Конфигурация RecyclerView
        binding.mainRecycler.apply {
            adapter = filmsAdapter
            layoutManager = LinearLayoutManager(this@MainActivity) // Линейный макет
            addItemDecoration(TopSpacingItemDecoration(8)) // Декоратор для отступов
        }

        // Загрузка данных в RecyclerView
        filmsAdapter.submitList(Data.films)

        // Настройка BottomNavigationView
        with(binding.bottomNavigation) {
            // Установка активного и неактивного цветов программно
            val colorStates = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked), // Активное состояние
                    intArrayOf(-android.R.attr.state_checked) // Неактивное
                ),
                intArrayOf(
                    ContextCompat.getColor(context, R.color.white), // Активный цвет
                    ContextCompat.getColor(context, R.color.button) // Неактивный цвет
                )
            )
            itemIconTintList = colorStates
            itemTextColor = colorStates

            // Обработка выбора пунктов меню
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.favorites -> showSnackbar("Избранное")
                    R.id.watch_later -> showSnackbar("Посмотреть позже")
                    R.id.selections -> showSnackbar("Подборки")
                    else -> return@setOnItemSelectedListener false
                }
                true // Возвращаем true для подтверждения выбора
            }

            // Анимация при нажатии
            setOnItemSelectedListener { item ->
                animateItemSelection(item)
                true
            }
        }

        // Инициализация ToolBar
        binding.appBarLayout.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settings -> Toast.makeText(this, "Настройки", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    // Анимация выбранного элемента меню
    private fun animateItemSelection(item: MenuItem) {
        val view = findViewById<View>(item.itemId)
        view.animate()
            .scaleX(0.8f) // Уменьшение по X
            .scaleY(0.8f) // Уменьшение по Y
            .translationYBy(-20f) // Сдвиг вверх
            .setDuration(500) // Длительность анимации
            .withEndAction { // Обратный эффект после завершения
                view.animate()
                    .scaleX(1f) // Возврат к исходному размеру
                    .scaleY(1f)
                    .translationYBy(20f) // Возврат в исходную позицию
                    .setDuration(500)
            }
            .start()
    }

    // Показ всплывающих уведомлений
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}





