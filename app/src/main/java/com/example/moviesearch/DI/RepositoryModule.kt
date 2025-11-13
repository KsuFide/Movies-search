package com.example.moviesearch.DI

import com.example.moviesearch.data.MainRepository
import com.example.moviesearch.data.api.KinopoiskApi
import com.example.moviesearch.data.preferences.PreferenceProvider
import com.example.moviesearch.domain.Interactor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMainRepository(): MainRepository {
        return MainRepository()
    }

    @Provides
    @Singleton
    fun provideInteractor(
        repository: MainRepository,
        kinopoiskApi: KinopoiskApi,
        apiKey: String,
        preferences: PreferenceProvider
    ): Interactor {
        return Interactor(repository, kinopoiskApi, apiKey, preferences)
    }
}