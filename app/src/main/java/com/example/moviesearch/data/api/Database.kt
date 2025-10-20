package com.example.moviesearch.data.api

import com.example.moviesearch.domain.Film

object Database {
    private val allFilms = mutableListOf<Film>()
    private val favoriteFilmIds = mutableSetOf<Int>()

    fun addFilmsFromApi(films: List<Film>) {
        // Добавляем только новые фильмы (проверяем по ID)
        films.forEach { newFilm ->
            if (allFilms.none { it.id == newFilm.id }) {
                allFilms.add(newFilm)
            }
        }
    }

    fun getFavoriteFilms(): List<Film> {
        return allFilms.filter { film -> favoriteFilmIds.contains(film.id) }
    }

    fun toggleFavorite(filmId: Int) {
        if (favoriteFilmIds.contains(filmId)) {
            favoriteFilmIds.remove(filmId)
        } else {
            favoriteFilmIds.add(filmId)
        }
    }

    fun isFavorite(filmId: Int): Boolean {
        return favoriteFilmIds.contains(filmId)
    }

    // Дополнительный метод для получения всех фильмов (если понадобится)
    fun getAllFilms(): List<Film> = allFilms.toList()
}