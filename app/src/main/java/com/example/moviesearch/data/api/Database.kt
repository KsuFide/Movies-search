package com.example.moviesearch.data.api

import com.example.moviesearch.data.entity.Film

object Database {
    private val allFilms = mutableListOf<Film>()
    private val favoriteFilmIds = mutableSetOf<Int>()
    private val watchLaterFilmIds = mutableSetOf<Int>()
    private val watchedFilmIds = mutableSetOf<Int>()

    fun addFilmsFromApi(films: List<Film>) {
        films.forEach { newFilm ->
            if (allFilms.none { it.id == newFilm.id }) {
                allFilms.add(newFilm)
            }
        }
    }

    fun getFavoriteFilms(): List<Film> {
        return allFilms.filter { film -> favoriteFilmIds.contains(film.id) }
    }

    fun getWatchLaterFilms(): List<Film> {
        return allFilms.filter { film -> watchLaterFilmIds.contains(film.id) }
    }

    fun getWatchedFilms(): List<Film> {
        return allFilms.filter { film -> watchedFilmIds.contains(film.id) }
    }

    fun toggleFavorite(filmId: Int) {
        if (favoriteFilmIds.contains(filmId)) {
            favoriteFilmIds.remove(filmId)
        } else {
            favoriteFilmIds.add(filmId)
        }
    }

    fun toggleWatchLater(filmId: Int) {
        if (watchLaterFilmIds.contains(filmId)) {
            watchLaterFilmIds.remove(filmId)
        } else {
            watchLaterFilmIds.add(filmId)
        }
    }

    fun markAsWatched(filmId: Int) {
        watchedFilmIds.add(filmId)
        // Убираем из "Посмотреть позже" если фильм просмотрен
        watchLaterFilmIds.remove(filmId)
    }

    fun isFavorite(filmId: Int): Boolean {
        return favoriteFilmIds.contains(filmId)
    }

    fun isWatchLater(filmId: Int): Boolean {
        return watchLaterFilmIds.contains(filmId)
    }

    fun isWatched(filmId: Int): Boolean {
        return watchedFilmIds.contains(filmId)
    }

    fun getAllFilms(): List<Film> = allFilms.toList()
}