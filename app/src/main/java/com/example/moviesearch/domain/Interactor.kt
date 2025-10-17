package com.example.moviesearch.domain

import com.example.moviesearch.API
import com.example.moviesearch.data.MainRepository
import com.example.moviesearch.data.TmdbApi
import com.example.moviesearch.data.dto.TmdbResultsDto
import com.example.moviesearch.utils.Converter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Interactor(
    private val repo: MainRepository,
    private val retrofitService: TmdbApi
) {
    fun getFilmsFromApi(page: Int, callback: ApiCallback) {
        retrofitService.getPopularFilms(API.API_KEY, "ru-RU", page)
            .enqueue(object : Callback<TmdbResultsDto> {
                override fun onResponse(call: Call<TmdbResultsDto>, response: Response<TmdbResultsDto>) {
                    if (response.isSuccessful) {
                        val films = Converter.convertApiListToDtoList(response.body()?.tmdbFilms)
                        callback.onSuccess(films)
                    } else {
                        callback.onFailure()
                    }
                }

                override fun onFailure(call: Call<TmdbResultsDto>, t: Throwable) {
                    callback.onFailure()
                }
            })
    }

    // Старый метод для обратной совместимости
    fun getFilmsDB(): List<Film> = emptyList()

    interface ApiCallback {
        fun onSuccess(films: List<Film>)
        fun onFailure()
    }
}