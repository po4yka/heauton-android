package com.po4yka.heauton.presentation.screens.settings

import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * MVI Contract for Security Settings Screen.
 *
 * Manages security preferences including biometric authentication and encryption settings.
 */
object SecuritySettingsContract {

    /**
     * User intents for Security Settings screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Navigate back to settings.
         */
        data object NavigateBack : Intent

        /**
         * Check biometric availability on the device.
         */
        data object CheckBiometricAvailability : Intent

        /**
         * Toggle biometric authentication requirement.
         */
        data class ToggleBiometricAuth(val enabled: Boolean) : Intent

        /**
         * Test biometric authentication.
         */
        data object TestBiometric : Intent

        /**
         * Change auto-lock timeout.
         */
        data class ChangeAutoLockTimeout(val minutes: Int) : Intent

        /**
         * Toggle require auth for journal entries.
         */
        data class ToggleJournalAuthRequired(val enabled: Boolean) : Intent

        /**
         * Clear all encryption keys (requires confirmation).
         */
        data object ClearEncryptionKeys : Intent

        /**
         * Confirm clearing encryption keys (after user confirms).
         */
        data object ConfirmClearEncryptionKeys : Intent
    }

    /**
     * State for Security Settings screen.
     */
    data class State(
        val biometricAvailable: Boolean = false,
        val biometricStatusMessage: String = "Checking...",
        val biometricEnabled: Boolean = false,
        val journalAuthRequired: Boolean = false,
        val autoLockTimeout: Int = 5, // minutes
        val isLoading: Boolean = false
    ) : MviState

    /**
     * Side effects for Security Settings screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate back to settings.
         */
        data object NavigateBack : Effect

        /**
         * Show biometric authentication prompt.
         */
        data object ShowBiometricPrompt : Effect

        /**
         * Show confirmation dialog for clearing encryption keys.
         */
        data object ShowClearKeysConfirmation : Effect

        /**
         * Show message to user.
         */
        data class ShowMessage(val message: String) : Effect

        /**
         * Show error message.
         */
        data class ShowError(val error: String) : Effect
    }
}
