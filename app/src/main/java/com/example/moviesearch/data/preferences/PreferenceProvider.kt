package com.example.moviesearch.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceProvider @Inject constructor(@ApplicationContext context: Context) {
    private val appContext = context.applicationContext
    private val preference: SharedPreferences = appContext.getSharedPreferences("movie_settings", Context.MODE_PRIVATE)

    init {
        Log.d("PreferenceProvider", "🔄 Инициализация PreferenceProvider")

        if (preference.getBoolean(KEY_FIRST_LAUNCH, true)) {
            Log.d("PreferenceProvider", "Первый запуск, устанавливаем категорию по умолчанию")
            preference.edit {
                putString(KEY_DEFAULT_CATEGORY, DEFAULT_CATEGORY)
                putBoolean(KEY_FIRST_LAUNCH, false)
            }
        }

        val currentCategory = getDefaultCategory()
        Log.d("PreferenceProvider", "Текущая категория: $currentCategory")
    }

    fun saveDefaultCategory(category: String) {
        Log.d("PreferenceProvider", "Сохраняем категорию: $category")
        preference.edit {
            putString(KEY_DEFAULT_CATEGORY, category)
        }

        val saved = preference.getString(KEY_DEFAULT_CATEGORY, "ERROR")
        Log.d("PreferenceProvider", "Проверка сохранения: $saved")
    }

    fun getDefaultCategory(): String {
        val category = preference.getString(KEY_DEFAULT_CATEGORY, DEFAULT_CATEGORY) ?: DEFAULT_CATEGORY
        Log.d("PreferenceProvider", "Читаем категорию: $category")
        return category
    }

    // НОВЫЕ МЕТОДЫ ДЛЯ ВРЕМЕНИ КЭШИРОВАНИЯ
    fun saveLastLoadTime() {
        val currentTime = System.currentTimeMillis()
        Log.d("PreferenceProvider", "Сохраняем время последней загрузки: $currentTime")
        preference.edit {
            putLong(KEY_LAST_LOAD_TIME, currentTime)
        }
    }

    fun getLastLoadTime(): Long {
        return preference.getLong(KEY_LAST_LOAD_TIME, 0L)
    }

    fun isCacheValid(): Boolean {
        val lastLoadTime = getLastLoadTime()
        val currentTime = System.currentTimeMillis()
        val cacheDuration = 10 * 60 * 1000 // 10 минут в миллисекундах
        val isValid = (currentTime - lastLoadTime) <= cacheDuration

        Log.d("PreferenceProvider", "Проверка кэша: lastLoad=$lastLoadTime, current=$currentTime, valid=$isValid")

        return isValid
    }

    fun clearCacheTime() {
        Log.d("PreferenceProvider", "Очищаем время кэша")
        preference.edit {
            remove(KEY_LAST_LOAD_TIME)
        }
    }

    companion object {
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_DEFAULT_CATEGORY = "default_category"
        private const val KEY_LAST_LOAD_TIME = "last_load_time"
        private const val DEFAULT_CATEGORY = "popular"


        val CATEGORIES = listOf(
            "popular" to "Популярные",
            "top_rated" to "Высокий рейтинг",
            "recent" to "Новинки",
            "action" to "Боевики",
            "comedy" to "Комедии",
            "drama" to "Драмы",
            "fantasy" to "Фэнтези",
            "family" to "Семейные",
            "thriller" to "Триллеры",
            "adventure" to "Приключения"
        )
    }
}