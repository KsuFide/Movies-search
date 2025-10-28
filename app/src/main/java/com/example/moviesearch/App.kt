package com.example.moviesearch

import android.app.Application
import com.example.moviesearch.DI.AppComponent
import com.example.moviesearch.DI.DaggerAppComponent

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        instance = this

        appComponent = DaggerAppComponent.create()
    }

    companion object {
        lateinit var instance: App
            private set
    }
}