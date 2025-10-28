package com.example.moviesearch.data

import com.example.moviesearch.domain.Film
import com.example.moviesearch.domain.IMainRepository
import javax.inject.Inject

class MainRepository @Inject constructor() : IMainRepository {
    private var filmsDataBase = mutableListOf<Film>()

    override fun updateFilms(newFilms: List<Film>) {
        filmsDataBase.clear()
        filmsDataBase.addAll(newFilms)
    }

    override fun getFilms(): List<Film> = filmsDataBase.toList()
}