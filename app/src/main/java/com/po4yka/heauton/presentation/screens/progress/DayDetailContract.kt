package com.po4yka.heauton.presentation.screens.progress

import com.po4yka.heauton.domain.model.ExerciseSession
import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * MVI Contract for Day Detail Screen.
 *
 * Shows all activities for a specific calendar day.
 */
object DayDetailContract {

    /**
     * User intents for Day Detail screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Load activities for the specified date.
         */
        data class LoadDayActivities(val date: Long) : Intent

        /**
         * Navigate back to progress dashboard.
         */
        data object NavigateBack : Intent

        /**
         * Navigate to journal entry detail.
         */
        data class NavigateToJournalEntry(val entryId: String) : Intent

        /**
         * Navigate to exercise session detail (if applicable).
         */
        data class NavigateToExerciseSession(val sessionId: String) : Intent
    }

    /**
     * State for Day Detail screen.
     */
    data class State(
        val date: Long = 0L,
        val formattedDate: String = "",
        val journalEntries: List<JournalEntry> = emptyList(),
        val exerciseSessions: List<ExerciseSession> = emptyList(),
        val quoteViewCount: Int = 0,
        val totalActivities: Int = 0,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : MviState

    /**
     * Side effects for Day Detail screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate back to progress dashboard.
         */
        data object NavigateBack : Effect

        /**
         * Navigate to journal entry detail.
         */
        data class NavigateToJournalEntry(val entryId: String) : Effect

        /**
         * Show error message.
         */
        data class ShowError(val error: String) : Effect

        /**
         * Show success message.
         */
        data class ShowMessage(val message: String) : Effect
    }
}
