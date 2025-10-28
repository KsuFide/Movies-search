package com.example.moviesearch.DI

import com.example.moviesearch.API
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApiModule {

    @Provides
    @Singleton
    fun provideApiKey(): String = API.API_KEY
}