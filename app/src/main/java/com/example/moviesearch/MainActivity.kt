package com.example.moviesearch

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Анимация с использованием ObjectAnimator
        // Найдём постеры
        val poster1 = findViewById<CardView>(R.id.poster_1)
        val poster2 = findViewById<CardView>(R.id.poster_2)
        val poster3 = findViewById<CardView>(R.id.poster_3)
        val poster4 = findViewById<CardView>(R.id.poster_4)

        // Применяем анимацию и обработчик кликов к каждому постеру
        listOf(poster1, poster2, poster3, poster4).forEach { poster ->
            setupPosterClick(poster)
        }


        // Инициализация BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
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
        val topAppBar = findViewById<MaterialToolbar>(R.id.app_bar_layout)
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
        // Анимация увеличения
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f).apply {
            duration = 2000
        }
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f).apply {
            duration = 2000
        }
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f).apply {
            duration = 2000
        }

        // Анимация возврата в исходное положение
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.2f, 1f).apply {
            duration = 2000
        }
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.2f, 1f).apply {
            duration = 2000
        }
        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0.7f, 1f).apply {
            duration = 2000
        }

        AnimatorSet().apply {
            play(scaleX).with(scaleY).with(fadeIn) // Одновременно увеличиваем масштаб и уменьшаем прозрачность
           play(scaleDownX).with(scaleDownY).with(alpha) // Возвращаем после увеличения
            start()
        }
    }
}

//    private fun runClickAnimation(view: View) {
//        // Создаём анимацию уменьшения
//        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f)
//        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f)
//
//        // Создаём анимацию увеличения
//        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f)
//        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f)
//
//        // Анимация в одном наборе
//        val animatorSet = AnimatorSet()
//        animatorSet.play(scaleDownX).with(scaleDownY)
//        animatorSet.play(scaleUpX).with(scaleUpY).after(scaleDownX) // Увеличение после уменьшения
//
//        animatorSet.duration = 200 // Общая продолжительность анимации
//        animatorSet.start()
//    }
//}

