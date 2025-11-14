package com.example.moviesearch.domain

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.moviesearch.data.MainRepository
import com.example.moviesearch.data.api.KinopoiskApi
import com.example.moviesearch.data.dto.KinopoiskResponse
import com.example.moviesearch.data.entity.Film
import com.example.moviesearch.data.network.RetrofitClient
import com.example.moviesearch.data.preferences.PreferenceProvider
import com.example.moviesearch.utils.Converter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    // НОВЫЙ МЕТОД: Получить список всех категорий
    fun getAvailableCategories(): List<Pair<String, String>> {
        return PreferenceProvider.CATEGORIES
    }

    interface ApiCallback {
        fun onSuccess(films: List<Film>, currentPage: Int, totalPages: Int)
        fun onFailure(errorMessage: String?)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFilmsFromApi(page: Int, callback: ApiCallback) {
        val category = getDefaultCategoryFromPreferences()

        // 🔄 ПРОВЕРКА ВАЛИДНОСТИ КЭША ДЛЯ ПЕРВОЙ СТРАНИЦЫ
        if (page == 1) {
            if (!preferences.isCacheValid()) {
                Log.d("Interactor", "🧹 Кэш устарел (>10 мин), очищаем БД")
                deleteAllFilmsFromDb()
                preferences.clearCacheTime()
            } else {
                Log.d("Interactor", "✅ Кэш валиден (<10 мин)")
            }
        }

        val call = when (category) {
            "popular" -> getPopularFilmsCall(page)
            "top_rated" -> getTopRatedFilmsCall(page)
            "recent" -> getRecentFilmsCall(page)
            "action" -> getActionFilmsCall(page)
            "comedy" -> getComedyFilmsCall(page)
            "drama" -> getDramaFilmsCall(page)
            "fantasy" -> getFantasyFilmsCall(page)
            "family" -> getFamilyFilmsCall(page)
            "thriller" -> getThrillerFilmsCall(page)
            "adventure" -> getAdventureFilmsCall(page)
            else -> getPopularFilmsCall(page)
        }

        call.enqueue(object : Callback<KinopoiskResponse> {
            override fun onResponse(call: Call<KinopoiskResponse>, response: Response<KinopoiskResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val films = Converter.convertApiListToDtoList(body?.docs)

                    //Сохраняем фильмы в БД при успешном ответе в корутине
                    CoroutineScope(Dispatchers.IO).launch {
                        repo.putToDb(films)

                        //СОХРАНЯЕМ ВРЕМЯ УСПЕШНОЙ ЗАГРУЗКИ ДЛЯ ПЕРВОЙ СТРАНИЦЫ
                        if (page == 1) {
                            preferences.saveLastLoadTime()
                            Log.d("Interactor", "Время загрузки сохранено")
                        }

                        withContext(Dispatchers.Main) {
                            callback.onSuccess(films, body?.page ?: page, body?.pages ?: 1)
                        }
                    }
                } else {
                    // При ошибке API пытаемся загрузить из БД в корутине
                    CoroutineScope(Dispatchers.IO).launch {
                        // 🔄 ПРОВЕРЯЕМ ВАЛИДНОСТЬ КЭША ПЕРЕД ПОКАЗОМ
                        if (preferences.isCacheValid()) {
                            val cachedFilms = repo.getAllFromDB()
                            withContext(Dispatchers.Main) {
                                if (cachedFilms.isNotEmpty()) {
                                    Log.d("Interactor", "Используем валидный кэш: ${cachedFilms.size} фильмов")
                                    callback.onSuccess(cachedFilms, 1, 1)
                                } else {
                                    handleApiError(response, callback)
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Log.d("Interactor", "Кэш невалиден, показываем ошибку")
                                handleApiError(response, callback)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<KinopoiskResponse>, t: Throwable) {
                Log.e("Interactor", "Сетевая ошибка: ${t.message}")

                // При сетевой ошибке загружаем из БД в корутине
                CoroutineScope(Dispatchers.IO).launch {
                    //ПРОВЕРЯЕМ ВАЛИДНОСТЬ КЭША ПЕРЕД ПОКАЗОМ
                    if (preferences.isCacheValid()) {
                        val cachedFilms = repo.getAllFromDB()
                        withContext(Dispatchers.Main) {
                            if (cachedFilms.isNotEmpty()) {
                                Log.d("Interactor", "Используем валидный кэш из-за сетевой ошибки: ${cachedFilms.size} фильмов")
                                callback.onSuccess(cachedFilms, 1, 1)
                            } else {
                                callback.onFailure("Сетевая ошибка: ${t.message}")
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.d("Interactor", "Кэш невалиден, показываем ошибку сети")
                            callback.onFailure("Сетевая ошибка: ${t.message}")
                        }
                    }
                }
            }
        })
    }

    fun searchFilms(query: String, page: Int, callback: ApiCallback) {
        val normalizedQuery = query.trim()

        if (normalizedQuery.length < 2) {
            callback.onSuccess(emptyList(), page, 1)
            return
        }

        //ДЛЯ ПОИСКА ТОЖЕ ПРОВЕРЯЕМ КЭШ ПЕРВОЙ СТРАНИЦЫ
        if (page == 1 && !preferences.isCacheValid()) {
            Log.d("Interactor", "Кэш устарел для поиска, очищаем БД")
            deleteAllFilmsFromDb()
            preferences.clearCacheTime()
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

                    // Сохраняем найденные фильмы в БД в корутине
                    CoroutineScope(Dispatchers.IO).launch {
                        repo.putToDb(relevantFilms)

                        // СОХРАНЯЕМ ВРЕМЯ УСПЕШНОЙ ЗАГРУЗКИ ДЛЯ ПЕРВОЙ СТРАНИЦЫ ПОИСКА
                        if (page == 1) {
                            preferences.saveLastLoadTime()
                        }

                        withContext(Dispatchers.Main) {
                            callback.onSuccess(relevantFilms, body?.page ?: page, body?.pages ?: 1)
                        }
                    }
                } else {
                    // При ошибке поиска пытаемся найти в кэше в корутине
                    CoroutineScope(Dispatchers.IO).launch {
                        // 🔄 ПРОВЕРЯЕМ ВАЛИДНОСТЬ КЭША
                        if (preferences.isCacheValid()) {
                            val cachedFilms = repo.getAllFromDB()
                            val searchResults = cachedFilms.filter { film ->
                                film.title.contains(normalizedQuery, ignoreCase = true) ||
                                        film.originalTitle?.contains(normalizedQuery, ignoreCase = true) == true ||
                                        film.alternativeName?.contains(normalizedQuery, ignoreCase = true) == true
                            }

                            withContext(Dispatchers.Main) {
                                if (searchResults.isNotEmpty()) {
                                    Log.d("Interactor", "Используем кэшированные результаты поиска: ${searchResults.size} фильмов")
                                    callback.onSuccess(searchResults, 1, 1)
                                } else {
                                    handleApiError(response, callback)
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                handleApiError(response, callback)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<KinopoiskResponse>, t: Throwable) {
                Log.e("Interactor", "Ошибка поиска: ${t.message}")

                // При сетевой ошибке поиска ищем в кэше в корутине
                CoroutineScope(Dispatchers.IO).launch {
                    // ПРОВЕРЯЕМ ВАЛИДНОСТЬ КЭША
                    if (preferences.isCacheValid()) {
                        val cachedFilms = repo.getAllFromDB()
                        val searchResults = cachedFilms.filter { film ->
                            film.title.contains(normalizedQuery, ignoreCase = true) ||
                                    film.originalTitle?.contains(normalizedQuery, ignoreCase = true) == true ||
                                    film.alternativeName?.contains(normalizedQuery, ignoreCase = true) == true
                        }

                        withContext(Dispatchers.Main) {
                            if (searchResults.isNotEmpty()) {
                                Log.d("Interactor", "Используем кэшированные результаты поиска: ${searchResults.size} фильмов")
                                callback.onSuccess(searchResults, 1, 1)
                            } else {
                                callback.onFailure("Ошибка поиска: ${t.message}")
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            callback.onFailure("Ошибка поиска: ${t.message}")
                        }
                    }
                }
            }
        })
    }

    // Обновляем фильм в БД
    fun updateFilmInDb(film: Film) {
        CoroutineScope(Dispatchers.IO).launch {
            repo.updateFilmInDb(film)
        }
    }

    // Удаляем фильм из БД по ID
    fun deleteFilmFromDb(filmId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            repo.deleteFilmFromDb(filmId)
        }
    }

    // Удаляем все фильмы из БД
    fun deleteAllFilmsFromDb() {
        CoroutineScope(Dispatchers.IO).launch {
            repo.deleteAllFilmsFromDb()
            Log.d("Interactor", "🗑Все фильмы удалены из БД")
        }
    }

    // Получаем фильмы по диапазону годов
    suspend fun getFilmsByYearRange(startYear: Int, endYear: Int): List<Film> {
        return repo.getFilmsByYearRange(startYear, endYear)
    }

    // Получаем фильмы по названию (поиск в БД)
    suspend fun getFilmsByTitle(title: String): List<Film> {
        return repo.getFilmsByTitle(title)
    }

    // Получаем количество фильмов в БД
    suspend fun getFilmsCount(): Int {
        return repo.getFilmsCount()
    }

    // Получаем фильмы по жанру из БД
    suspend fun getFilmsByGenre(genre: String): List<Film> = repo.getFilmsByGenre(genre)

    // Получаем фильмы с высоким рейтингом из БД
    suspend fun getHighRatedFilms(minRating: Double = 7.0): List<Film> = repo.getHighRatedFilms(minRating)

    // Получаем последние фильмы из БД
    suspend fun getRecentFilmsFromDB(limit: Int = 20): List<Film> = repo.getRecentFilms(limit)

    // Новый метод для получения фильмов из БД
    suspend fun getFilmsFromDB(): List<Film> = repo.getAllFromDB()

    // НОВЫЙ МЕТОД: Получить информацию о кэше
    fun getCacheInfo(): String {
        val lastLoadTime = preferences.getLastLoadTime()
        val isValid = preferences.isCacheValid()
        val timeDiff = if (lastLoadTime > 0) {
            (System.currentTimeMillis() - lastLoadTime) / 1000 / 60 // в минутах
        } else {
            -1
        }

        return "Кэш: ${if (isValid) "валиден" else "невалиден"} (${if (timeDiff >= 0) "$timeDiff мин назад" else "никогда"})"
    }

    fun quickSearch(query: String, callback: (List<Film>) -> Unit) {
        val normalizedQuery = query.trim()

        if (normalizedQuery.length < 2) {
            callback(emptyList())
            return
        }

        // ДЛЯ БЫСТРОГО ПОИСКА ТОЖЕ ПРОВЕРЯЕМ КЭШ
        if (!preferences.isCacheValid()) {
            Log.d("Interactor", "Кэш устарел для быстрого поиска, очищаем БД")
            deleteAllFilmsFromDb()
            preferences.clearCacheTime()
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

                    // Сохраняем результаты быстрого поиска в БД в корутине
                    CoroutineScope(Dispatchers.IO).launch {
                        repo.putToDb(relevantFilms)

                        // СОХРАНЯЕМ ВРЕМЯ УСПЕШНОЙ ЗАГРУЗКИ
                        preferences.saveLastLoadTime()

                        withContext(Dispatchers.Main) {
                            callback(relevantFilms)
                        }
                    }
                } else {
                    // При ошибке ищем в кэше в корутине
                    CoroutineScope(Dispatchers.IO).launch {
                        // ПРОВЕРЯЕМ ВАЛИДНОСТЬ КЭША
                        if (preferences.isCacheValid()) {
                            val cachedFilms = repo.getAllFromDB()
                            val searchResults = cachedFilms.filter { film ->
                                film.title.contains(normalizedQuery, ignoreCase = true) ||
                                        film.originalTitle?.contains(normalizedQuery, ignoreCase = true) == true ||
                                        film.alternativeName?.contains(normalizedQuery, ignoreCase = true) == true
                            }

                            withContext(Dispatchers.Main) {
                                callback(searchResults)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                callback(emptyList())
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<KinopoiskResponse>, t: Throwable) {
                // При сетевой ошибке ищем в кэше в корутине
                CoroutineScope(Dispatchers.IO).launch {
                    //ПРОВЕРЯЕМ ВАЛИДНОСТЬ КЭША
                    if (preferences.isCacheValid()) {
                        val cachedFilms = repo.getAllFromDB()
                        val searchResults = cachedFilms.filter { film ->
                            film.title.contains(normalizedQuery, ignoreCase = true) ||
                                    film.originalTitle?.contains(normalizedQuery, ignoreCase = true) == true ||
                                    film.alternativeName?.contains(normalizedQuery, ignoreCase = true) == true
                        }

                        withContext(Dispatchers.Main) {
                            callback(searchResults)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            callback(emptyList())
                        }
                    }
                }
            }
        })
    }

    // МЕТОДЫ ДЛЯ КАТЕГОРИЙ
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

    private fun getComedyFilmsCall(page: Int): Call<KinopoiskResponse> {
        return kinopoiskApi.getFilmsByCategory(
            apiKey = apiKey,
            page = page,
            year = "2010-2024",
            rating = "6-10",
            sortField = "votes.kp",
            sortType = "-1",
            type = "movie",
            genres = listOf("комедия")
        )
    }

    private fun getDramaFilmsCall(page: Int): Call<KinopoiskResponse> {
        return kinopoiskApi.getFilmsByCategory(
            apiKey = apiKey,
            page = page,
            year = "2010-2024",
            rating = "6-10",
            sortField = "votes.kp",
            sortType = "-1",
            type = "movie",
            genres = listOf("драма")
        )
    }

    private fun getFantasyFilmsCall(page: Int): Call<KinopoiskResponse> {
        return kinopoiskApi.getFilmsByCategory(
            apiKey = apiKey,
            page = page,
            year = "2010-2024",
            rating = "6-10",
            sortField = "votes.kp",
            sortType = "-1",
            type = "movie",
            genres = listOf("фэнтези")
        )
    }

    private fun getFamilyFilmsCall(page: Int): Call<KinopoiskResponse> {
        return kinopoiskApi.getFilmsByCategory(
            apiKey = apiKey,
            page = page,
            year = "2010-2024",
            rating = "5-10",
            sortField = "votes.kp",
            sortType = "-1",
            type = "movie",
            genres = listOf("семейный")
        )
    }


    private fun getThrillerFilmsCall(page: Int): Call<KinopoiskResponse> {
        return kinopoiskApi.getFilmsByCategory(
            apiKey = apiKey,
            page = page,
            year = "2010-2024",
            rating = "6-10",
            sortField = "votes.kp",
            sortType = "-1",
            type = "movie",
            genres = listOf("триллер")
        )
    }

    private fun getAdventureFilmsCall(page: Int): Call<KinopoiskResponse> {
        return kinopoiskApi.getFilmsByCategory(
            apiKey = apiKey,
            page = page,
            year = "2010-2024",
            rating = "6-10",
            sortField = "votes.kp",
            sortType = "-1",
            type = "movie",
            genres = listOf("приключения")
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
}
