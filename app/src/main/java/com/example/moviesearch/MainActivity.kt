package com.example.moviesearch

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviesearch.Data.films
import com.example.moviesearch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var filmsAdapter: FilmListRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainRecycler.apply {

            // Инициализируем наш адаптер в конструктор передаём анонимно инициализированный интерфейс
            filmsAdapter =
                FilmListRecyclerAdapter(object : FilmListRecyclerAdapter.OnItemClickListener {
                    override fun click(film: Film) {
                        // Запускаем наше активити
                        val intent = Intent(this@MainActivity, DetailsActivity::class.java)
                        startActivity(intent)
                    }
                })

            // Присваиваем адаптер
            adapter = filmsAdapter
            // Присвоим layoutManager
            layoutManager = LinearLayoutManager(this@MainActivity)
            // Применяем декоратор для отступов
            val decoration = TopSpacingItemDecoration(8)
            addItemDecoration(decoration)
        }
        // Кладём нашу БД в RV
        filmsAdapter.addItems(films)


        // Инициализация BottomNavigationView
        val bottomNavigationView = binding.bottomNavigation
        // Установка активного и неактивного цветов программно
        val colorStates = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked), // Активное состояние
                intArrayOf(-android.R.attr.state_checked) // Неактивное
            ),
            intArrayOf(
                ContextCompat.getColor(this, R.color.white), // Активный цвет
                ContextCompat.getColor(this, R.color.button) // Неактивный цвет
            )

        )
        bottomNavigationView.itemIconTintList = colorStates
        bottomNavigationView.itemTextColor = colorStates
        bottomNavigationView.setOnItemSelectedListener { item ->
            val view = findViewById<View>(item.itemId)
            view.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .translationYBy(-20f)
                .setDuration(500)
                .withEndAction {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .translationYBy(20f)
                        .setDuration(500)
                }
                .start()

            when (item.itemId) {
                R.id.favorites -> {
                    Toast.makeText(this, "Избранное", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.watch_later -> {
                    Toast.makeText(this, "Посмотреть позже", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.selections -> {
                    Toast.makeText(this, "Подборки", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }

        // Инициализация ToolBar
        val topAppBar = binding.appBarLayout
        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settings -> {
                    Toast.makeText(this, "Настройки", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }
    }

    // Настройка кликов и анимации для CardView
    private fun setupPosterClick(cardView: CardView) {
        // Добавляем обработчик клика
        cardView.setOnClickListener { poster ->
            applyComboAnimation(poster) // Комбо-анимация
            Toast.makeText(this, "Клик по карточке!", Toast.LENGTH_SHORT).show()
        }
    }

    // Реализация метода applyComboAnimation
    private fun applyComboAnimation(view: View) {
        // Создаём анимации
        // Анимация увеличения и затемнения
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f).apply { duration = 1000 }
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f).apply { duration = 1000 }
        val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f).apply { duration = 1000 }

        // Анимация уменьшения и восстановления яркости
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.2f, 1f).apply { duration = 1000 }
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.2f, 1f).apply { duration = 1000 }
        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0.7f, 1f).apply { duration = 1000 }

        AnimatorSet().apply {
            // Сначала запускаем увеличение и затемнение вместе
            play(scaleUpX).with(scaleUpY).with(fadeOut)
            // Затем, после их завершения, запускаем уменьшение и восстановление
            play(scaleDownX).with(scaleDownY).with(fadeIn).after(scaleUpX)
            start()
        }
    }
}
