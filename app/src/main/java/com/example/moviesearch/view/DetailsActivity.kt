package com.example.moviesearch.view

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.moviesearch.data.api.Database
import com.example.moviesearch.R
import com.example.moviesearch.databinding.FragmentDetailsBinding
import com.example.moviesearch.data.entity.Film
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: FragmentDetailsBinding
    private lateinit var film: Film

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        film = intent.getParcelableExtra<Film>("film") ?: run {
            finish()
            return
        }

        // ОТСЛЕЖИВАЕМ ПРОСМОТР ФИЛЬМА в фоне
        lifecycleScope.launch(Dispatchers.IO) {
            Database.markAsWatched(film.id)
        }

        setupUI()
    }

    private fun setupUI() {
        with(binding) {
            // Устанавливаем заголовок - ТОЛЬКО ЭТО, без setSupportActionBar!
            detailsToolbar.title = film.title

            // Настройка кнопки "Назад" в Toolbar
            detailsToolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24)
            detailsToolbar.setNavigationOnClickListener {
                finish()
            }

            // Загрузка изображения через Glide
            Glide.with(this@DetailsActivity)
                .load(film.posterUrl)
                .centerCrop()
                .into(detailsPoster)

            // Устанавливаем описание
            detailsDescription.text = film.description ?: "Описание отсутствует"

            updateFavoriteButton()
            updateWatchLaterButton()

            setupClickListeners()
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            // Обработчик клика по кнопке избранного
            detailsFabFavorites.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    Database.toggleFavorite(film.id)
                    withContext(Dispatchers.Main) {
                        updateFavoriteButton()
                        showSnackbar(
                            if (Database.isFavorite(film.id)) "Добавлено в избранное" else "Удалено из избранного",
                            "Отменить",
                            {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    Database.toggleFavorite(film.id)
                                    withContext(Dispatchers.Main) {
                                        updateFavoriteButton()
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Обработчик кнопки "Посмотреть позже"
            detailsFabWatchLater.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    Database.toggleWatchLater(film.id)
                    withContext(Dispatchers.Main) {
                        updateWatchLaterButton()
                        showSnackbar(
                            if (Database.isWatchLater(film.id)) "Добавлено в 'Посмотреть позже'" else "Удалено из 'Посмотреть позже'",
                            "Отменить",
                            {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    Database.toggleWatchLater(film.id)
                                    withContext(Dispatchers.Main) {
                                        updateWatchLaterButton()
                                    }
                                }
                            }
                        )
                    }
                }
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

    // Метод: обновление кнопки "Посмотреть позже"
    private fun updateWatchLaterButton() {
        val isWatchLater = Database.isWatchLater(film.id)
        binding.detailsFabWatchLater.setImageResource(
            R.drawable.baseline_watch_later_24
        )
        binding.detailsFabWatchLater.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                this,
                if (isWatchLater) R.color.orange else R.color.white
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
