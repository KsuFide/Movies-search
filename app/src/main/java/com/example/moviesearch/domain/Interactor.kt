package com.example.moviesearch.domain

import android.util.Log
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
    // Добавляем правильный интерфейс callback
    interface ApiCallback {
        fun onSuccess(films: List<Film>)
        fun onFailure()
    }

    fun getFilmsFromApi(page: Int, callback: ApiCallback) { // Исправляем тип на ApiCallback
        Log.d("DEBUG", "🔄 Отправляем запрос к API...")

        retrofitService.getPopularFilms(API.API_KEY, "ru-RU", page)
            .enqueue(object : Callback<TmdbResultsDto> {
                override fun onResponse(
                    call: Call<TmdbResultsDto>,
                    response: Response<TmdbResultsDto>
                ) {
                    Log.d("DEBUG", "📡 Получен ответ от API")
                    Log.d("DEBUG", "Код ответа: ${response.code()}")
                    Log.d("DEBUG", "Успешен ли запрос: ${response.isSuccessful}")

                    if (response.isSuccessful) {
                        val body = response.body()
                        Log.d("DEBUG", "Тело ответа: $body")
                        Log.d("DEBUG", "Количество фильмов в ответе: ${body?.tmdbFilms?.size ?: 0}")

                        val films = Converter.convertApiListToDtoList(body?.tmdbFilms)
                        Log.d("DEBUG", "После конвертации: ${films.size} фильмов")

                        callback.onSuccess(films)
                    } else {
                        Log.e("DEBUG", "❌ Ошибка API: ${response.code()} - ${response.message()}")
                        try {
                            Log.e("DEBUG", "Текст ошибки: ${response.errorBody()?.string()}")
                        } catch (e: Exception) {
                            Log.e("DEBUG", "Не удалось прочитать тело ошибки")
                        }
                        callback.onFailure()
                    }
                }

                override fun onFailure(call: Call<TmdbResultsDto>, t: Throwable) {
                    Log.e("DEBUG", "❌ Сетевая ошибка: ${t.message}")
                    t.printStackTrace()
                    callback.onFailure()
                }
            })
    }
}
