package com.po4yka.heauton.presentation.screens.journal

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.domain.usecase.journal.DeleteJournalEntryUseCase
import com.po4yka.heauton.domain.usecase.journal.GetJournalEntryByIdUseCase
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.presentation.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Journal Detail Screen using MVI pattern.
 *
 * ## Features:
 * - Load and display journal entry
 * - Handle favorite and pin toggles
 * - Navigate to edit screen
 * - Share entry content
 * - Delete entry with confirmation
 */
@HiltViewModel
class JournalDetailViewModel @Inject constructor(
    private val getJournalEntryByIdUseCase: GetJournalEntryByIdUseCase,
    private val deleteJournalEntryUseCase: DeleteJournalEntryUseCase,
    private val repository: JournalRepository
) : MviViewModel<JournalDetailContract.Intent, JournalDetailContract.State, JournalDetailContract.Effect>() {

    override fun createInitialState(): JournalDetailContract.State {
        return JournalDetailContract.State()
    }

    override fun handleIntent(intent: JournalDetailContract.Intent) {
        when (intent) {
            is JournalDetailContract.Intent.LoadEntry -> handleLoadEntry(intent.entryId)
            is JournalDetailContract.Intent.EditEntry -> handleEditEntry()
            is JournalDetailContract.Intent.DeleteEntry -> handleDeleteEntry()
            is JournalDetailContract.Intent.ShareEntry -> handleShareEntry()
            is JournalDetailContract.Intent.ToggleFavorite -> handleToggleFavorite()
            is JournalDetailContract.Intent.TogglePinned -> handleTogglePinned()
            is JournalDetailContract.Intent.QuoteClicked -> handleQuoteClicked(intent.quoteId)
        }
    }

    private fun handleLoadEntry(entryId: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }

            try {
                val entry = getJournalEntryByIdUseCase(entryId)
                if (entry != null) {
                    updateState {
                        copy(
                            entry = entry,
                            isLoading = false
                        )
                    }
                } else {
                    updateState {
                        copy(
                            isLoading = false,
                            error = "Entry not found"
                        )
                    }
                    sendEffect(JournalDetailContract.Effect.ShowError("Entry not found"))
                    sendEffect(JournalDetailContract.Effect.NavigateBack)
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load entry"
                    )
                }
                sendEffect(JournalDetailContract.Effect.ShowError("Failed to load entry"))
            }
        }
    }

    private fun handleEditEntry() {
        val entry = state.value.entry ?: return
        sendEffect(JournalDetailContract.Effect.NavigateToEdit(entry.id))
    }

    private fun handleDeleteEntry() {
        val entry = state.value.entry ?: return
        sendEffect(
            JournalDetailContract.Effect.ShowDeleteConfirmation(
                entry.id,
                entry.displayTitle
            )
        )
    }

    fun confirmDelete() {
        val entry = state.value.entry ?: return

        viewModelScope.launch {
            deleteJournalEntryUseCase(entry.id)
                .onSuccess {
                    sendEffect(JournalDetailContract.Effect.ShowMessage("Entry deleted"))
                    sendEffect(JournalDetailContract.Effect.NavigateBack)
                }
                .onFailure {
                    sendEffect(JournalDetailContract.Effect.ShowError("Failed to delete entry"))
                }
        }
    }

    private fun handleShareEntry() {
        val entry = state.value.entry ?: return

        val shareText = buildString {
            if (!entry.title.isNullOrBlank()) {
                appendLine(entry.title)
                appendLine()
            }
            appendLine(entry.content)
            appendLine()
            appendLine("---")
            appendLine("Created: ${entry.getFormattedDate()}")
            if (entry.mood != null) {
                appendLine("Mood: ${entry.mood.displayName} ${entry.mood.emoji}")
            }
            if (entry.tags.isNotEmpty()) {
                appendLine("Tags: ${entry.tags.joinToString(", ")}")
            }
        }

        sendEffect(JournalDetailContract.Effect.ShowShareSheet(shareText))
    }

    private fun handleToggleFavorite() {
        val entry = state.value.entry ?: return

        viewModelScope.launch {
            repository.toggleFavorite(entry.id, !entry.isFavorite)
                .onSuccess {
                    // Update local state
                    updateState {
                        copy(entry = entry.copy(isFavorite = !entry.isFavorite))
                    }
                    sendEffect(
                        JournalDetailContract.Effect.ShowMessage(
                            if (!entry.isFavorite) "Added to favorites" else "Removed from favorites"
                        )
                    )
                }
                .onFailure {
                    sendEffect(JournalDetailContract.Effect.ShowError("Failed to update favorite"))
                }
        }
    }

    private fun handleTogglePinned() {
        val entry = state.value.entry ?: return

        viewModelScope.launch {
            repository.togglePinned(entry.id, !entry.isPinned)
                .onSuccess {
                    // Update local state
                    updateState {
                        copy(entry = entry.copy(isPinned = !entry.isPinned))
                    }
                    sendEffect(
                        JournalDetailContract.Effect.ShowMessage(
                            if (!entry.isPinned) "Entry pinned" else "Entry unpinned"
                        )
                    )
                }
                .onFailure {
                    sendEffect(JournalDetailContract.Effect.ShowError("Failed to update pinned status"))
                }
        }
    }

    private fun handleQuoteClicked(quoteId: String) {
        sendEffect(JournalDetailContract.Effect.NavigateToQuote(quoteId))
    }
}
