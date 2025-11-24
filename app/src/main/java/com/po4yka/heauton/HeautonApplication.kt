package com.po4yka.heauton

import android.app.Application
import com.po4yka.heauton.data.local.search.appsearch.AppSearchManager
import com.po4yka.heauton.di.ApplicationScope
import com.po4yka.heauton.util.NotificationHelper
import com.po4yka.heauton.util.WidgetUpdateHelper
import com.po4yka.heauton.util.WorkManagerScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Application class for Heauton wellness app.
 * Initializes Hilt dependency injection, notifications, scheduled work, and widgets.
 */
@HiltAndroidApp
class HeautonApplication : Application() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var workManagerScheduler: WorkManagerScheduler

    @Inject
    lateinit var widgetUpdateHelper: WidgetUpdateHelper

    @Inject
    lateinit var appSearchManager: AppSearchManager

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        // Initialize notification channels
        notificationHelper.createNotificationChannels()

        // Schedule daily quote work
        workManagerScheduler.scheduleDailyQuoteWork()

        // Schedule periodic widget updates
        widgetUpdateHelper.schedulePeriodicWidgetUpdates()

        applicationScope.launch {
            appSearchManager.initialize()
        }

        // Future: Initialize analytics, crash monitoring, etc.
    }
}
