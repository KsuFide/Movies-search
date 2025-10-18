package com.example.moviesearch.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.moviesearch.App
import com.example.moviesearch.data.Database
import com.example.moviesearch.domain.Film
import com.example.moviesearch.domain.Interactor

class HomeFragmentViewModel : ViewModel() {
    val filmsListLiveData = MutableLiveData<List<Film>>()
    val errorLiveData = MutableLiveData<String>() // Для ошибок
    private val interactor = App.instance.interactor

    init {
        loadFilmsFromApi()
    }

    private fun loadFilmsFromApi() {
        interactor.getFilmsFromApi(1, object : Interactor.ApiCallback {
            override fun onSuccess(films: List<Film>) {
                filmsListLiveData.postValue(films)
                errorLiveData.postValue("") // Очищаем ошибку
            }

            override fun onFailure() {
                errorLiveData.postValue("Ошибка загрузки фильмов")
                filmsListLiveData.postValue(emptyList())
            }
        })
    }
}