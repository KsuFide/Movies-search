package com.example.moviesearch

import androidx.recyclerview.widget.DiffUtil

class FilmDiffCallback : DiffUtil.ItemCallback<Film>() {
    // Проверка на одинаковость объектов по уникальному индефикатору
    override fun areItemsTheSame(oldItem: Film, newItem: Film): Boolean {
        return oldItem.title == newItem.title
    }

    // Проверка на идентичность содержимого объектов
    override fun areContentsTheSame(oldItem: Film, newItem: Film): Boolean {
        return oldItem == newItem
    }
}