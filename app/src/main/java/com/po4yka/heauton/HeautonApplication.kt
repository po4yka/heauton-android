package com.po4yka.heauton

import android.app.Application
import com.po4yka.heauton.util.NotificationHelper
import com.po4yka.heauton.util.WidgetUpdateHelper
import com.po4yka.heauton.util.WorkManagerScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

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

    override fun onCreate() {
        super.onCreate()

        // Initialize notification channels
        notificationHelper.createNotificationChannels()

        // Schedule daily quote work
        workManagerScheduler.scheduleDailyQuoteWork()

        // Schedule periodic widget updates
        widgetUpdateHelper.schedulePeriodicWidgetUpdates()

        // Future: Initialize analytics, crash reporting, etc.
    }
}
