package com.po4yka.heauton.presentation.screens.settings

import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * MVI Contract for About Screen.
 *
 * Displays app information, version, credits, and licenses.
 */
object AboutContract {

    /**
     * User intents for About screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Navigate back to settings.
         */
        data object NavigateBack : Intent

        /**
         * Open GitHub repository.
         */
        data object OpenGitHub : Intent

        /**
         * Open privacy policy.
         */
        data object OpenPrivacyPolicy : Intent

        /**
         * Open terms of service.
         */
        data object OpenTermsOfService : Intent

        /**
         * Open open source licenses screen.
         */
        data object OpenLicenses : Intent
    }

    /**
     * State for About screen.
     */
    data class State(
        val appName: String = "Heauton",
        val versionName: String = "1.0.0",
        val versionCode: Int = 1,
        val buildType: String = "Debug"
    ) : MviState

    /**
     * Side effects for About screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate back to settings.
         */
        data object NavigateBack : Effect

        /**
         * Open URL in browser.
         */
        data class OpenUrl(val url: String) : Effect

        /**
         * Show message to user.
         */
        data class ShowMessage(val message: String) : Effect
    }
}
