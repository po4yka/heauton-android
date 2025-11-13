package com.po4yka.heauton.presentation.screens.quotes

import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * MVI Contract for Quote Detail Screen.
 */
object QuoteDetailContract {

    /**
     * User intents for quote detail.
     */
    sealed interface Intent : MviIntent {
        data class LoadQuote(val quoteId: String) : Intent
        data object ToggleFavorite : Intent
        data object ShareQuote : Intent
        data object DeleteQuote : Intent
        data object EditQuote : Intent
        data object CreateJournalEntry : Intent
    }

    /**
     * UI state for quote detail.
     */
    data class State(
        val quote: Quote? = null,
        val isLoading: Boolean = true,
        val error: String? = null,
        val isDeleting: Boolean = false
    ) : MviState

    /**
     * One-time effects.
     */
    sealed interface Effect : MviEffect {
        data object NavigateBack : Effect
        data class NavigateToEdit(val quoteId: String) : Effect
        data class NavigateToJournalEditor(val quoteText: String) : Effect
        data class ShareQuote(val text: String) : Effect
        data class ShowMessage(val message: String) : Effect
    }
}
