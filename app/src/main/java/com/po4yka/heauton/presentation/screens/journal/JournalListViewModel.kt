package com.po4yka.heauton.presentation.screens.journal

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.domain.usecase.journal.DeleteJournalEntryUseCase
import com.po4yka.heauton.domain.usecase.journal.GetJournalEntriesUseCase
import com.po4yka.heauton.domain.usecase.journal.GetJournalStreakUseCase
import com.po4yka.heauton.domain.usecase.journal.GetRandomPromptUseCase
import com.po4yka.heauton.domain.usecase.journal.SearchJournalEntriesUseCase
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Journal List Screen using MVI pattern.
 *
 * ## Features:
 * - Reactive data loading with Flows
 * - Search with debouncing (300ms)
 * - Mood and favorite filtering
 * - Streak calculation
 * - Entry management (favorite, pin, delete)
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class JournalListViewModel @Inject constructor(
    private val getJournalEntriesUseCase: GetJournalEntriesUseCase,
    private val searchJournalEntriesUseCase: SearchJournalEntriesUseCase,
    private val getJournalStreakUseCase: GetJournalStreakUseCase,
    private val getRandomPromptUseCase: GetRandomPromptUseCase,
    private val deleteJournalEntryUseCase: DeleteJournalEntryUseCase,
    private val repository: JournalRepository
) : MviViewModel<JournalListContract.Intent, JournalListContract.State, JournalListContract.Effect>() {

    private val searchQueryFlow = MutableStateFlow("")
    private val moodFilterFlow = MutableStateFlow<String?>(null)
    private val favoritesFilterFlow = MutableStateFlow(false)

    init {
        observeEntries()
        loadStreaks()
    }

    override fun createInitialState(): JournalListContract.State {
        return JournalListContract.State()
    }

    override fun handleIntent(intent: JournalListContract.Intent) {
        when (intent) {
            is JournalListContract.Intent.LoadEntries -> loadEntries()
            is JournalListContract.Intent.SearchQueryChanged -> handleSearchQueryChanged(intent.query)
            is JournalListContract.Intent.MoodFilterChanged -> handleMoodFilterChanged(intent.mood)
            is JournalListContract.Intent.ToggleFavoritesFilter -> handleToggleFavoritesFilter()
            is JournalListContract.Intent.EntryClicked -> handleEntryClicked(intent.entryId)
            is JournalListContract.Intent.CreateEntryClicked -> handleCreateEntryClicked()
            is JournalListContract.Intent.ToggleFavorite -> handleToggleFavorite(intent.entryId, intent.isFavorite)
            is JournalListContract.Intent.TogglePinned -> handleTogglePinned(intent.entryId, intent.isPinned)
            is JournalListContract.Intent.DeleteEntry -> handleDeleteEntry(intent.entryId)
            is JournalListContract.Intent.Refresh -> handleRefresh()
            is JournalListContract.Intent.GetRandomPrompt -> handleGetRandomPrompt()
            is JournalListContract.Intent.DismissError -> handleDismissError()
        }
    }

    /**
     * Observe entries with reactive filtering.
     */
    private fun observeEntries() {
        viewModelScope.launch {
            combine(
                searchQueryFlow.debounce(300),
                moodFilterFlow,
                favoritesFilterFlow
            ) { searchQuery, moodFilter, showFavorites ->
                Triple(searchQuery, moodFilter, showFavorites)
            }.collectLatest { (searchQuery, moodFilter, showFavorites) ->
                updateState {
                    copy(
                        searchQuery = searchQuery,
                        moodFilter = moodFilter,
                        showOnlyFavorites = showFavorites,
                        isLoading = true
                    )
                }

                try {
                    // Determine which flow to collect from
                    val entriesFlow = when {
                        searchQuery.isNotBlank() -> searchJournalEntriesUseCase(searchQuery)
                        moodFilter != null -> repository.getEntriesByMood(moodFilter)
                        showFavorites -> repository.getFavoriteEntries()
                        else -> getJournalEntriesUseCase()
                    }

                    entriesFlow.collect { entries ->
                        updateState {
                            copy(
                                entries = entries,
                                totalEntries = entries.size,
                                isLoading = false,
                                isRefreshing = false,
                                error = null
                            )
                        }
                    }
                } catch (e: Exception) {
                    updateState {
                        copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = e.message ?: "Failed to load entries"
                        )
                    }
                }
            }
        }
    }

    private fun loadEntries() {
        // Entries are loaded reactively via observeEntries()
        updateState { copy(isLoading = true) }
    }

    private fun loadStreaks() {
        viewModelScope.launch {
            try {
                val (currentStreak, longestStreak) = getJournalStreakUseCase()
                updateState {
                    copy(
                        currentStreak = currentStreak,
                        longestStreak = longestStreak
                    )
                }
            } catch (e: Exception) {
                // Silently fail - streaks are not critical
            }
        }
    }

    private fun handleSearchQueryChanged(query: String) {
        searchQueryFlow.value = query
    }

    private fun handleMoodFilterChanged(mood: String?) {
        moodFilterFlow.value = mood
    }

    private fun handleToggleFavoritesFilter() {
        favoritesFilterFlow.value = !favoritesFilterFlow.value
    }

    private fun handleEntryClicked(entryId: String) {
        sendEffect(JournalListContract.Effect.NavigateToEntryDetail(entryId))
    }

    private fun handleCreateEntryClicked() {
        sendEffect(JournalListContract.Effect.NavigateToCreateEntry())
    }

    private fun handleToggleFavorite(entryId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(entryId, isFavorite)
                .onSuccess {
                    sendEffect(
                        JournalListContract.Effect.ShowMessage(
                            if (isFavorite) "Added to favorites" else "Removed from favorites"
                        )
                    )
                }
                .onFailure { message, _ ->
                    sendEffect(JournalListContract.Effect.ShowError("Failed to update favorite"))
                }
        }
    }

    private fun handleTogglePinned(entryId: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinned(entryId, isPinned)
                .onSuccess {
                    sendEffect(
                        JournalListContract.Effect.ShowMessage(
                            if (isPinned) "Entry pinned" else "Entry unpinned"
                        )
                    )
                }
                .onFailure { message, _ ->
                    sendEffect(JournalListContract.Effect.ShowError("Failed to update pinned status"))
                }
        }
    }

    private fun handleDeleteEntry(entryId: String) {
        viewModelScope.launch {
            deleteJournalEntryUseCase(entryId)
                .onSuccess {
                    sendEffect(JournalListContract.Effect.ShowMessage("Entry deleted"))
                    loadStreaks() // Refresh streaks after deletion
                }
                .onFailure { message, _ ->
                    sendEffect(JournalListContract.Effect.ShowError("Failed to delete entry"))
                }
        }
    }

    private fun handleRefresh() {
        updateState { copy(isRefreshing = true) }
        loadStreaks()
        // Entries will refresh automatically via observeEntries()
    }

    private fun handleGetRandomPrompt() {
        viewModelScope.launch {
            try {
                val prompt = getRandomPromptUseCase()
                if (prompt != null) {
                    sendEffect(JournalListContract.Effect.NavigateToCreateEntry(prompt.text))
                } else {
                    sendEffect(JournalListContract.Effect.ShowMessage("No prompts available"))
                }
            } catch (e: Exception) {
                sendEffect(JournalListContract.Effect.ShowError("Failed to get prompt"))
            }
        }
    }

    private fun handleDismissError() {
        updateState { copy(error = null) }
    }
}
