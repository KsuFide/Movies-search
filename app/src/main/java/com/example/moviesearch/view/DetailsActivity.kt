package com.example.moviesearch.view

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.moviesearch.R
import com.example.moviesearch.data.api.Database
import com.example.moviesearch.databinding.FragmentDetailsBinding
import com.example.moviesearch.domain.Film
import com.example.moviesearch.domain.IInteractor
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject


class DetailsActivity : AppCompatActivity() {

    @Inject
    lateinit var interactor: IInteractor

    private lateinit var binding: FragmentDetailsBinding
    private lateinit var film: Film

    override fun onCreate(savedInstanceState: Bundle?) {

        (application as com.example.moviesearch.App).appComponent.inject(this)

        super.onCreate(savedInstanceState)
        binding = FragmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        film = intent.getParcelableExtra<Film>("film") ?: run {
            finish()
            return
        }

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
            detailsToolbar.title = film.title

            // Загрузка изображения через Glide
            Glide.with(this@DetailsActivity)
                .load(film.posterUrl)
                .centerCrop()
                .into(detailsPoster)

            // Устанавливаем описание
            detailsDescription.text = film.description ?: "Описание отсутствует"

            updateFavoriteButton()

            // Обработчик клика по кнопке избранного
            detailsFabFavorites.setOnClickListener {
                val wasFavorite = Database.isFavorite(film.id)
                Database.toggleFavorite(film.id)

                // Обновляем вид кнопки
                updateFavoriteButton()

                // Показываем уведомление о действии
                showSnackbar(
                    if (Database.isFavorite(film.id)) "Добавлено в избранное" else "Удалено из избранного",
                    "Отменить",
                    {
                        // Действие при отмене - возвращаем предыдущее состояние
                        Database.toggleFavorite(film.id)
                        updateFavoriteButton()
                    }
                )
            }

            // Обработчик клика по кнопке поделиться
            detailsFab.setOnClickListener {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Посмотрите этот фильм: ${film.title}\n\n${film.description}"
                    )
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(intent, "Поделиться через:"))
            }
        }
    }

    // Метод для обновления вида кнопки избранного
    private fun updateFavoriteButton() {
        // ИСПРАВЛЕНО: работаем с film.id
        val isFavorite = Database.isFavorite(film.id)
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
        undoAction: () -> Unit = {}
    ) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).apply {
            actionText?.let { text ->
                setAction(text) { undoAction() }
            }
            setAnchorView(binding.detailsFab)
        }.show()
    }
}