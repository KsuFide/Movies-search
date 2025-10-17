package com.example.moviesearch.domain

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class Film(
    val title: String,
    val poster: String,
    val description: String, // Описание фильма
    var rating: Double = 0.0,  // Рейтинг фильма
    var isInFavorites: Boolean = false // Флаг избранного
) : Parcelable // Реализация Parcelable для передачи между компонентами