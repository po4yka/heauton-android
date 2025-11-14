package com.po4yka.heauton.presentation.screens.quotes

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.domain.usecase.quotes.GetFavoriteQuotesUseCase
import com.po4yka.heauton.domain.usecase.quotes.GetQuotesUseCase
import com.po4yka.heauton.domain.usecase.quotes.SearchQuotesUseCase
import com.po4yka.heauton.domain.usecase.quotes.ToggleFavoriteUseCase
import com.po4yka.heauton.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Quotes List screen using MVI architecture.
 *
 * ## MVI Flow:
 * ```
 * User Action → Intent → ViewModel → State Update → View Renders
 *                           ↓
 *                        Effect (optional)
 * ```
 *
 * ## Example Usage:
 * ```kotlin
 * // From UI:
 * viewModel.sendIntent(QuotesListContract.Intent.SearchQueryChanged("wisdom"))
 *
 * // Observe state:
 * val state by viewModel.state.collectAsState()
 *
 * // Observe effects:
 * LaunchedEffect(Unit) {
 *     viewModel.effect.collect { effect ->
 *         when (effect) {
 *             is QuotesListContract.Effect.NavigateToQuoteDetail -> navigate(effect.quoteId)
 *         }
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class QuotesListViewModel @Inject constructor(
    private val getQuotesUseCase: GetQuotesUseCase,
    private val getFavoriteQuotesUseCase: GetFavoriteQuotesUseCase,
    private val searchQuotesUseCase: SearchQuotesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val quotesRepository: QuotesRepository
) : MviViewModel<QuotesListContract.Intent, QuotesListContract.State, QuotesListContract.Effect>() {

    private val _searchQuery = MutableStateFlow("")
    private val _showOnlyFavorites = MutableStateFlow(false)

    init {
        // Seed sample quotes on first launch
        viewModelScope.launch {
            quotesRepository.seedSampleQuotes()
        }

        // Observe quotes based on search query and filter
        observeQuotes()

        // Load initial quotes
        sendIntent(QuotesListContract.Intent.LoadQuotes)
    }

    override fun createInitialState(): QuotesListContract.State {
        return QuotesListContract.State(
            quotes = emptyList(),
            searchQuery = "",
            showOnlyFavorites = false,
            isLoading = true,
            error = null
        )
    }

    override fun handleIntent(intent: QuotesListContract.Intent) {
        when (intent) {
            is QuotesListContract.Intent.LoadQuotes -> loadQuotes()
            is QuotesListContract.Intent.SearchQueryChanged -> handleSearchQueryChanged(intent.query)
            is QuotesListContract.Intent.ToggleFavoritesFilter -> handleToggleFavoritesFilter()
            is QuotesListContract.Intent.ToggleFavorite -> handleToggleFavorite(intent.quoteId, intent.isFavorite)
            is QuotesListContract.Intent.QuoteClicked -> handleQuoteClicked(intent.quoteId)
            is QuotesListContract.Intent.Refresh -> handleRefresh()
            is QuotesListContract.Intent.DismissError -> handleDismissError()
        }
    }

    /**
     * Observes quotes based on search query and favorites filter.
     * Uses reactive streams to automatically update the state.
     */
    @OptIn(FlowPreview::class)
    private fun observeQuotes() {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300), // Debounce search input for 300ms
                _showOnlyFavorites
            ) { query, favoritesOnly ->
                Pair(query, favoritesOnly)
            }.flatMapLatest { (query, favoritesOnly) ->
                // Select the appropriate data source based on filters
                when {
                    favoritesOnly -> getFavoriteQuotesUseCase()
                    query.isNotBlank() -> searchQuotesUseCase(query)
                    else -> getQuotesUseCase()
                }
            }.catch { error ->
                // Handle errors and update state
                updateState {
                    copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred"
                    )
                }
                sendEffect(QuotesListContract.Effect.ShowError(error.message ?: "Unknown error"))
            }.collect { quotes ->
                // Update state with new quotes
                updateState {
                    copy(
                        quotes = quotes,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    private fun loadQuotes() {
        updateState { copy(isLoading = true, error = null) }
    }

    private fun handleSearchQueryChanged(query: String) {
        _searchQuery.value = query
        updateState { copy(searchQuery = query) }
    }

    private fun handleToggleFavoritesFilter() {
        _showOnlyFavorites.update { !it }
        updateState { copy(showOnlyFavorites = _showOnlyFavorites.value) }
    }

    private fun handleToggleFavorite(quoteId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(quoteId, isFavorite)
                val message = if (isFavorite) "Added to favorites" else "Removed from favorites"
                sendEffect(QuotesListContract.Effect.ShowMessage(message))
            } catch (e: Exception) {
                sendEffect(QuotesListContract.Effect.ShowError("Failed to update favorite status"))
            }
        }
    }

    private fun handleQuoteClicked(quoteId: String) {
        sendEffect(QuotesListContract.Effect.NavigateToQuoteDetail(quoteId))
    }

    private fun handleRefresh() {
        updateState { copy(isLoading = true, error = null) }
        // The observeQuotes flow will automatically refresh the data
    }

    private fun handleDismissError() {
        updateState { copy(error = null) }
    }
}
