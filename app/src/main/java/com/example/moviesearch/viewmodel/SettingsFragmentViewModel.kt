package com.example.moviesearch.viewmodel

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
        // Получаем категорию при инициализации
        getCategoryProperty()
    }

    private fun getCategoryProperty() {
        // Кладем категорию в LiveData
        categoryPropertyLiveData.value = interactor.getDefaultCategoryFromPreferences()
    }

    fun putCategoryProperty(category: String) {
        // Сохраняем в настройки
        interactor.saveDefaultCategoryToPreferences(category)
        // И сразу забираем, чтобы сохранить состояние в модели
        getCategoryProperty()
    }
}