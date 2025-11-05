package com.example.moviesearch.data.api

import com.example.moviesearch.data.dto.KinopoiskResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KinopoiskApi {

    // Универсальный метод для получения фильмов по категории
    @GET("v1.4/movie")
    fun getFilmsByCategory(
        @Header("X-API-KEY") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("selectFields") selectFields: List<String> = listOf(
            "id", "name", "alternativeName", "year", "description", "rating", "poster", "genres"
        ),
        @Query("type") type: String = "movie",
        @Query("year") year: String? = null,
        @Query("rating.kp") rating: String? = null,
        @Query("sortField") sortField: String? = null,
        @Query("sortType") sortType: String? = null,
        @Query("genres.name") genres: List<String>? = null // Фильтрация по жанрам
    ): Call<KinopoiskResponse>

    // Метод для популярных фильмов
    @GET("v1.4/movie")
    fun getPopularFilms(
        @Header("X-API-KEY") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20, // Загружаем по 20 фильмов за раз
        @Query("selectFields") selectFields: List<String> = listOf(
            "id",
            "name",
            "alternativeName",
            "year",
            "description",
            "rating",
            "poster",
            "genres"
        ),
        @Query("type") type: String = "movie",
        @Query("year") year: String = "2020-2024",
        @Query("rating.kp") rating: String = "6-10",
        @Query("sortField") sortField: String = "votes.kp", // Сортировка по популярности
        @Query("sortType") sortType: String = "-1" // По убыванию
    ): Call<KinopoiskResponse>


    // метод для поиска фильмов
    @GET("v1.4/movie")
    fun searchFilmsOptimized(
        @Header("X-API-KEY") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("selectFields") selectFields: List<String> = listOf(
            "id", "name", "alternativeName", "enName", "year",
            "description", "rating", "poster", "genres"
        ),
        @Query("query") query: String? = null, // Если API поддерживает общий поиск
        @Query("name") name: String? = null,
        @Query("alternativeName") alternativeName: String? = null,
        @Query("enName") enName: String? = null,
        @Query("type") type: String = "movie",
        @Query("year") year: String = "1990-2024",
        @Query("rating.kp") rating: String = "5-10",
        @Query("sortField") sortField: String = "votes.kp", // Сортировка по популярности
        @Query("sortType") sortType: String = "-1"
    ): Call<KinopoiskResponse>
}