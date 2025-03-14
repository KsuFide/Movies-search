package com.example.moviesearch

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.moviesearch.databinding.ActivityDetailsBinding
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.Snackbar

class DetailsActivity : AppCompatActivity() {

    // Объявляем переменную для binding
    private lateinit var binding: ActivityDetailsBinding

    // Флаги состояний для кнопок
    private var isFavorite = false
    private var isWatchLater = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируем binding
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Скругляем иконку fab
        binding.detailsFab.shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, 100f)
            .build()

        // Получаем объект Film из интента
        val film = intent.getParcelableExtra<Film>("film") ?: return

        // Настройка элементов интерфейса
        with(binding) {
            // Устанавливаем заголовок
            detailsToolbar.title = film.title
            // Устанавливаем картинку
            detailsPoster.setImageResource(film.poster)
            // Устанавливаем описание
            detailsDescription.text = film.description

            // Обработчик кликов
            favoriteBtn.setOnClickListener { handleFavoriteClick() }
            watchLaterBtn.setOnClickListener { handleWatchLaterClick() }

        }
    }

//        // Для цвета иконки (если захочу поменять)
//        binding.detailsFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))

    // Обработчик клика для кнопки "Избранное"
    private fun handleFavoriteClick() {
        isFavorite = !isFavorite
        showSnackbar( // Обновляем внешний вид кнопок
            if (isFavorite) "Добавлено в избранное" else "Удалено из избранного",
            if (isFavorite) "Отменить" else null
        ) {
            // Действие при отмене
            isFavorite = !isFavorite
            updateButtonAppearance()
        }
    }

    // Обработчик клика для кнопки "Посмотреть позже"

    private fun handleWatchLaterClick() {
        isWatchLater = !isWatchLater
        updateButtonAppearance()
        showSnackbar( // Обновляем внешний вид кнопок
            if (isWatchLater) "Добавлено в список" else "Удалено из списка",
            if (isWatchLater) "Отменить" else null
        ) {
            isWatchLater = !isWatchLater
            updateButtonAppearance()
        }
    }

    // Обновление внешнего вида кнопок на основе текущих состояний
    private fun updateButtonAppearance() {
        with(binding) {
            // Установка цвета для кнопки "Избранное"
            favoriteBtn.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@DetailsActivity,
                    if (isFavorite) R.color.red else R.color.button
                )
            )

            // Установка цвета для кнопки "Посмотреть позже"
            watchLaterBtn.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@DetailsActivity,
                    if (isWatchLater) R.color.blue else R.color.button
                )
            )
        }
    }

    // Показать всплывающее уведомление (Snackbar)
    private fun showSnackbar(
        message: String,
        actionText: String?,
        undoAction: () -> Unit // Функция для отмены действия
    ) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).apply {
            actionText?.let { text ->
                // Добавляем кнопку отмены если нужно
                setAction(text) { undoAction() }
            }
            // Привязываем Snackbar к FAB для правильного позиционирования
            anchorView = binding.detailsFab
        }.show()
    }
}