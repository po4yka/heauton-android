package com.po4yka.heauton.presentation.screens.settings

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.data.local.security.BiometricAuthManager
import com.po4yka.heauton.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Settings Screen.
 *
 * Manages settings state and coordinates navigation to sub-settings screens.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val biometricAuthManager: BiometricAuthManager
) : MviViewModel<SettingsContract.Intent, SettingsContract.State, SettingsContract.Effect>() {

    override fun createInitialState(): SettingsContract.State {
        return SettingsContract.State()
    }

    init {
        loadInitialSettings()
    }

    override fun handleIntent(intent: SettingsContract.Intent) {
        when (intent) {
            is SettingsContract.Intent.NavigateToScheduleSettings -> {
                sendEffect(SettingsContract.Effect.NavigateToScheduleSettings)
            }
            is SettingsContract.Intent.NavigateToSecuritySettings -> {
                sendEffect(SettingsContract.Effect.NavigateToSecuritySettings)
            }
            is SettingsContract.Intent.NavigateToAppearanceSettings -> {
                sendEffect(SettingsContract.Effect.NavigateToAppearanceSettings)
            }
            is SettingsContract.Intent.NavigateToDataSettings -> {
                sendEffect(SettingsContract.Effect.NavigateToDataSettings)
            }
            is SettingsContract.Intent.NavigateToNotificationSettings -> {
                sendEffect(SettingsContract.Effect.NavigateToNotificationSettings)
            }
            is SettingsContract.Intent.NavigateToAbout -> {
                sendEffect(SettingsContract.Effect.NavigateToAbout)
            }
            is SettingsContract.Intent.CheckBiometricAvailability -> {
                checkBiometricAvailability()
            }
        }
    }

    /**
     * Load initial settings values.
     */
    private fun loadInitialSettings() {
        viewModelScope.launch {
            try {
                val appVersion = getAppVersion()
                val isBiometricAvailable = biometricAuthManager.isBiometricAvailable()

                updateState {
                    copy(
                        appVersion = appVersion,
                        isBiometricAvailable = isBiometricAvailable
                    )
                }
            } catch (e: Exception) {
                updateState { copy(error = e.message ?: "Failed to load settings") }
            }
        }
    }

    /**
     * Get app version from package info.
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    /**
     * Check biometric availability.
     */
    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            val isAvailable = biometricAuthManager.isBiometricAvailable()
            updateState { copy(isBiometricAvailable = isAvailable) }

            if (!isAvailable) {
                val status = biometricAuthManager.checkBiometricAvailability()
                val message = biometricAuthManager.getStatusMessage(status)
                sendEffect(SettingsContract.Effect.ShowMessage(message))
            }
        }
    }

}
