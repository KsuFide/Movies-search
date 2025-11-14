package com.example.moviesearch.DI

import android.content.Context
import com.example.moviesearch.data.MainRepository
import com.example.moviesearch.data.db.AppDatabase
import com.example.moviesearch.data.db.FilmDao
import com.example.moviesearch.data.api.KinopoiskApi
import com.example.moviesearch.data.network.RetrofitClient
import com.example.moviesearch.data.preferences.PreferenceProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideFilmDao(database: AppDatabase): FilmDao {
        return database.filmDao()
    }

    @Provides
    @Singleton
    fun provideRepository(filmDao: FilmDao): MainRepository {
        return MainRepository(filmDao)
    }

    @Provides
    @Singleton
    fun provideKinopoiskApi(): KinopoiskApi {
        return RetrofitClient.kinopoiskApi
    }

    @Provides
    @Singleton
    fun provideApiKey(): String {
        return RetrofitClient.getApiKey()
    }

    @Provides
    @Singleton
    fun providePreferenceProvider(@ApplicationContext context: Context): PreferenceProvider {
        return PreferenceProvider(context)
    }
}