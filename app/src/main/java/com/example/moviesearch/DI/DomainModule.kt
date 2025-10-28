package com.example.moviesearch.DI

import com.example.moviesearch.domain.IInteractor
import com.example.moviesearch.domain.Interactor
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface DomainModule {

    @Binds
    @Singleton
    fun bindInteractor(impl: Interactor): IInteractor
}