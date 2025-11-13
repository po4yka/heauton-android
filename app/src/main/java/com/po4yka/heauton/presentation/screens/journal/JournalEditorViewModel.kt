package com.po4yka.heauton.presentation.screens.journal

import androidx.lifecycle.viewModelScope
import com.po4yka.heauton.domain.usecase.journal.CreateJournalEntryUseCase
import com.po4yka.heauton.domain.usecase.journal.GetJournalEntryByIdUseCase
import com.po4yka.heauton.domain.usecase.journal.GetRandomPromptUseCase
import com.po4yka.heauton.domain.usecase.journal.UpdateJournalEntryUseCase
import com.po4yka.heauton.presentation.mvi.MviViewModel
import com.po4yka.heauton.util.MarkdownFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Journal Editor Screen using MVI pattern.
 *
 * ## Features:
 * - Auto-save every 30 seconds
 * - Real-time word and character counting
 * - Markdown formatting support
 * - Mood and tag management
 * - Load existing entries for editing
 */
@HiltViewModel
class JournalEditorViewModel @Inject constructor(
    private val createJournalEntryUseCase: CreateJournalEntryUseCase,
    private val updateJournalEntryUseCase: UpdateJournalEntryUseCase,
    private val getJournalEntryByIdUseCase: GetJournalEntryByIdUseCase,
    private val getRandomPromptUseCase: GetRandomPromptUseCase
) : MviViewModel<JournalEditorContract.Intent, JournalEditorContract.State, JournalEditorContract.Effect>() {

    private var autoSaveJob: Job? = null

    override fun createInitialState(): JournalEditorContract.State {
        return JournalEditorContract.State()
    }

    override fun handleIntent(intent: JournalEditorContract.Intent) {
        when (intent) {
            is JournalEditorContract.Intent.LoadEntry -> handleLoadEntry(intent.entryId)
            is JournalEditorContract.Intent.TitleChanged -> handleTitleChanged(intent.title)
            is JournalEditorContract.Intent.ContentChanged -> handleContentChanged(intent.content)
            is JournalEditorContract.Intent.MoodSelected -> handleMoodSelected(intent.mood)
            is JournalEditorContract.Intent.TagAdded -> handleTagAdded(intent.tag)
            is JournalEditorContract.Intent.TagRemoved -> handleTagRemoved(intent.tag)
            is JournalEditorContract.Intent.ToggleEncryption -> handleToggleEncryption()
            is JournalEditorContract.Intent.ApplyFormatting -> handleApplyFormatting(intent.formatting)
            is JournalEditorContract.Intent.Save -> handleSave()
            is JournalEditorContract.Intent.AutoSave -> handleAutoSave()
            is JournalEditorContract.Intent.DiscardChanges -> handleDiscardChanges()
            is JournalEditorContract.Intent.InsertPrompt -> handleInsertPrompt(intent.promptText)
            is JournalEditorContract.Intent.RequestRandomPrompt -> handleRequestRandomPrompt()
        }
    }

    private fun handleLoadEntry(entryId: String?) {
        if (entryId == null) {
            // New entry - nothing to load
            startAutoSave()
            return
        }

        viewModelScope.launch {
            updateState { copy(isLoading = true) }

            try {
                val entry = getJournalEntryByIdUseCase(entryId)
                if (entry != null) {
                    updateState {
                        copy(
                            entryId = entry.id,
                            title = entry.title ?: "",
                            content = entry.content,
                            mood = entry.mood,
                            tags = entry.tags,
                            relatedQuoteId = entry.relatedQuoteId,
                            isEncrypted = entry.isEncrypted,
                            wordCount = entry.wordCount,
                            charCount = entry.content.length,
                            isLoading = false,
                            hasUnsavedChanges = false
                        )
                    }
                    startAutoSave()
                } else {
                    updateState { copy(isLoading = false, error = "Entry not found") }
                    sendEffect(JournalEditorContract.Effect.NavigateBack)
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load entry"
                    )
                }
            }
        }
    }

    private fun handleTitleChanged(title: String) {
        updateState {
            copy(
                title = title,
                hasUnsavedChanges = true
            )
        }
    }

    private fun handleContentChanged(content: String) {
        val wordCount = MarkdownFormatter.getWordCount(content)
        val charCount = content.length

        updateState {
            copy(
                content = content,
                wordCount = wordCount,
                charCount = charCount,
                hasUnsavedChanges = true
            )
        }
    }

    private fun handleMoodSelected(mood: com.po4yka.heauton.data.local.database.entities.JournalMood?) {
        updateState {
            copy(
                mood = mood,
                hasUnsavedChanges = true
            )
        }
    }

    private fun handleTagAdded(tag: String) {
        val trimmedTag = tag.trim()
        if (trimmedTag.isBlank()) return

        val currentTags = state.value.tags
        if (currentTags.contains(trimmedTag)) {
            sendEffect(JournalEditorContract.Effect.ShowError("Tag already exists"))
            return
        }

        updateState {
            copy(
                tags = tags + trimmedTag,
                hasUnsavedChanges = true
            )
        }
    }

    private fun handleTagRemoved(tag: String) {
        updateState {
            copy(
                tags = tags - tag,
                hasUnsavedChanges = true
            )
        }
    }

    private fun handleToggleEncryption() {
        val newEncryptionState = !state.value.isEncrypted

        if (newEncryptionState) {
            // Request biometric authentication
            sendEffect(JournalEditorContract.Effect.RequestBiometricAuth)
        }

        updateState {
            copy(
                isEncrypted = newEncryptionState,
                hasUnsavedChanges = true
            )
        }
    }

    private fun handleApplyFormatting(formatting: JournalEditorContract.MarkdownFormatting) {
        val currentState = state.value
        // This would be handled in the UI layer with text selection
        // We just send an effect to notify the UI
        sendEffect(JournalEditorContract.Effect.ShowError("Select text to format"))
    }

    private fun handleSave() {
        val currentState = state.value

        if (!currentState.canSave) {
            sendEffect(JournalEditorContract.Effect.ShowError("Cannot save empty entry"))
            return
        }

        viewModelScope.launch {
            updateState { copy(isSaving = true) }

            try {
                if (currentState.isNewEntry) {
                    // Create new entry
                    createJournalEntryUseCase(
                        title = currentState.title.takeIf { it.isNotBlank() },
                        content = currentState.content,
                        mood = currentState.mood,
                        tags = currentState.tags,
                        relatedQuoteId = currentState.relatedQuoteId,
                        encrypt = currentState.isEncrypted
                    ).onSuccess {
                        updateState {
                            copy(
                                isSaving = false,
                                hasUnsavedChanges = false
                            )
                        }
                        sendEffect(JournalEditorContract.Effect.ShowSaveSuccess("Entry created"))
                        sendEffect(JournalEditorContract.Effect.NavigateBack)
                    }.onFailure { message, _ ->
                        updateState { copy(isSaving = false) }
                        sendEffect(
                            JournalEditorContract.Effect.ShowError(
                                message
                            )
                        )
                    }
                } else {
                    // Update existing entry
                    val entry = getJournalEntryByIdUseCase(currentState.entryId!!)
                    if (entry != null) {
                        val updatedEntry = entry.copy(
                            title = currentState.title.takeIf { it.isNotBlank() },
                            content = currentState.content,
                            mood = currentState.mood,
                            tags = currentState.tags,
                            wordCount = currentState.wordCount
                        )

                        updateJournalEntryUseCase(updatedEntry)
                            .onSuccess {
                                updateState {
                                    copy(
                                        isSaving = false,
                                        hasUnsavedChanges = false
                                    )
                                }
                                sendEffect(JournalEditorContract.Effect.ShowSaveSuccess("Entry updated"))
                                sendEffect(JournalEditorContract.Effect.NavigateBack)
                            }
                            .onFailure { message, _ ->
                                updateState { copy(isSaving = false) }
                                sendEffect(
                                    JournalEditorContract.Effect.ShowError(
                                        message
                                    )
                                )
                            }
                    }
                }
            } catch (e: Exception) {
                updateState { copy(isSaving = false) }
                sendEffect(
                    JournalEditorContract.Effect.ShowError(
                        e.message ?: "Failed to save entry"
                    )
                )
            }
        }
    }

    private fun handleAutoSave() {
        val currentState = state.value

        if (!currentState.hasUnsavedChanges || !currentState.canSave) {
            return
        }

        viewModelScope.launch {
            try {
                if (currentState.isNewEntry) {
                    // For new entries, only auto-save if there's substantial content
                    if (currentState.wordCount < 10) return@launch

                    // Create the entry
                    createJournalEntryUseCase(
                        title = currentState.title.takeIf { it.isNotBlank() },
                        content = currentState.content,
                        mood = currentState.mood,
                        tags = currentState.tags,
                        relatedQuoteId = currentState.relatedQuoteId,
                        encrypt = currentState.isEncrypted
                    ).onSuccess { entryId ->
                        updateState {
                            copy(
                                entryId = entryId,
                                hasUnsavedChanges = false,
                                lastAutoSaveTime = System.currentTimeMillis()
                            )
                        }
                    }
                } else {
                    // Update existing entry
                    val entry = getJournalEntryByIdUseCase(currentState.entryId!!)
                    if (entry != null) {
                        val updatedEntry = entry.copy(
                            title = currentState.title.takeIf { it.isNotBlank() },
                            content = currentState.content,
                            mood = currentState.mood,
                            tags = currentState.tags,
                            wordCount = currentState.wordCount
                        )

                        updateJournalEntryUseCase(updatedEntry)
                            .onSuccess {
                                updateState {
                                    copy(
                                        hasUnsavedChanges = false,
                                        lastAutoSaveTime = System.currentTimeMillis()
                                    )
                                }
                            }
                    }
                }
            } catch (e: Exception) {
                // Silently fail auto-save
            }
        }
    }

    private fun handleDiscardChanges() {
        if (state.value.hasUnsavedChanges) {
            sendEffect(JournalEditorContract.Effect.ShowDiscardConfirmation)
        } else {
            sendEffect(JournalEditorContract.Effect.NavigateBack)
        }
    }

    private fun handleInsertPrompt(promptText: String) {
        val currentContent = state.value.content
        val newContent = if (currentContent.isBlank()) {
            promptText
        } else {
            "$currentContent\n\n$promptText"
        }

        handleContentChanged(newContent)
    }

    private fun handleRequestRandomPrompt() {
        viewModelScope.launch {
            try {
                val prompt = getRandomPromptUseCase()
                if (prompt != null) {
                    sendEffect(JournalEditorContract.Effect.ShowRandomPrompt(prompt.text))
                } else {
                    sendEffect(JournalEditorContract.Effect.ShowError("No prompts available"))
                }
            } catch (e: Exception) {
                sendEffect(JournalEditorContract.Effect.ShowError("Failed to get prompt"))
            }
        }
    }

    /**
     * Start auto-save timer (30 seconds).
     */
    private fun startAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            while (true) {
                delay(30_000) // 30 seconds
                handleAutoSave()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }
}
