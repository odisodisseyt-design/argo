package com.textworld.app

import android.app.Application
import com.textworld.app.di.ServiceLocator

class TextWorldApp : Application() {
    lateinit var locator: ServiceLocator
        private set

    override fun onCreate() {
        super.onCreate()
        locator = ServiceLocator(this)
    }
}
