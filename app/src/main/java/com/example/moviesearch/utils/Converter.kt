package com.example.moviesearch.utils

import android.util.Log
import com.example.moviesearch.data.dto.TmdbFilm
import com.example.moviesearch.domain.Film

object Converter {
    fun convertApiListToDtoList(list: List<TmdbFilm>?): List<Film> {
        val result = mutableListOf<Film>()

        if (list == null) {
            Log.w("DEBUG", "⚠️ Получен null список фильмов")
            return result
        }

        Log.d("DEBUG", "🔄 Конвертируем ${list.size} фильмов")

        list.forEachIndexed { index, tmdbFilm ->
            // Проверяем обязательные поля
            val title = tmdbFilm.title ?: "Без названия"
            val poster = tmdbFilm.posterPath ?: ""
            val description = tmdbFilm.overview ?: "Нет описания"
            val rating = tmdbFilm.voteAverage ?: 0.0

            Log.d("DEBUG", "Фильм $index: '$title', постер: $poster")

            result.add(Film(
                title = title,
                poster = poster,
                description = description,
                rating = rating,
                isInFavorites = false
            ))
        }

        Log.d("DEBUG", "✅ Конвертировано ${result.size} фильмов")
        return result
    }
}