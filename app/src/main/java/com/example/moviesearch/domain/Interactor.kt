package com.example.moviesearch.domain

import android.util.Log
import com.example.moviesearch.data.MainRepository
import com.example.moviesearch.data.network.RetrofitClient
import com.example.moviesearch.data.dto.KinopoiskResponse
import com.example.moviesearch.utils.Converter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Interactor(private val repo: MainRepository) {

    interface ApiCallback {
        fun onSuccess(films: List<Film>, currentPage: Int, totalPages: Int)
        fun onFailure(errorMessage: String?)
    }

    // Метод для получения популярных фильмов
    fun getFilmsFromApi(page: Int, callback: ApiCallback) {
        Log.d("Interactor", "🔄 Запрос популярных фильмов, страница $page...")

        RetrofitClient.kinopoiskApi.getPopularFilms(
            apiKey = RetrofitClient.getApiKey(),
            page = page
        ).enqueue(object : Callback<KinopoiskResponse> {
            override fun onResponse(
                call: Call<KinopoiskResponse>,
                response: Response<KinopoiskResponse>
            ) {
                Log.d("Interactor", "📡 Ответ от Кинопоиска для страницы $page")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("Interactor", "✅ Фильмов в ответе: ${body?.docs?.size ?: 0}")

                    val films = Converter.convertApiListToDtoList(body?.docs)
                    Log.d("Interactor", "🔄 После конвертации: ${films.size} фильмов")

                    // Отладка первых фильмов
                    films.take(2).forEachIndexed { index, film ->
                        Log.d("Interactor", "   🎬 ${index + 1}. '${film.title}' (${film.year}) - рейтинг: ${film.rating}")
                    }

                    callback.onSuccess(films, body?.page ?: page, body?.pages ?: 1)
                } else {
                    handleApiError(response, callback)
                }
            }

            override fun onFailure(call: Call<KinopoiskResponse>, t: Throwable) {
                Log.e("Interactor", "❌ Сетевая ошибка: ${t.message}")
                callback.onFailure("Сетевая ошибка: ${t.message}")
            }
        })
    }

    // УЛУЧШЕННЫЙ метод для поиска фильмов с детальной отладкой
    fun searchFilms(query: String, page: Int, callback: ApiCallback) {
        val normalizedQuery = query.trim()

        // Валидация запроса
        if (normalizedQuery.length < 2) {
            Log.w("Interactor", "⚠️ Слишком короткий запрос: '$normalizedQuery'")
            callback.onSuccess(emptyList(), page, 1)
            return
        }

        Log.d("Interactor", "🔍 Умный поиск: '$normalizedQuery', страница $page")

        // Используем все возможные поля для поиска
        RetrofitClient.kinopoiskApi.searchFilmsOptimized(
            apiKey = RetrofitClient.getApiKey(),
            name = normalizedQuery,           // Русское название
            alternativeName = normalizedQuery, // Оригинальное название
            enName = normalizedQuery,         // Английское название
            page = page
        ).enqueue(object : Callback<KinopoiskResponse> {
            override fun onResponse(
                call: Call<KinopoiskResponse>,
                response: Response<KinopoiskResponse>
            ) {
                Log.d("Interactor", "📡 Ответ поиска от Кинопоиска")

                if (response.isSuccessful) {
                    val body = response.body()
                    val rawFilms = body?.docs ?: emptyList()
                    Log.d("Interactor", "📊 API вернул ${rawFilms.size} фильмов для запроса '$normalizedQuery'")

                    // Конвертируем DTO в доменные объекты
                    val films = Converter.convertApiListToDtoList(rawFilms)
                    Log.d("Interactor", "🔄 После конвертации: ${films.size} фильмов")

                    // ДЕТАЛЬНАЯ ОТЛАДКА: смотрим что пришло от API
                    if (films.isEmpty()) {
                        Log.w("Interactor", "⚠️ API не вернул фильмов для запроса '$normalizedQuery'")
                    } else {
                        Log.d("Interactor", "🎯 АНАЛИЗ ПЕРВЫХ ФИЛЬМОВ ОТ API:")
                        films.take(5).forEachIndexed { index, film ->
                            Log.d("Interactor", "   ${index + 1}. '${film.title}'")
                            Log.d("Interactor", "      - originalTitle: '${film.originalTitle ?: "null"}'")
                            Log.d("Interactor", "      - alternativeName: '${film.alternativeName ?: "null"}'")
                            Log.d("Interactor", "      - год: ${film.year}, рейтинг: ${film.rating}")

                            // Проверяем совпадения вручную
                            val titleMatch = film.title?.lowercase()?.contains(normalizedQuery.lowercase()) == true
                            val originalMatch = film.originalTitle?.lowercase()?.contains(normalizedQuery.lowercase()) == true
                            val alternativeMatch = film.alternativeName?.lowercase()?.contains(normalizedQuery.lowercase()) == true

                            Log.d("Interactor", "      - совпадения: title=$titleMatch, original=$originalMatch, alternative=$alternativeMatch")
                        }
                    }

                    // ПРИМЕНЯЕМ УМНЫЙ ПОИСК
                    Log.d("Interactor", "🔍 Запускаем умный поиск...")
                    val relevantFilms = SearchEngine.smartFilmSearch(films, normalizedQuery)

                    Log.d("Interactor", "✅ После умного поиска: ${relevantFilms.size} релевантных фильмов")

                    // Логируем результаты умного поиска
                    if (relevantFilms.isNotEmpty()) {
                        Log.d("Interactor", "🏆 ТОП РЕЗУЛЬТАТЫ ПОИСКА:")
                        relevantFilms.take(5).forEachIndexed { index, film ->
                            val relevanceScore = SearchEngine.calculateRelevanceScore(film, normalizedQuery)
                            Log.d("Interactor", "   ${index + 1}. '${film.title}' - релевантность: $relevanceScore")
                            Log.d("Interactor", "      - ${SearchEngine.debugFilmSearch(film, normalizedQuery)}")
                        }
                    } else {
                        Log.w("Interactor", "⚠️ Умный поиск не нашел релевантных фильмов")

                        // Дополнительная диагностика - покажем почему фильмы не подошли
                        if (films.isNotEmpty()) {
                            Log.d("Interactor", "🔎 ДИАГНОСТИКА ПОЧЕМУ ФИЛЬМЫ НЕ ПОДОШЛИ:")
                            films.take(3).forEach { film ->
                                val debugInfo = SearchEngine.debugFilmSearch(film, normalizedQuery)
                                Log.d("Interactor", "   - '${film.title}': $debugInfo")
                            }
                        }
                    }

                    callback.onSuccess(relevantFilms, body?.page ?: page, body?.pages ?: 1)
                } else {
                    handleApiError(response, callback)
                }
            }

            override fun onFailure(call: Call<KinopoiskResponse>, t: Throwable) {
                Log.e("Interactor", "❌ Сетевая ошибка поиска: ${t.message}")
                callback.onFailure("Сетевая ошибка: ${t.message}")
            }
        })
    }

    // Улучшенная обработка ошибок API
    private fun handleApiError(response: Response<*>, callback: ApiCallback) {
        val errorCode = response.code()
        val errorMessage = when (errorCode) {
            400 -> "Неверный запрос к API (400)"
            401 -> "Неверный API ключ (401)"
            403 -> "Доступ запрещен (403)"
            429 -> "Слишком много запросов (429). Попробуйте позже."
            500 -> "Ошибка сервера Кинопоиска (500)"
            502 -> "Проблемы с соединением (502)"
            503 -> "Сервис временно недоступен (503)"
            else -> "Ошибка API: $errorCode"
        }

        Log.e("Interactor", "❌ Ошибка API: $errorMessage")

        // Дополнительная информация для отладки
        try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                Log.e("Interactor", "❌ Тело ошибки: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("Interactor", "❌ Не удалось прочитать тело ошибки")
        }

        callback.onFailure(errorMessage)
    }

    // Дополнительный метод для быстрого поиска (без пагинации)
    fun quickSearch(query: String, callback: (List<Film>) -> Unit) {
        val normalizedQuery = query.trim()

        if (normalizedQuery.length < 2) {
            callback(emptyList())
            return
        }

        Log.d("Interactor", "⚡ Быстрый поиск: '$normalizedQuery'")

        RetrofitClient.kinopoiskApi.searchFilmsOptimized(
            apiKey = RetrofitClient.getApiKey(),
            name = normalizedQuery,
            alternativeName = normalizedQuery,
            enName = normalizedQuery,
            page = 1,
            limit = 10 // Ограничиваем для быстрого ответа
        ).enqueue(object : Callback<KinopoiskResponse> {
            override fun onResponse(
                call: Call<KinopoiskResponse>,
                response: Response<KinopoiskResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val films = Converter.convertApiListToDtoList(body?.docs)
                    val relevantFilms = SearchEngine.smartFilmSearch(films, normalizedQuery)

                    Log.d("Interactor", "⚡ Быстрый поиск нашел: ${relevantFilms.size} фильмов")
                    callback(relevantFilms)
                } else {
                    Log.e("Interactor", "❌ Ошибка быстрого поиска: ${response.code()}")
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<KinopoiskResponse>, t: Throwable) {
                Log.e("Interactor", "❌ Сетевая ошибка быстрого поиска: ${t.message}")
                callback(emptyList())
            }
        })
    }
}