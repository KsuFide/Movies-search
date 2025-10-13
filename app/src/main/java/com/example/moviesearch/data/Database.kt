package com.example.moviesearch.data

import com.example.moviesearch.domain.Film

object Database {
    // Используем данные из MainRepository вместо Data
    private val allFilms = MainRepository().filmsDataBase.toMutableList()
    private val favoriteFilms = mutableListOf<Film>()

    // Получить все фильмы
    fun getAllFilms(): List<Film> = allFilms

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