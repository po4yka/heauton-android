package com.po4yka.heauton.presentation.screens.settings

import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * MVI Contract for the Settings Screen.
 *
 * Main settings hub providing navigation to various settings sections.
 */
object SettingsContract {

    /**
     * User intents for the Settings screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Navigate to schedule settings.
         */
        data object NavigateToScheduleSettings : Intent

        /**
         * Navigate to security settings.
         */
        data object NavigateToSecuritySettings : Intent

        /**
         * Navigate to appearance settings.
         */
        data object NavigateToAppearanceSettings : Intent

        /**
         * Navigate to data management settings.
         */
        data object NavigateToDataSettings : Intent

        /**
         * Navigate to notification settings.
         */
        data object NavigateToNotificationSettings : Intent

        /**
         * Navigate to about screen.
         */
        data object NavigateToAbout : Intent

        /**
         * Check biometric availability.
         */
        data object CheckBiometricAvailability : Intent
    }

    /**
     * State for the Settings screen.
     */
    data class State(
        val appVersion: String = "",
        val isBiometricAvailable: Boolean = false,
        val isDarkTheme: Boolean = false,
        val notificationsEnabled: Boolean = true,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : MviState

    /**
     * Side effects for the Settings screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate back.
         */
        data object NavigateBack : Effect

        /**
         * Navigate to schedule settings screen.
         */
        data object NavigateToScheduleSettings : Effect

        /**
         * Navigate to security settings screen.
         */
        data object NavigateToSecuritySettings : Effect

        /**
         * Navigate to appearance settings screen.
         */
        data object NavigateToAppearanceSettings : Effect

        /**
         * Navigate to data management settings screen.
         */
        data object NavigateToDataSettings : Effect

        /**
         * Navigate to notification settings screen.
         */
        data object NavigateToNotificationSettings : Effect

        /**
         * Navigate to about screen.
         */
        data object NavigateToAbout : Effect

        /**
         * Show message to user.
         */
        data class ShowMessage(val message: String) : Effect
    }
}
