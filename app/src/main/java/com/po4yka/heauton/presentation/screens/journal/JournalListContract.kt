package com.po4yka.heauton.presentation.screens.journal

import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * MVI Contract for Journal List Screen.
 *
 * ## Screen Purpose:
 * Display list of journal entries with search, filtering, and quick actions.
 *
 * ## Features:
 * - Timeline view of entries (newest first)
 * - Search functionality
 * - Mood filtering
 * - Favorite filtering
 * - Pull-to-refresh
 * - Entry actions (favorite, pin, delete)
 * - Streak display
 */
object JournalListContract {

    /**
     * User Intents for Journal List Screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Load all journal entries.
         */
        data object LoadEntries : Intent

        /**
         * Search query changed.
         */
        data class SearchQueryChanged(val query: String) : Intent

        /**
         * Filter by mood changed.
         */
        data class MoodFilterChanged(val mood: String?) : Intent

        /**
         * Toggle showing only favorites.
         */
        data object ToggleFavoritesFilter : Intent

        /**
         * Entry clicked - navigate to detail.
         */
        data class EntryClicked(val entryId: String) : Intent

        /**
         * Create new entry button clicked.
         */
        data object CreateEntryClicked : Intent

        /**
         * Toggle favorite status of an entry.
         */
        data class ToggleFavorite(val entryId: String, val isFavorite: Boolean) : Intent

        /**
         * Toggle pinned status of an entry.
         */
        data class TogglePinned(val entryId: String, val isPinned: Boolean) : Intent

        /**
         * Delete entry (with confirmation in UI).
         */
        data class DeleteEntry(val entryId: String) : Intent

        /**
         * Refresh entries (pull-to-refresh).
         */
        data object Refresh : Intent

        /**
         * Get random prompt clicked.
         */
        data object GetRandomPrompt : Intent

        /**
         * Dismiss error message.
         */
        data object DismissError : Intent
    }

    /**
     * UI State for Journal List Screen.
     */
    data class State(
        /**
         * List of journal entries to display.
         */
        val entries: List<JournalEntry> = emptyList(),

        /**
         * Current search query.
         */
        val searchQuery: String = "",

        /**
         * Selected mood filter (null = show all).
         */
        val moodFilter: String? = null,

        /**
         * Whether to show only favorite entries.
         */
        val showOnlyFavorites: Boolean = false,

        /**
         * Current journaling streak (consecutive days).
         */
        val currentStreak: Int = 0,

        /**
         * Longest journaling streak.
         */
        val longestStreak: Int = 0,

        /**
         * Total number of entries.
         */
        val totalEntries: Int = 0,

        /**
         * Loading state.
         */
        val isLoading: Boolean = true,

        /**
         * Refreshing state (pull-to-refresh).
         */
        val isRefreshing: Boolean = false,

        /**
         * Error message, if any.
         */
        val error: String? = null
    ) : MviState {
        /**
         * Whether any filters are active.
         */
        val hasActiveFilters: Boolean
            get() = searchQuery.isNotBlank() || moodFilter != null || showOnlyFavorites

        /**
         * Whether the list is empty after filtering.
         */
        val isEmptyAfterFiltering: Boolean
            get() = entries.isEmpty() && !isLoading && hasActiveFilters

        /**
         * Whether there are no entries at all.
         */
        val isEmptyState: Boolean
            get() = entries.isEmpty() && !isLoading && !hasActiveFilters
    }

    /**
     * One-time Effects for Journal List Screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate to entry detail screen.
         */
        data class NavigateToEntryDetail(val entryId: String) : Effect

        /**
         * Navigate to create/edit entry screen.
         */
        data class NavigateToCreateEntry(val promptText: String? = null) : Effect

        /**
         * Show success message.
         */
        data class ShowMessage(val message: String) : Effect

        /**
         * Show error message.
         */
        data class ShowError(val error: String) : Effect

        /**
         * Show delete confirmation dialog.
         */
        data class ShowDeleteConfirmation(val entryId: String, val title: String) : Effect
    }
}
