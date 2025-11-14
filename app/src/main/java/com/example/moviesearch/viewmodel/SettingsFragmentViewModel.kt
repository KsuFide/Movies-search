package com.example.moviesearch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviesearch.domain.Interactor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsFragmentViewModel @Inject constructor(
    private val interactor: Interactor
) : ViewModel() {

    private val _filmsCount = MutableStateFlow(0)
    val filmsCount: StateFlow<Int> = _filmsCount

    fun loadFilmsCount() {
        viewModelScope.launch {
            try {
                // Временное решение - возвращаем 0
                _filmsCount.value = 0
            } catch (e: Exception) {
                _filmsCount.value = 0
            }
        }
    }
}