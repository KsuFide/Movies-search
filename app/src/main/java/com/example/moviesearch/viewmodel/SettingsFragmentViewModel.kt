package com.example.moviesearch.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.example.moviesearch.domain.Interactor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsFragmentViewModel @Inject constructor(
    private val interactor: Interactor
) : ViewModel() {

    val categoryPropertyLiveData: MutableLiveData<String> = MutableLiveData()

    init {
        getCategoryProperty()
    }

    private fun getCategoryProperty() {
        categoryPropertyLiveData.value = interactor.getDefaultCategoryFromPreferences()
    }

    fun putCategoryProperty(category: String) {
        interactor.saveDefaultCategoryToPreferences(category)
        getCategoryProperty()
    }

    // Очистка кэша
    fun clearCache(context: Context): Boolean {
        return try {
            interactor.deleteAllFilmsFromDb(context)
            true
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Ошибка при очистке кэша: ${e.message}", e)
            false
        }
    }

    // Получение статистики кэша
    fun getCacheStats(context: Context): String {
        return try {
            val count = interactor.getFilmsCount(context)
            "В кэше: $count фильмов"
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Ошибка при получении статистики: ${e.message}", e)
            "Ошибка загрузки статистики"
        }
    }

    companion object {
        const val POPULAR_CATEGORY = "popular"
        const val TOP_RATED_CATEGORY = "top_rated"
        const val RECENT_CATEGORY = "recent"
    }
}