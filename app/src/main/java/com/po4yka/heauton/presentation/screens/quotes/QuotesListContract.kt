package com.po4yka.heauton.presentation.screens.quotes

import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * Contract for Quotes List screen following MVI pattern.
 * Defines Intent, State, and Effect for the screen.
 */
object QuotesListContract {

    /**
     * User intents/actions for the Quotes List screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Load initial quotes (triggered on screen creation).
         */
        data object LoadQuotes : Intent

        /**
         * User changed the search query.
         */
        data class SearchQueryChanged(val query: String) : Intent

        /**
         * User toggled the favorites filter.
         */
        data object ToggleFavoritesFilter : Intent

        /**
         * User toggled favorite status of a quote.
         */
        data class ToggleFavorite(val quoteId: String, val isFavorite: Boolean) : Intent

        /**
         * User clicked on a quote to view details.
         */
        data class QuoteClicked(val quoteId: String) : Intent

        /**
         * User pulled to refresh the list.
         */
        data object Refresh : Intent

        /**
         * User dismissed an error message.
         */
        data object DismissError : Intent
    }

    /**
     * UI state for the Quotes List screen.
     * Represents the complete state of the UI at any given time.
     */
    data class State(
        val quotes: List<Quote> = emptyList(),
        val searchQuery: String = "",
        val showOnlyFavorites: Boolean = false,
        val isLoading: Boolean = true,
        val error: String? = null
    ) : MviState {
        /**
         * Helper property to check if there are no quotes to display.
         */
        val isEmpty: Boolean
            get() = quotes.isEmpty() && !isLoading && error == null
    }

    /**
     * One-time side effects for the Quotes List screen.
     * Effects are consumed once and don't survive configuration changes.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate to quote detail screen.
         */
        data class NavigateToQuoteDetail(val quoteId: String) : Effect

        /**
         * Show a success message (e.g., quote favorited).
         */
        data class ShowMessage(val message: String) : Effect

        /**
         * Show an error message.
         */
        data class ShowError(val error: String) : Effect
    }
}
