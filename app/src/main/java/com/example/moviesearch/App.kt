package com.example.moviesearch

import android.app.Application
import com.example.moviesearch.data.MainRepository
import com.example.moviesearch.domain.Interactor

class App : Application() {
    private lateinit var repo: MainRepository
    private lateinit var interactor: Interactor


    override fun onCreate() {
        super.onCreate()
        instance = this

        // Инициализируем репозиторий и интерактор
        repo = MainRepository()
        interactor = Interactor(repo) // Теперь Interactor принимает только один параметр
    }

    // Добавляем геттеры, если нужно использовать извне
    fun getInteractor(): Interactor = interactor
    fun getRepository(): MainRepository = repo

    companion object {
        lateinit var instance: App
            private set
    }
}