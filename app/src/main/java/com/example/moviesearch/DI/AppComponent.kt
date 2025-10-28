package com.example.moviesearch.DI

import com.example.moviesearch.view.DetailsActivity
import com.example.moviesearch.view.MainActivity
import com.example.moviesearch.view.fragments.HomeFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        RemoteModule::class,
        DatabaseModule::class,
        DomainModule::class,
        ApiModule::class
    ]
)
interface AppComponent {
    fun inject(activity: DetailsActivity)
    fun inject(fragment: HomeFragment)
}