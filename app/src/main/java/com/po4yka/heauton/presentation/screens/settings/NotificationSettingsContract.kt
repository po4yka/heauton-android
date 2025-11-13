package com.po4yka.heauton.presentation.screens.settings

import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * MVI Contract for Notification Settings Screen.
 *
 * Manages notification preferences for quotes, reminders, and app alerts.
 */
object NotificationSettingsContract {

    /**
     * User intents for Notification Settings screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Navigate back to settings.
         */
        data object NavigateBack : Intent

        /**
         * Toggle quote notifications.
         */
        data class ToggleQuoteNotifications(val enabled: Boolean) : Intent

        /**
         * Toggle journal reminders.
         */
        data class ToggleJournalReminders(val enabled: Boolean) : Intent

        /**
         * Toggle exercise reminders.
         */
        data class ToggleExerciseReminders(val enabled: Boolean) : Intent

        /**
         * Toggle notification sound.
         */
        data class ToggleNotificationSound(val enabled: Boolean) : Intent

        /**
         * Toggle notification vibration.
         */
        data class ToggleNotificationVibration(val enabled: Boolean) : Intent

        /**
         * Navigate to quote schedule settings.
         */
        data object NavigateToQuoteSchedule : Intent

        /**
         * Open system notification settings.
         */
        data object OpenSystemSettings : Intent
    }

    /**
     * State for Notification Settings screen.
     */
    data class State(
        val quoteNotificationsEnabled: Boolean = true,
        val journalRemindersEnabled: Boolean = false,
        val exerciseRemindersEnabled: Boolean = false,
        val soundEnabled: Boolean = true,
        val vibrationEnabled: Boolean = true,
        val isLoading: Boolean = false
    ) : MviState

    /**
     * Side effects for Notification Settings screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate back to settings.
         */
        data object NavigateBack : Effect

        /**
         * Navigate to quote schedule settings.
         */
        data object NavigateToQuoteSchedule : Effect

        /**
         * Open system notification settings.
         */
        data object OpenSystemSettings : Effect

        /**
         * Show message to user.
         */
        data class ShowMessage(val message: String) : Effect
    }
}
