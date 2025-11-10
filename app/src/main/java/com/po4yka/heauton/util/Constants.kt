package com.po4yka.heauton.util

/**
 * Application-wide constants.
 */
object Constants {
    // Database
    const val DATABASE_NAME = "heauton.db"
    const val DATABASE_VERSION = 1

    // Search
    const val SEARCH_DEBOUNCE_MILLIS = 300L
    const val MIN_SEARCH_QUERY_LENGTH = 2

    // Pagination
    const val DEFAULT_PAGE_SIZE = 20

    // Auto-save
    const val AUTO_SAVE_DELAY_MILLIS = 30_000L // 30 seconds

    // Quotes
    const val MAX_QUOTE_TEXT_LENGTH = 10_000
    const val QUOTE_CHUNK_THRESHOLD = 5_000

    // Journal
    const val MAX_JOURNAL_CONTENT_LENGTH = 100_000

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "heauton_daily_quotes"
    const val NOTIFICATION_CHANNEL_NAME = "Daily Quotes"

    // Widget
    const val WIDGET_UPDATE_INTERVAL_MINUTES = 30L

    // Preferences
    const val PREFERENCES_NAME = "heauton_preferences"

    // Biometric
    const val BIOMETRIC_LOCK_TIMEOUT_MILLIS = 300_000L // 5 minutes
}
