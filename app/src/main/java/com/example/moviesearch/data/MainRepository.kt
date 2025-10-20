package com.example.moviesearch.data

import com.example.moviesearch.domain.Film

class MainRepository {
    // Делаем изменяемым списком и добавляем метод обновления
    var filmsDataBase = mutableListOf<Film>()
        private set

    fun updateFilms(newFilms: List<Film>) {
        filmsDataBase.clear()
        filmsDataBase.addAll(newFilms)
    }

    fun getFilms(): List<Film> = filmsDataBase.toList()
}