package com.example.moviesearch

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.moviesearch.databinding.FragmentDetailsBinding
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.Snackbar

class DetailsFragment : Fragment() {

    // Объявляем переменную для binding
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    // Флаги состояний для кнопок
    private var isFavorite = false
    private var isWatchLater = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)


        // Скругляем иконку fab
        binding.detailsFab.shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, 100f)
            .build()

        // Получаем объект Film
        val film = arguments?.getParcelable<Film>("film") ?: run {
            showErrorAndClose()
            return binding.root
        }

        // Настройка элементов интерфейса
        with(binding) {
            // Устанавливаем заголовок
            detailsToolbar.title = film.title
            // Устанавливаем картинку
            detailsPoster.setImageResource(film.poster)
            // Устанавливаем описание
            detailsDescription.text = film.description

            // Обработчик кликов
            favoriteBtn.setOnClickListener { toggleFavorite() }
            watchLaterBtn.setOnClickListener { toggleWatchLater() }
            updateButtonAppearance()

        }
        return binding.root
    }

    // Обработчик клика для кнопки "Избранное"
    private fun toggleFavorite() {
        isFavorite = !isFavorite
        showSnackbar(
            if (isFavorite) "Добавлено в избранное" else "Удалено из избранного",
            if (isFavorite) "Отменить" else null
        ) { isFavorite = !isFavorite; updateButtonAppearance() }
        updateButtonAppearance()
    }

    // Обработчик клика для кнопки "Посмотреть позже"
    private fun toggleWatchLater() {
        isWatchLater = !isWatchLater
        showSnackbar(
            if (isWatchLater) "Добавлено в список" else "Удалено из списка",
            if (isWatchLater) "Отменить" else null
        ) { isWatchLater = !isWatchLater; updateButtonAppearance() }
        updateButtonAppearance()
    }

    // Обновление внешнего вида кнопок на основе текущих состояний
    private fun updateButtonAppearance() {
        with(binding) {
            favoriteBtn.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    if (isFavorite) R.color.red else R.color.button
                )
            )
            // Установка цвета для кнопки "Посмотреть позже"
            watchLaterBtn.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    if (isWatchLater) R.color.blue else R.color.button
                )
            )
        }
    }


    private fun showErrorAndClose() {
        Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }


    // Для цвета иконки (если захочу поменять)
    // binding.detailsFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black))

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}