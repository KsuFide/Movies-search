package com.example.moviesearch.domain

interface IInteractor {
    fun getFilmsFromApi(page: Int, callback: Interactor.ApiCallback)
    fun searchFilms(query: String, page: Int, callback: Interactor.ApiCallback)
    fun quickSearch(query: String, callback: (List<Film>) -> Unit)
}