package com.example.moviesearch

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.moviesearch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var lastSelectedItemId: Int = R.id.home // Текущий выбранный пункт

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Установка стартового фрагмента
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, HomeFragment())
                .commit()
        }

        binding.bottomNavigation.selectedItemId = R.id.home
        lastSelectedItemId = R.id.home

        // Обработчик навигации
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            // Анимация иконки
            animateIcon(item)

            // Определение анимации перехода
            val (enterAnim, exitAnim) = when {
                // Home -> Favorites: вход справа, выход влево
                item.itemId == R.id.favorites && lastSelectedItemId == R.id.home ->
                    Pair(R.anim.slide_in_right, R.anim.slide_out_left)

                // Favorites -> Home: вход слева, выход вправо
                item.itemId == R.id.home && lastSelectedItemId == R.id.favorites ->
                    Pair(R.anim.slide_in_left, R.anim.slide_out_right)

                // Для других случаев - плавное появление
                else -> Pair(R.anim.fade_in, R.anim.fade_out)
            }

            lastSelectedItemId = item.itemId // Обновление текущего пункта

            // Обработка выбора пункта меню
            when (item.itemId) {
                R.id.favorites -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(enterAnim, exitAnim) // Применение анимации
                        .replace(R.id.main_container, FavoritesFragment())
                        .commit()
                    true
                }

                R.id.home -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(enterAnim, exitAnim)
                        .replace(R.id.main_container, HomeFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }
    }

    // Запуск экрана деталей фильма
    fun launchDetailsActivity(film: Film) {
        startActivity(Intent(this, DetailsActivity::class.java).apply {
            putExtra("film", film) // Передача объекта фильма
        })
    }

    // Анимация иконки при нажатии
    private fun animateIcon(item: MenuItem) {
        val view = binding.bottomNavigation.findViewById<View>(item.itemId)
        view?.animate()
            ?.scaleX(0.8f)    // Уменьшение по X
            ?.scaleY(0.8f)    // Уменьшение по Y
            ?.translationYBy(-20f) // Сдвиг вверх
            ?.setDuration(500)
            ?.withEndAction {
                // Обратная анимация
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationYBy(20f)
                    .setDuration(500)
                    .start()
            }
            ?.start()
    }
}





