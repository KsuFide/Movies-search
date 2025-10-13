package com.example.moviesearch.view

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.moviesearch.data.Database
import com.example.moviesearch.R
import com.example.moviesearch.databinding.FragmentDetailsBinding
import com.example.moviesearch.domain.Film
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.Snackbar


class DetailsActivity : AppCompatActivity() {

    // Объявляем переменную для binding
    private lateinit var binding: FragmentDetailsBinding
    private lateinit var film: Film // Фильм, который просматривается

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Инициализируем binding
        binding = FragmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем объект Film из интента
        film = intent.getParcelableExtra<Film>("film") ?: run {
            finish() // Закрываем активити, если фильм передан
            return
        }


        // Проверяем статус избранного при создании активити
        film.isInFavorites = Database.isFavorite(film)

        // Скругляем иконку fab
        binding.detailsFab.shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, 100f)
            .build()
        binding.detailsFabFavorites.shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, 100f)
            .build()


        // Настройка элементов интерфейса
        with(binding) {
            // Устанавливаем заголовок
            detailsToolbar.title = film.title
            // Устанавливаем картинку
            detailsPoster.setImageResource(film.poster)
            // Устанавливаем описание
            detailsDescription.text = film.description


            // Обновляем вид кнопки согласно текущему состоянию
            updateFavoriteButton()

            // Обработчик клика по кнопке избранного
            detailsFabFavorites.setOnClickListener {
                val wasFavorite = Database.isFavorite(film)
                Database.toggleFavorite(film)
                // Обновляем вид кнопки
                updateFavoriteButton()
                // Показываем уведомление о действии
                showSnackbar(
                    if (Database.isFavorite(film)) "Добавлено в избранное" else "Удалено из избранного",
                    "Отменить", // Текст кнопки отмены
                    {
                        //Действие при отмене - возвращаем предыдущее состояние
                        Database.toggleFavorite(film)
                        updateFavoriteButton()
                    }
                )
            }

            // Обработчик клика по кнопке поделиться
            detailsFab.setOnClickListener {
                // Создаём интент для отправки данных
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND // Указываем тип действие
                    // Добавляем текст с информацией о фильме
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Посмотрите этот фильм: ${film.title}\n\n${film.description}"
                    )
                    type = "text/plain" // Указываем тип данных
                }
                // Запускаем диалог выбора приложения для отправки
                startActivity(Intent.createChooser(intent, "Поделиться через:"))
            }
        }
    }


    // Метод для обновления вида кнопки избранного
    private fun updateFavoriteButton() {
        val isFavorite = Database.isFavorite(film)
        binding.detailsFabFavorites.setImageResource(
            R.drawable.baseline_favorite_24
        )
        binding.detailsFabFavorites.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                this,
                if (isFavorite) R.color.red else R.color.white
            )
        )
    }



    // Показать всплывающее уведомление (Snackbar)
    private fun showSnackbar(
        message: String,
        actionText: String?,
        undoAction: () -> Unit = {} // Функция для отмены действия
    ) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).apply {
            actionText?.let { text ->
                // Добавляем кнопку отмены если нужно
                setAction(text) { undoAction() }
            }
            // Привязываем Snackbar к FAB для правильного позиционирования
            setAnchorView(binding.detailsFab)
        }.show()
    }
}