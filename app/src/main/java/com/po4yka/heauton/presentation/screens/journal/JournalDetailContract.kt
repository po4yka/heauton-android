package com.po4yka.heauton.presentation.screens.journal

import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * MVI Contract for Journal Detail Screen.
 *
 * ## Screen Purpose:
 * Display a journal entry with rendered Markdown and actions.
 *
 * ## Features:
 * - Rendered Markdown view
 * - Edit/Delete/Share actions
 * - Mood and tags display
 * - Related quote display
 * - Favorite and pin toggles
 * - Word count and metadata
 */
object JournalDetailContract {

    /**
     * User Intents for Journal Detail Screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Load entry by ID.
         */
        data class LoadEntry(val entryId: String) : Intent

        /**
         * Edit button clicked.
         */
        data object EditEntry : Intent

        /**
         * Delete button clicked.
         */
        data object DeleteEntry : Intent

        /**
         * Share button clicked.
         */
        data object ShareEntry : Intent

        /**
         * Toggle favorite status.
         */
        data object ToggleFavorite : Intent

        /**
         * Toggle pinned status.
         */
        data object TogglePinned : Intent

        /**
         * Related quote clicked.
         */
        data class QuoteClicked(val quoteId: String) : Intent
    }

    /**
     * UI State for Journal Detail Screen.
     */
    data class State(
        /**
         * The journal entry being displayed.
         */
        val entry: JournalEntry? = null,

        /**
         * Loading state.
         */
        val isLoading: Boolean = true,

        /**
         * Error message, if any.
         */
        val error: String? = null
    ) : MviState

    /**
     * One-time Effects for Journal Detail Screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate back.
         */
        data object NavigateBack : Effect

        /**
         * Navigate to edit screen.
         */
        data class NavigateToEdit(val entryId: String) : Effect

        /**
         * Navigate to quote detail.
         */
        data class NavigateToQuote(val quoteId: String) : Effect

        /**
         * Show delete confirmation dialog.
         */
        data class ShowDeleteConfirmation(val entryId: String, val title: String) : Effect

        /**
         * Show share sheet.
         */
        data class ShowShareSheet(val text: String) : Effect

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
