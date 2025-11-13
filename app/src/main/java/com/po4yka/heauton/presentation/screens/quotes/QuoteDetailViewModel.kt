package com.po4yka.heauton.presentation.screens.quotes

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.domain.usecase.quotes.DeleteQuoteUseCase
import com.po4yka.heauton.domain.usecase.quotes.GetQuoteByIdUseCase
import com.po4yka.heauton.domain.usecase.quotes.ToggleFavoriteUseCase
import com.po4yka.heauton.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Quote Detail Screen.
 */
@HiltViewModel
class QuoteDetailViewModel @Inject constructor(
    private val getQuoteByIdUseCase: GetQuoteByIdUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val deleteQuoteUseCase: DeleteQuoteUseCase
) : MviViewModel<QuoteDetailContract.Intent, QuoteDetailContract.State, QuoteDetailContract.Effect>() {

    override fun createInitialState(): QuoteDetailContract.State {
        return QuoteDetailContract.State()
    }

    override fun handleIntent(intent: QuoteDetailContract.Intent) {
        when (intent) {
            is QuoteDetailContract.Intent.LoadQuote -> loadQuote(intent.quoteId)
            is QuoteDetailContract.Intent.ToggleFavorite -> toggleFavorite()
            is QuoteDetailContract.Intent.ShareQuote -> shareQuote()
            is QuoteDetailContract.Intent.DeleteQuote -> deleteQuote()
            is QuoteDetailContract.Intent.EditQuote -> editQuote()
            is QuoteDetailContract.Intent.CreateJournalEntry -> createJournalEntry()
        }
    }

    private fun loadQuote(quoteId: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }

            try {
                val quote = getQuoteByIdUseCase(quoteId)
                if (quote != null) {
                    setState { copy(quote = quote, isLoading = false) }
                } else {
                    setState {
                        copy(
                            isLoading = false,
                            error = "Quote not found"
                        )
                    }
                    setEffect { QuoteDetailContract.Effect.ShowMessage("Quote not found") }
                    setEffect { QuoteDetailContract.Effect.NavigateBack }
                }
            } catch (e: Exception) {
                setState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load quote"
                    )
                }
                setEffect {
                    QuoteDetailContract.Effect.ShowMessage(
                        e.message ?: "Failed to load quote"
                    )
                }
            }
        }
    }

    private fun toggleFavorite() {
        val currentQuote = currentState.quote ?: return

        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(currentQuote.id, !currentQuote.isFavorite)
                setState {
                    copy(quote = currentQuote.copy(isFavorite = !currentQuote.isFavorite))
                }

                val message = if (!currentQuote.isFavorite) {
                    "Added to favorites"
                } else {
                    "Removed from favorites"
                }
                setEffect { QuoteDetailContract.Effect.ShowMessage(message) }
            } catch (e: Exception) {
                setEffect {
                    QuoteDetailContract.Effect.ShowMessage(
                        "Failed to update favorite status"
                    )
                }
            }
        }
    }

    private fun shareQuote() {
        val quote = currentState.quote ?: return
        val shareText = "${quote.text}\n\n— ${quote.author}" +
                if (quote.source != null) "\n(${quote.source})" else ""

        setEffect { QuoteDetailContract.Effect.ShareQuote(shareText) }
    }

    private fun deleteQuote() {
        val quote = currentState.quote ?: return

        viewModelScope.launch {
            setState { copy(isDeleting = true) }

            try {
                deleteQuoteUseCase(quote.id)
                setEffect { QuoteDetailContract.Effect.ShowMessage("Quote deleted") }
                setEffect { QuoteDetailContract.Effect.NavigateBack }
            } catch (e: Exception) {
                setState { copy(isDeleting = false) }
                setEffect {
                    QuoteDetailContract.Effect.ShowMessage(
                        "Failed to delete quote"
                    )
                }
            }
        }
    }

    private fun editQuote() {
        val quote = currentState.quote ?: return
        setEffect { QuoteDetailContract.Effect.NavigateToEdit(quote.id) }
    }

    private fun createJournalEntry() {
        val quote = currentState.quote ?: return
        val promptText = "Reflecting on: \"${quote.text}\" — ${quote.author}"
        setEffect { QuoteDetailContract.Effect.NavigateToJournalEditor(promptText) }
    }
}
