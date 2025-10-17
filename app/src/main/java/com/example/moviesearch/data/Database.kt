package com.example.moviesearch.data

import com.example.moviesearch.domain.Film

object Database {
    // Используем данные из MainRepository вместо Data
    private val allFilms = mutableListOf<Film>() // Теперь пустой mutable список
    private val favoriteFilms = mutableListOf<Film>()

    // Добавляем метод для добавления фильмов из API
    fun addFilmsFromApi(films: List<Film>) {
        allFilms.clear()
        allFilms.addAll(films)
    }

    // Получить избранные фильмы
    fun getFavoriteFilms(): List<Film> = favoriteFilms

    // Добавить/удалить из избранного
    fun toggleFavorite(film: Film) {
        val index = allFilms.indexOfFirst {
            it.title == film.title
        }
        if (index != -1) {
            allFilms[index].isInFavorites = !allFilms[index].isInFavorites
            if (allFilms[index].isInFavorites) {
                favoriteFilms.add(allFilms[index])
            } else {
                favoriteFilms.remove(allFilms[index])
            }
        }
    }

    // Проверить, есть ли фильм в избранном
    fun isFavorite(film: Film): Boolean {
        return favoriteFilms.any { it.title == film.title }
    }
}