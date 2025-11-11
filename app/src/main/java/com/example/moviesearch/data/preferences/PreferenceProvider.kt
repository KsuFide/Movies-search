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
            Log.d("PreferenceProvider", "🚀 Первый запуск, устанавливаем категорию по умолчанию")
            preference.edit {
                putString(KEY_DEFAULT_CATEGORY, DEFAULT_CATEGORY)
                putBoolean(KEY_FIRST_LAUNCH, false)
            }
        }

        val currentCategory = getDefaultCategory()
        Log.d("PreferenceProvider", "📁 Текущая категория: $currentCategory")
    }

    fun saveDefaultCategory(category: String) {
        Log.d("PreferenceProvider", "💾 Сохраняем категорию: $category")
        preference.edit {
            putString(KEY_DEFAULT_CATEGORY, category)
        }

        val saved = preference.getString(KEY_DEFAULT_CATEGORY, "ERROR")
        Log.d("PreferenceProvider", "✅ Проверка сохранения: $saved")
    }

    fun getDefaultCategory(): String {
        val category = preference.getString(KEY_DEFAULT_CATEGORY, DEFAULT_CATEGORY) ?: DEFAULT_CATEGORY
        Log.d("PreferenceProvider", "📖 Читаем категорию: $category")
        return category
    }

    companion object {
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_DEFAULT_CATEGORY = "default_category"
        private const val DEFAULT_CATEGORY = "popular"
    }
}