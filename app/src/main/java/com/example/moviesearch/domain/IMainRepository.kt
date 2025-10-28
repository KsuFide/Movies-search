package com.example.moviesearch.domain

interface IMainRepository {
    fun getFilms(): List<Film>
    fun updateFilms(newFilms: List<Film>)
}