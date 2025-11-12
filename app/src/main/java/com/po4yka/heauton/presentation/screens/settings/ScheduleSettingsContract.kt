package com.po4yka.heauton.presentation.screens.settings

import com.po4yka.heauton.data.local.database.entities.DeliveryMethod
import com.po4yka.heauton.domain.model.QuoteSchedule
import com.po4yka.heauton.presentation.base.MviEffect
import com.po4yka.heauton.presentation.base.MviIntent
import com.po4yka.heauton.presentation.base.MviState

/**
 * MVI Contract for Schedule Settings Screen.
 */
object ScheduleSettingsContract {

    /**
     * User intents for the schedule settings screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Load the default schedule.
         */
        data object LoadSchedule : Intent

        /**
         * Update enabled state.
         */
        data class UpdateEnabled(val isEnabled: Boolean) : Intent

        /**
         * Update scheduled time.
         */
        data class UpdateTime(val hour: Int, val minute: Int) : Intent

        /**
         * Update delivery method.
         */
        data class UpdateDeliveryMethod(val deliveryMethod: DeliveryMethod) : Intent

        /**
         * Test notification.
         */
        data object TestNotification : Intent

        /**
         * Navigate back.
         */
        data object NavigateBack : Intent
    }

    /**
     * State for the schedule settings screen.
     */
    data class State(
        val schedule: QuoteSchedule? = null,
        val isLoading: Boolean = true,
        val error: String? = null,
        val isSaving: Boolean = false,
        val isTestingNotification: Boolean = false
    ) : MviState {
        /**
         * Returns whether the schedule is enabled.
         */
        val isEnabled: Boolean
            get() = schedule?.isEnabled ?: false

        /**
         * Returns the scheduled hour.
         */
        val scheduledHour: Int
            get() = schedule?.scheduledHour ?: 9

        /**
         * Returns the scheduled minute.
         */
        val scheduledMinute: Int
            get() = schedule?.scheduledMinute ?: 0

        /**
         * Returns the delivery method.
         */
        val deliveryMethod: DeliveryMethod
            get() = schedule?.deliveryMethod ?: DeliveryMethod.BOTH

        /**
         * Returns formatted time string.
         */
        val formattedTime: String
            get() = schedule?.getFormattedTime() ?: "09:00"

        /**
         * Returns formatted time string (12-hour).
         */
        val formattedTime12Hour: String
            get() = schedule?.getFormattedTime12Hour() ?: "9:00 AM"

        /**
         * Returns next delivery description.
         */
        val nextDeliveryDescription: String?
            get() = schedule?.getTimeUntilNextDelivery()
    }

    /**
     * One-time effects for the schedule settings screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate back.
         */
        data object NavigateBack : Effect

        /**
         * Show success message.
         */
        data class ShowMessage(val message: String) : Effect

        /**
         * Show error message.
         */
        data class ShowError(val error: String) : Effect
    }
}
