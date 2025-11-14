package com.po4yka.heauton.presentation.screens.settings

import android.os.Build
import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.data.local.preferences.UserPreferencesManager
import com.po4yka.heauton.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Appearance Settings Screen.
 *
 * Manages UI appearance preferences with DataStore persistence.
 */
@HiltViewModel
class AppearanceSettingsViewModel @Inject constructor(
    private val preferencesManager: UserPreferencesManager
) : MviViewModel<AppearanceSettingsContract.Intent, AppearanceSettingsContract.State, AppearanceSettingsContract.Effect>() {

    init {
        loadPreferences()
    }

    override fun createInitialState(): AppearanceSettingsContract.State {
        return AppearanceSettingsContract.State(
            dynamicColorsAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        )
    }

    /**
     * Load saved preferences from DataStore.
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesManager.appearancePreferences.collect { prefs ->
                updateState {
                    copy(
                        themeMode = AppearanceSettingsContract.ThemeMode.valueOf(prefs.themeMode),
                        dynamicColorsEnabled = prefs.dynamicColorsEnabled,
                        fontScale = AppearanceSettingsContract.FontScale.valueOf(prefs.fontScale),
                        animationsEnabled = prefs.animationsEnabled
                    )
                }
            }
        }
    }

    override fun handleIntent(intent: AppearanceSettingsContract.Intent) {
        when (intent) {
            is AppearanceSettingsContract.Intent.NavigateBack -> {
                sendEffect(AppearanceSettingsContract.Effect.NavigateBack)
            }

            is AppearanceSettingsContract.Intent.ChangeThemeMode -> {
                updateState { copy(themeMode = intent.mode) }
                viewModelScope.launch {
                    preferencesManager.setThemeMode(intent.mode.name)
                }
                sendEffect(
                    AppearanceSettingsContract.Effect.ShowMessage(
                        "Theme changed to ${intent.mode.displayName}"
                    )
                )
            }

            is AppearanceSettingsContract.Intent.ToggleDynamicColors -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    updateState { copy(dynamicColorsEnabled = intent.enabled) }
                    viewModelScope.launch {
                        preferencesManager.setDynamicColorsEnabled(intent.enabled)
                    }
                    sendEffect(
                        AppearanceSettingsContract.Effect.ShowMessage(
                            if (intent.enabled) "Dynamic colors enabled"
                            else "Dynamic colors disabled"
                        )
                    )
                } else {
                    sendEffect(
                        AppearanceSettingsContract.Effect.ShowMessage(
                            "Dynamic colors require Android 12 or higher"
                        )
                    )
                }
            }

            is AppearanceSettingsContract.Intent.ChangeFontScale -> {
                updateState { copy(fontScale = intent.scale) }
                viewModelScope.launch {
                    preferencesManager.setFontScale(intent.scale.name)
                }
                sendEffect(
                    AppearanceSettingsContract.Effect.ShowMessage(
                        "Font size changed to ${intent.scale.displayName}"
                    )
                )
            }

            is AppearanceSettingsContract.Intent.ToggleAnimations -> {
                updateState { copy(animationsEnabled = intent.enabled) }
                viewModelScope.launch {
                    preferencesManager.setAnimationsEnabled(intent.enabled)
                }
                sendEffect(
                    AppearanceSettingsContract.Effect.ShowMessage(
                        if (intent.enabled) "Animations enabled"
                        else "Animations disabled"
                    )
                )
            }
        }
    }
}
