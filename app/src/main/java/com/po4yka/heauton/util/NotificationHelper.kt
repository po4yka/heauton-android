package com.po4yka.heauton.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.po4yka.heauton.R
import com.po4yka.heauton.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for managing notifications.
 *
 * Handles notification channel creation, notification building,
 * and displaying notifications to the user.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    companion object {
        private const val CHANNEL_ID_DAILY_QUOTES = "daily_quotes"
        private const val CHANNEL_NAME_DAILY_QUOTES = "Daily Quotes"
        private const val CHANNEL_DESCRIPTION_DAILY_QUOTES = "Daily quote notifications"

        private const val NOTIFICATION_ID_DAILY_QUOTE = 1001

        private const val REQUEST_CODE_OPEN_APP = 100
        private const val REQUEST_CODE_FAVORITE = 101
        private const val REQUEST_CODE_JOURNAL = 102
    }

    /**
     * Creates notification channels (required for Android O+).
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_DAILY_QUOTES,
                CHANNEL_NAME_DAILY_QUOTES,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION_DAILY_QUOTES
                enableVibration(true)
                enableLights(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows a daily quote notification.
     *
     * @param quoteId ID of the quote to display
     * @param author Author of the quote
     * @param text Text of the quote
     */
    fun showDailyQuoteNotification(
        quoteId: String,
        author: String,
        text: String
    ) {
        // Create intent to open app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("quote_id", quoteId)
            putExtra("source", "notification")
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_DAILY_QUOTES)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Use custom icon
            .setContentTitle("Daily Quote")
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(text)
                    .setBigContentTitle("$author")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        // Show notification
        if (hasNotificationPermission()) {
            notificationManager.notify(NOTIFICATION_ID_DAILY_QUOTE, notification)
        }
    }

    /**
     * Shows a simple notification with title and text.
     */
    fun showSimpleNotification(
        title: String,
        text: String,
        notificationId: Int = NOTIFICATION_ID_DAILY_QUOTE
    ) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_APP,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_DAILY_QUOTES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .build()

        if (hasNotificationPermission()) {
            notificationManager.notify(notificationId, notification)
        }
    }

    /**
     * Cancels a notification by ID.
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    /**
     * Cancels all notifications.
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    /**
     * Checks if notification permission is granted (Android 13+).
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }

    /**
     * Checks if notification channel is enabled.
     */
    fun isChannelEnabled(channelId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = manager.getNotificationChannel(channelId)
            return channel?.importance != NotificationManager.IMPORTANCE_NONE
        }
        return true
    }
}
