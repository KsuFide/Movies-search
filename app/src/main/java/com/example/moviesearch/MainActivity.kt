package com.example.moviesearch

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviesearch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // View Binding для доступа к элементам интерфейса
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Устанавливаем HomeFragment по умолчанию (только при первом создании)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, HomeFragment()) // Замена контейнера на HomeFragment
                .commit()
        }

        // Устанавливаем Home (иконка главное меню) активным по умолчанию
        binding.bottomNavigation.selectedItemId = R.id.home


        // Настройка нижнего меню
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            // Запускаем анимацию
            animateIcon(item)

            when (item.itemId) {
                R.id.favorites -> {
                    // Переход во фрагмент избранного
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, FavoritesFragment())
                        .commit()
                    true
                }

                R.id.home -> {
                    // Переход на главный фрагмент
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, HomeFragment())
                    true
                }

                else -> false // Неизвестный пункт - не обрабатываем
            }
        }
    }

    // Запуск активити с деталями фильма
    fun launchDetailsActivity(film: Film) {
        startActivity(Intent(this, DetailsActivity::class.java).apply {
            putExtra("film", film) // Передаём фильм через Intent
        })
    }

    // Анимация выбранного элемента меню
    private fun animateIcon(item: MenuItem) {
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

    private fun showFavorites() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, FavoritesFragment())
            .commit()
    }

    private fun showHome() {
        supportFragmentManager.findFragmentById(R.id.main_container)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
        // Возвращаем выбор на Home
        binding.bottomNavigation.selectedItemId = R.id.home
    }
}








