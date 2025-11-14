package com.example.moviesearch.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.moviesearch.data.entity.Film

class FilmDiffCallback : DiffUtil.ItemCallback<Film>() {
    override fun areItemsTheSame(oldItem: Film, newItem: Film): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Film, newItem: Film): Boolean {
        return oldItem == newItem &&
                oldItem.isFavorite == newItem.isFavorite &&
                oldItem.isInFavorites == newItem.isInFavorites
    }
}