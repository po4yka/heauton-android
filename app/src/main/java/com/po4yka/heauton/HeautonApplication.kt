package com.po4yka.heauton

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Heauton wellness app.
 * Initializes Hilt dependency injection.
 */
@HiltAndroidApp
class HeautonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Future: Initialize analytics, crash reporting, etc.
    }
}
