package com.po4yka.heauton.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user preferences using DataStore.
 *
 * This class provides a centralized way to manage all user preferences across the app,
 * including appearance, notification, and security settings.
 */
@Singleton
class UserPreferencesManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

    /**
     * Preference keys for all settings.
     */
    private object PreferencesKeys {
        // Appearance Settings
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLORS_ENABLED = booleanPreferencesKey("dynamic_colors_enabled")
        val FONT_SCALE = stringPreferencesKey("font_scale")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")

        // Notification Settings
        val QUOTE_NOTIFICATIONS_ENABLED = booleanPreferencesKey("quote_notifications_enabled")
        val JOURNAL_REMINDERS_ENABLED = booleanPreferencesKey("journal_reminders_enabled")
        val EXERCISE_REMINDERS_ENABLED = booleanPreferencesKey("exercise_reminders_enabled")
        val NOTIFICATION_SOUND_ENABLED = booleanPreferencesKey("notification_sound_enabled")
        val NOTIFICATION_VIBRATION_ENABLED = booleanPreferencesKey("notification_vibration_enabled")

        // Security Settings
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val JOURNAL_AUTH_REQUIRED = booleanPreferencesKey("journal_auth_required")
        val AUTO_LOCK_TIMEOUT = intPreferencesKey("auto_lock_timeout")
    }

    // ========== Appearance Settings ==========

    /**
     * Get the current theme mode.
     */
    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
    }

    /**
     * Set the theme mode.
     */
    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    /**
     * Get whether dynamic colors are enabled.
     */
    val dynamicColorsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLORS_ENABLED] ?: true
    }

    /**
     * Set whether dynamic colors are enabled.
     */
    suspend fun setDynamicColorsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLORS_ENABLED] = enabled
        }
    }

    /**
     * Get the current font scale.
     */
    val fontScale: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FONT_SCALE] ?: "NORMAL"
    }

    /**
     * Set the font scale.
     */
    suspend fun setFontScale(scale: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SCALE] = scale
        }
    }

    /**
     * Get whether animations are enabled.
     */
    val animationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ANIMATIONS_ENABLED] ?: true
    }

    /**
     * Set whether animations are enabled.
     */
    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANIMATIONS_ENABLED] = enabled
        }
    }

    // ========== Notification Settings ==========

    /**
     * Get whether quote notifications are enabled.
     */
    val quoteNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.QUOTE_NOTIFICATIONS_ENABLED] ?: true
    }

    /**
     * Set whether quote notifications are enabled.
     */
    suspend fun setQuoteNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUOTE_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    /**
     * Get whether journal reminders are enabled.
     */
    val journalRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.JOURNAL_REMINDERS_ENABLED] ?: false
    }

    /**
     * Set whether journal reminders are enabled.
     */
    suspend fun setJournalRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.JOURNAL_REMINDERS_ENABLED] = enabled
        }
    }

    /**
     * Get whether exercise reminders are enabled.
     */
    val exerciseRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EXERCISE_REMINDERS_ENABLED] ?: false
    }

    /**
     * Set whether exercise reminders are enabled.
     */
    suspend fun setExerciseRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXERCISE_REMINDERS_ENABLED] = enabled
        }
    }

    /**
     * Get whether notification sound is enabled.
     */
    val notificationSoundEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATION_SOUND_ENABLED] ?: true
    }

    /**
     * Set whether notification sound is enabled.
     */
    suspend fun setNotificationSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_SOUND_ENABLED] = enabled
        }
    }

    /**
     * Get whether notification vibration is enabled.
     */
    val notificationVibrationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATION_VIBRATION_ENABLED] ?: true
    }

    /**
     * Set whether notification vibration is enabled.
     */
    suspend fun setNotificationVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_VIBRATION_ENABLED] = enabled
        }
    }

    // ========== Security Settings ==========

    /**
     * Get whether biometric authentication is enabled.
     */
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
    }

    /**
     * Set whether biometric authentication is enabled.
     */
    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
    }

    /**
     * Get whether authentication is required for journal entries.
     */
    val journalAuthRequired: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.JOURNAL_AUTH_REQUIRED] ?: false
    }

    /**
     * Set whether authentication is required for journal entries.
     */
    suspend fun setJournalAuthRequired(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.JOURNAL_AUTH_REQUIRED] = enabled
        }
    }

    /**
     * Get the auto-lock timeout in minutes.
     */
    val autoLockTimeout: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_LOCK_TIMEOUT] ?: 5
    }

    /**
     * Set the auto-lock timeout in minutes.
     */
    suspend fun setAutoLockTimeout(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_LOCK_TIMEOUT] = minutes
        }
    }

    /**
     * Get all appearance preferences as a flow.
     */
    val appearancePreferences: Flow<AppearancePreferences> = context.dataStore.data.map { preferences ->
        AppearancePreferences(
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
            dynamicColorsEnabled = preferences[PreferencesKeys.DYNAMIC_COLORS_ENABLED] ?: true,
            fontScale = preferences[PreferencesKeys.FONT_SCALE] ?: "NORMAL",
            animationsEnabled = preferences[PreferencesKeys.ANIMATIONS_ENABLED] ?: true
        )
    }

    /**
     * Get all notification preferences as a flow.
     */
    val notificationPreferences: Flow<NotificationPreferences> = context.dataStore.data.map { preferences ->
        NotificationPreferences(
            quoteNotificationsEnabled = preferences[PreferencesKeys.QUOTE_NOTIFICATIONS_ENABLED] ?: true,
            journalRemindersEnabled = preferences[PreferencesKeys.JOURNAL_REMINDERS_ENABLED] ?: false,
            exerciseRemindersEnabled = preferences[PreferencesKeys.EXERCISE_REMINDERS_ENABLED] ?: false,
            soundEnabled = preferences[PreferencesKeys.NOTIFICATION_SOUND_ENABLED] ?: true,
            vibrationEnabled = preferences[PreferencesKeys.NOTIFICATION_VIBRATION_ENABLED] ?: true
        )
    }

    /**
     * Get all security preferences as a flow.
     */
    val securityPreferences: Flow<SecurityPreferences> = context.dataStore.data.map { preferences ->
        SecurityPreferences(
            biometricEnabled = preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false,
            journalAuthRequired = preferences[PreferencesKeys.JOURNAL_AUTH_REQUIRED] ?: false,
            autoLockTimeout = preferences[PreferencesKeys.AUTO_LOCK_TIMEOUT] ?: 5
        )
    }
}

/**
 * Data class for appearance preferences.
 */
data class AppearancePreferences(
    val themeMode: String,
    val dynamicColorsEnabled: Boolean,
    val fontScale: String,
    val animationsEnabled: Boolean
)

/**
 * Data class for notification preferences.
 */
data class NotificationPreferences(
    val quoteNotificationsEnabled: Boolean,
    val journalRemindersEnabled: Boolean,
    val exerciseRemindersEnabled: Boolean,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean
)

/**
 * Data class for security preferences.
 */
data class SecurityPreferences(
    val biometricEnabled: Boolean,
    val journalAuthRequired: Boolean,
    val autoLockTimeout: Int
)
