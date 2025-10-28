package com.example.moviesearch.DI

import com.example.moviesearch.data.MainRepository
import com.example.moviesearch.domain.IMainRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface DatabaseModule {

    @Binds
    @Singleton
    fun bindMainRepository(impl: MainRepository): IMainRepository
}