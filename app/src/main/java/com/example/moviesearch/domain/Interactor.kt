package com.example.moviesearch.domain

import android.content.Context
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
    fun getFilmsFromApi(page: Int, callback: ApiCallback, context: Context) {
        val category = getDefaultCategoryFromPreferences()

        val call = when (category) {
            "popular" -> getPopularFilmsCall(page)
            "top_rated" -> getTopRatedFilmsCall(page)
            "recent" -> getRecentFilmsCall(page)
            "action" -> getActionFilmsCall(page)
            else -> getPopularFilmsCall(page)
        }

        call.enqueue(object : Callback<KinopoiskResponse> {
            override fun onResponse(call: Call<KinopoiskResponse>, response: Response<KinopoiskResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val films = Converter.convertApiListToDtoList(body?.docs)

                    // Сохраняем фильмы в БД при успешном ответе
                    repo.putToDb(films, context)

                    callback.onSuccess(films, body?.page ?: page, body?.pages ?: 1)
                } else {
                    // При ошибке API пытаемся загрузить из БД
                    val cachedFilms = repo.getAllFromDB(context)
                    if (cachedFilms.isNotEmpty()) {
                        Log.d("Interactor", "📦 Используем кэшированные данные: ${cachedFilms.size} фильмов")
                        callback.onSuccess(cachedFilms, 1, 1)
                    } else {
                        handleApiError(response, callback)
                    }
                }
            }

            override fun onFailure(call: Call<KinopoiskResponse>, t: Throwable) {
                Log.e("Interactor", "Сетевая ошибка: ${t.message}")

                // При сетевой ошибке загружаем из БД
                val cachedFilms = repo.getAllFromDB(context)
                if (cachedFilms.isNotEmpty()) {
                    Log.d("Interactor", "📦 Используем кэшированные данные из-за сетевой ошибки: ${cachedFilms.size} фильмов")
                    callback.onSuccess(cachedFilms, 1, 1)
                } else {
                    callback.onFailure("Сетевая ошибка: ${t.message}")
                }
            }
        })
    }

    fun searchFilms(query: String, page: Int, callback: ApiCallback, context: Context) {
        val normalizedQuery = query.trim()

        if (normalizedQuery.length < 2) {
            callback.onSuccess(emptyList(), page, 1)
            return
        }

        RetrofitClient.kinopoiskApi.searchFilmsOptimized(
            apiKey = RetrofitClient.getApiKey(),
            name = normalizedQuery,
            alternativeName = normalizedQuery,
            enName = normalizedQuery,
            page = page,
            type = "movie"
        ).enqueue(object : Callback<KinopoiskResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<KinopoiskResponse>,
                response: Response<KinopoiskResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val rawFilms = body?.docs ?: emptyList()

                    val films = Converter.convertApiListToDtoList(rawFilms)
                    val relevantFilms = SearchEngine.smartFilmSearch(films, normalizedQuery)

                    // Сохраняем найденные фильмы в БД
                    repo.putToDb(relevantFilms, context)

                    callback.onSuccess(relevantFilms, body?.page ?: page, body?.pages ?: 1)
                } else {
                    // При ошибке поиска пытаемся найти в кэше
                    val cachedFilms = repo.getAllFromDB(context)
                    val searchResults = cachedFilms.filter { film ->
                        film.title.contains(normalizedQuery, ignoreCase = true) ||
                                film.originalTitle?.contains(normalizedQuery, ignoreCase = true) == true ||
                                film.alternativeName?.contains(normalizedQuery, ignoreCase = true) == true
                    }

                    if (searchResults.isNotEmpty()) {
                        Log.d("Interactor", "📦 Используем кэшированные результаты поиска: ${searchResults.size} фильмов")
                        callback.onSuccess(searchResults, 1, 1)
                    } else {
                        handleApiError(response, callback)
                    }
                }
            }

            override fun onFailure(call: Call<KinopoiskResponse>, t: Throwable) {
                Log.e("Interactor", "Ошибка поиска: ${t.message}")

                // При сетевой ошибке поиска ищем в кэше
                val cachedFilms = repo.getAllFromDB(context)
                val searchResults = cachedFilms.filter { film ->
                    film.title.contains(normalizedQuery, ignoreCase = true) ||
                            film.originalTitle?.contains(normalizedQuery, ignoreCase = true) == true ||
                            film.alternativeName?.contains(normalizedQuery, ignoreCase = true) == true
                }

                if (searchResults.isNotEmpty()) {
                    Log.d("Interactor", "📦 Используем кэшированные результаты поиска: ${searchResults.size} фильмов")
                    callback.onSuccess(searchResults, 1, 1)
                } else {
                    callback.onFailure("Ошибка поиска: ${t.message}")
                }
            }
        })
    }

    // Обновляем фильм в БД
    fun updateFilmInDb(film: Film, context: Context) {
        repo.updateFilmInDb(film, context)
    }

    // Удаляем фильм из БД по ID
    fun deleteFilmFromDb(filmId: Int, context: Context) {
        repo.deleteFilmFromDb(filmId, context)
    }

    // Удаляем все фильмы из БД
    fun deleteAllFilmsFromDb(context: Context) {
        repo.deleteAllFilmsFromDb(context)
    }

    // Получаем фильмы по диапазону годов
    fun getFilmsByYearRange(startYear: Int, endYear: Int, context: Context): List<Film> {
        return repo.getFilmsByYearRange(startYear, endYear, context)
    }

    // Получаем фильмы по названию (поиск в БД)
    fun getFilmsByTitle(title: String, context: Context): List<Film> {
        return repo.getFilmsByTitle(title, context)
    }


    // Получаем количество фильмов в БД
    fun getFilmsCount(context: Context): Int {
        return repo.getFilmsCount(context)
    }

    // Получаем фильмы по жанру из БД
    fun getFilmsByGenre(genre: String, context: Context): List<Film> = repo.getFilmsByGenre(genre, context)

    // Получаем фильмы с высоким рейтингом из БД
    fun getHighRatedFilms(minRating: Double = 7.0, context: Context): List<Film> = repo.getHighRatedFilms(minRating, context)

    // Получаем последние фильмы из БД
    fun getRecentFilmsFromDB(limit: Int = 20, context: Context): List<Film> = repo.getRecentFilms(limit, context)

    // Новый метод для получения фильмов из БД
    fun getFilmsFromDB(context: Context): List<Film> = repo.getAllFromDB(context)

    // Остальные методы остаются без изменений...
    private fun getPopularFilmsCall(page: Int): Call<KinopoiskResponse> {
        return kinopoiskApi.getFilmsByCategory(
            apiKey = apiKey,
            page = page,
            year = "2020-2024",
            rating = "6-10",
            sortField = "votes.kp",
            sortType = "-1",
            type = "movie"
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

    private fun getActionFilmsCall(page: Int): Call<KinopoiskResponse> {
        return kinopoiskApi.getFilmsByCategory(
            apiKey = apiKey,
            page = page,
            year = "2010-2024",
            rating = "6-10",
            sortField = "votes.kp",
            sortType = "-1",
            type = "movie",
            genres = listOf("боевик")
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

    fun quickSearch(query: String, callback: (List<Film>) -> Unit, context: Context) {
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
            type = "movie"
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

                    // Сохраняем результаты быстрого поиска в БД
                    repo.putToDb(relevantFilms, context)

                    callback(relevantFilms)
                } else {
                    // При ошибке ищем в кэше
                    val cachedFilms = repo.getAllFromDB(context)
                    val searchResults = cachedFilms.filter { film ->
                        film.title.contains(normalizedQuery, ignoreCase = true) ||
                                film.originalTitle?.contains(normalizedQuery, ignoreCase = true) == true ||
                                film.alternativeName?.contains(normalizedQuery, ignoreCase = true) == true
                    }
                    callback(searchResults)
                }
            }

            override fun onFailure(call: Call<KinopoiskResponse>, t: Throwable) {
                // При сетевой ошибке ищем в кэше
                val cachedFilms = repo.getAllFromDB(context)
                val searchResults = cachedFilms.filter { film ->
                    film.title.contains(normalizedQuery, ignoreCase = true) ||
                            film.originalTitle?.contains(normalizedQuery, ignoreCase = true) == true ||
                            film.alternativeName?.contains(normalizedQuery, ignoreCase = true) == true
                }
                callback(searchResults)
            }
        })
    }
}