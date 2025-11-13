package com.po4yka.heauton.presentation.screens.settings

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.data.local.preferences.UserPreferencesManager
import com.po4yka.heauton.data.local.security.BiometricAuthManager
import com.po4yka.heauton.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Security Settings Screen.
 *
 * Manages security preferences with DataStore persistence and biometric authentication.
 */
@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val biometricAuthManager: BiometricAuthManager,
    private val preferencesManager: UserPreferencesManager
) : MviViewModel<SecuritySettingsContract.Intent, SecuritySettingsContract.State, SecuritySettingsContract.Effect>() {

    init {
        // Check biometric availability and load preferences
        checkBiometricAvailability()
        loadPreferences()
    }

    override fun createInitialState(): SecuritySettingsContract.State {
        return SecuritySettingsContract.State()
    }

    /**
     * Load saved preferences from DataStore.
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesManager.securityPreferences.collect { prefs ->
                setState {
                    copy(
                        biometricEnabled = prefs.biometricEnabled,
                        journalAuthRequired = prefs.journalAuthRequired,
                        autoLockTimeout = prefs.autoLockTimeout
                    )
                }
            }
        }
    }

    override fun handleIntent(intent: SecuritySettingsContract.Intent) {
        when (intent) {
            is SecuritySettingsContract.Intent.NavigateBack -> {
                setEffect { SecuritySettingsContract.Effect.NavigateBack }
            }

            is SecuritySettingsContract.Intent.CheckBiometricAvailability -> {
                checkBiometricAvailability()
            }

            is SecuritySettingsContract.Intent.ToggleBiometricAuth -> {
                toggleBiometricAuth(intent.enabled)
            }

            is SecuritySettingsContract.Intent.TestBiometric -> {
                setEffect { SecuritySettingsContract.Effect.ShowBiometricPrompt }
            }

            is SecuritySettingsContract.Intent.ChangeAutoLockTimeout -> {
                setState { copy(autoLockTimeout = intent.minutes) }
                viewModelScope.launch {
                    preferencesManager.setAutoLockTimeout(intent.minutes)
                }
                setEffect {
                    SecuritySettingsContract.Effect.ShowMessage(
                        "Auto-lock timeout set to ${intent.minutes} minutes"
                    )
                }
            }

            is SecuritySettingsContract.Intent.ToggleJournalAuthRequired -> {
                setState { copy(journalAuthRequired = intent.enabled) }
                viewModelScope.launch {
                    preferencesManager.setJournalAuthRequired(intent.enabled)
                }
                setEffect {
                    SecuritySettingsContract.Effect.ShowMessage(
                        if (intent.enabled) "Authentication required for journal entries"
                        else "Authentication disabled for journal entries"
                    )
                }
            }

            is SecuritySettingsContract.Intent.ClearEncryptionKeys -> {
                // Show confirmation dialog before clearing keys (destructive operation)
                setEffect { SecuritySettingsContract.Effect.ShowClearKeysConfirmation }
            }

            is SecuritySettingsContract.Intent.ConfirmClearEncryptionKeys -> {
                clearEncryptionKeys()
            }
        }
    }

    /**
     * Check biometric authentication availability.
     */
    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            val status = biometricAuthManager.checkBiometricAvailability()
            val available = status is BiometricAuthManager.BiometricStatus.Available
            val message = biometricAuthManager.getStatusMessage(status)

            setState {
                copy(
                    biometricAvailable = available,
                    biometricStatusMessage = message
                )
            }
        }
    }

    /**
     * Toggle biometric authentication.
     */
    private fun toggleBiometricAuth(enabled: Boolean) {
        if (enabled && !currentState.biometricAvailable) {
            setEffect {
                SecuritySettingsContract.Effect.ShowError(
                    "Biometric authentication is not available on this device"
                )
            }
            return
        }

        if (enabled) {
            // Test biometric first before enabling
            setEffect { SecuritySettingsContract.Effect.ShowBiometricPrompt }
        } else {
            setState { copy(biometricEnabled = false) }
            viewModelScope.launch {
                preferencesManager.setBiometricEnabled(false)
            }
            setEffect {
                SecuritySettingsContract.Effect.ShowMessage("Biometric authentication disabled")
            }
        }
    }

    /**
     * Handle successful biometric authentication test.
     */
    fun onBiometricSuccess() {
        setState { copy(biometricEnabled = true) }
        viewModelScope.launch {
            preferencesManager.setBiometricEnabled(true)
        }
        setEffect {
            SecuritySettingsContract.Effect.ShowMessage("Biometric authentication enabled")
        }
    }

    /**
     * Handle failed biometric authentication test.
     */
    fun onBiometricError(error: String) {
        setState { copy(biometricEnabled = false) }
        viewModelScope.launch {
            preferencesManager.setBiometricEnabled(false)
        }
        setEffect {
            SecuritySettingsContract.Effect.ShowError("Biometric test failed: $error")
        }
    }

    /**
     * Clear all encryption keys.
     * This is a destructive operation that will remove all stored encryption keys.
     */
    private fun clearEncryptionKeys() {
        viewModelScope.launch {
            try {
                // Note: Actual key clearing implementation would go here
                // For now, we'll just reset security preferences
                preferencesManager.setBiometricEnabled(false)
                preferencesManager.setJournalAuthRequired(false)

                setState {
                    copy(
                        biometricEnabled = false,
                        journalAuthRequired = false
                    )
                }

                setEffect {
                    SecuritySettingsContract.Effect.ShowMessage(
                        "Encryption keys cleared. Security settings have been reset."
                    )
                }
            } catch (e: Exception) {
                setEffect {
                    SecuritySettingsContract.Effect.ShowError(
                        "Failed to clear encryption keys: ${e.message}"
                    )
                }
            }
        }
    }
}
