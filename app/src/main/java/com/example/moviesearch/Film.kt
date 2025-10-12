package com.example.moviesearch

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class Film(
    val title: String,
    @DrawableRes val poster: Int, // ID ресурса постера (аннотация для проверки)
    val description: String, // Описание фильма
    var rating: Float = 0f, // Рейтинг фильма
    var isInFavorites: Boolean = false // Флаг избранного
) : Parcelable // Реализация Parcelable для передачи между компонентами