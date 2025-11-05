package com.example.moviesearch.domain

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.moviesearch.data.MainRepository
import com.example.moviesearch.data.api.KinopoiskApi
import com.example.moviesearch.data.dto.KinopoiskResponse
import com.example.moviesearch.data.network.RetrofitClient
import com.example.moviesearch.data.preferences.PreferenceProvider
import com.example.moviesearch.utils.Converter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class Interactor @Inject constructor(
    private val repo: MainRepository,
    private val kinopoiskApi: KinopoiskApi,
    private val apiKey: String,
    private val preferences: PreferenceProvider
) {

    fun saveDefaultCategoryToPreferences(category: String) {
        preferences.saveDefaultCategory(category)
    }

    fun getDefaultCategoryFromPreferences() = preferences.getDefaultCategory()

    interface ApiCallback {
        fun onSuccess(films: List<Film>, currentPage: Int, totalPages: Int)
        fun onFailure(errorMessage: String?)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFilmsFromApi(page: Int, callback: ApiCallback) {
        val category = getDefaultCategoryFromPreferences()

        val call = when (category) {
            "popular" -> getPopularFilmsCall(page)
            "top_rated" -> getTopRatedFilmsCall(page)
            "recent" -> getRecentFilmsCall(page)
            "action" -> getActionFilmsCall(page) // Новая категория
            else -> getPopularFilmsCall(page)
        }

        call.enqueue(object : Callback<KinopoiskResponse> {
            override fun onResponse(call: Call<KinopoiskResponse>, response: Response<KinopoiskResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val films = Converter.convertApiListToDtoList(body?.docs)
                    callback.onSuccess(films, body?.page ?: page, body?.pages ?: 1)
                } else {
                    handleApiError(response, callback)
                }
            }

            override fun onFailure(call: Call<KinopoiskResponse>, t: Throwable) {
                Log.e("Interactor", "Сетевая ошибка: ${t.message}")
                callback.onFailure("Сетевая ошибка: ${t.message}")
            }
        })
    }

    fun searchFilms(query: String, page: Int, callback: ApiCallback) {
        val normalizedQuery = query.trim()

        if (normalizedQuery.length < 2) {
            callback.onSuccess(emptyList(), page, 1)
            return
        }

        // Улучшенный поиск только фильмов с жанрами
        RetrofitClient.kinopoiskApi.searchFilmsOptimized(
            apiKey = RetrofitClient.getApiKey(),
            name = normalizedQuery,
            alternativeName = normalizedQuery,
            enName = normalizedQuery,
            page = page,
            type = "movie" // Гарантируем поиск только фильмов
        ).enqueue(object : Callback<KinopoiskResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<KinopoiskResponse>,
                response: Response<KinopoiskResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val rawFilms = body?.docs ?: emptyList()

                    // Фильтруем только фильмы (исключаем сериалы, аниме и т.д.)
                    val films = Converter.convertApiListToDtoList(rawFilms)
                    val relevantFilms = SearchEngine.smartFilmSearch(films, normalizedQuery)

                    callback.onSuccess(relevantFilms, body?.page ?: page, body?.pages ?: 1)
                } else {
                    handleApiError(response, callback)
                }
            }

            override fun onFailure(call: Call<KinopoiskResponse>, t: Throwable) {
                Log.e("Interactor", "Ошибка поиска: ${t.message}")
                callback.onFailure("Ошибка поиска: ${t.message}")
            }
        })
    }

    // Методы для разных категорий
    private fun getPopularFilmsCall(page: Int): Call<KinopoiskResponse> {
        return kinopoiskApi.getFilmsByCategory(
            apiKey = apiKey,
            page = page,
            year = "2020-2024",
            rating = "6-10",
            sortField = "votes.kp",
            sortType = "-1",
            type = "movie" // Только фильмы
        )
    }

    private fun getTopRatedFilmsCall(page: Int): Call<KinopoiskResponse> {
        return kinopoiskApi.getFilmsByCategory(
            apiKey = apiKey,
            page = page,
            year = "2010-2024",
            rating = "7-10",
            sortField = "rating.kp",
            sortType = "-1",
            type = "movie"
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getRecentFilmsCall(page: Int): Call<KinopoiskResponse> {
        val currentYear = java.time.Year.now().value
        return kinopoiskApi.getFilmsByCategory(
            apiKey = apiKey,
            page = page,
            year = "${currentYear - 1}-$currentYear",
            rating = "5-10",
            sortField = "year",
            sortType = "-1",
            type = "movie"
        )
    }

    // Новая категория - боевики
    private fun getActionFilmsCall(page: Int): Call<KinopoiskResponse> {
        return kinopoiskApi.getFilmsByCategory(
            apiKey = apiKey,
            page = page,
            year = "2010-2024",
            rating = "6-10",
            sortField = "votes.kp",
            sortType = "-1",
            type = "movie",

        )
    }

    private fun handleApiError(response: Response<*>, callback: ApiCallback) {
        val errorCode = response.code()
        val errorMessage = when (errorCode) {
            400 -> "Неверный запрос к API"
            401 -> "Неверный API ключ"
            403 -> "Доступ запрещен"
            429 -> "Слишком много запросов. Попробуйте позже."
            500 -> "Ошибка сервера Кинопоиска"
            else -> "Ошибка API: $errorCode"
        }
        callback.onFailure(errorMessage)
    }

    fun quickSearch(query: String, callback: (List<Film>) -> Unit) {
        val normalizedQuery = query.trim()

        if (normalizedQuery.length < 2) {
            callback(emptyList())
            return
        }

        RetrofitClient.kinopoiskApi.searchFilmsOptimized(
            apiKey = RetrofitClient.getApiKey(),
            name = normalizedQuery,
            alternativeName = normalizedQuery,
            enName = normalizedQuery,
            page = 1,
            limit = 10,
            type = "movie" // Только фильмы
        ).enqueue(object : Callback<KinopoiskResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<KinopoiskResponse>,
                response: Response<KinopoiskResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val films = Converter.convertApiListToDtoList(body?.docs)
                    val relevantFilms = SearchEngine.smartFilmSearch(films, normalizedQuery)
                    callback(relevantFilms)
                } else {
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<KinopoiskResponse>, t: Throwable) {
                callback(emptyList())
            }
        })
    }
}