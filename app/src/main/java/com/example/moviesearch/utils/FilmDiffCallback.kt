package com.example.moviesearch.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.moviesearch.domain.Film

class FilmDiffCallback : DiffUtil.ItemCallback<Film>() {
    // Проверка на одинаковость объектов по уникальному индефикатору
    override fun areItemsTheSame(oldItem: Film, newItem: Film): Boolean {
        // Используем title как уникальный модификатор
        return oldItem.title == newItem.title && oldItem.posterUrl == newItem.posterUrl
    }

    // Проверка на идентичность содержимого объектов
    override fun areContentsTheSame(oldItem: Film, newItem: Film): Boolean {
        // Сравниваем все значимые поля, включая состояние избранного
        return oldItem.title == newItem.title &&
                oldItem.posterUrl == newItem.posterUrl &&
                oldItem.description == newItem.description &&
                oldItem.isInFavorites == newItem.isInFavorites
    }

    // Опционально: метод для эффективного обновления только изменённых полей
    override fun getChangePayload(oldItem: Film, newItem: Film): Any? {
        return if (oldItem.isInFavorites != newItem.isInFavorites) {
          // Просто передаём новое состояние
           newItem.isInFavorites
        } else {
            super.getChangePayload(oldItem, newItem)
        }
    }
}