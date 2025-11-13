package com.po4yka.heauton.presentation.screens.settings

import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * MVI Contract for Appearance Settings Screen.
 *
 * Manages UI appearance preferences including theme, colors, and typography.
 */
object AppearanceSettingsContract {

    /**
     * User intents for Appearance Settings screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Navigate back to settings.
         */
        data object NavigateBack : Intent

        /**
         * Change theme mode.
         */
        data class ChangeThemeMode(val mode: ThemeMode) : Intent

        /**
         * Toggle dynamic colors (Material You).
         */
        data class ToggleDynamicColors(val enabled: Boolean) : Intent

        /**
         * Change font scale.
         */
        data class ChangeFontScale(val scale: FontScale) : Intent

        /**
         * Toggle animations.
         */
        data class ToggleAnimations(val enabled: Boolean) : Intent
    }

    /**
     * State for Appearance Settings screen.
     */
    data class State(
        val themeMode: ThemeMode = ThemeMode.SYSTEM,
        val dynamicColorsEnabled: Boolean = true,
        val dynamicColorsAvailable: Boolean = false,
        val fontScale: FontScale = FontScale.NORMAL,
        val animationsEnabled: Boolean = true
    ) : MviState

    /**
     * Side effects for Appearance Settings screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate back to settings.
         */
        data object NavigateBack : Effect

        /**
         * Show message to user.
         */
        data class ShowMessage(val message: String) : Effect
    }

    /**
     * Theme mode options.
     */
    enum class ThemeMode(val displayName: String) {
        LIGHT("Light"),
        DARK("Dark"),
        SYSTEM("System Default")
    }

    /**
     * Font scale options.
     */
    enum class FontScale(val displayName: String, val scale: Float) {
        SMALL("Small", 0.85f),
        NORMAL("Normal", 1.0f),
        LARGE("Large", 1.15f),
        EXTRA_LARGE("Extra Large", 1.3f)
    }
}
