package com.po4yka.heauton.presentation.screens.journal

import com.po4yka.heauton.data.local.database.entities.JournalMood
import com.po4yka.heauton.presentation.mvi.MviEffect
import com.po4yka.heauton.presentation.mvi.MviIntent
import com.po4yka.heauton.presentation.mvi.MviState

/**
 * MVI Contract for Journal Editor Screen.
 *
 * ## Screen Purpose:
 * Create and edit journal entries with Markdown support.
 *
 * ## Features:
 * - Markdown editor with toolbar
 * - Mood selection
 * - Tag management
 * - Auto-save (30-second timer)
 * - Quote inspiration integration
 * - Encryption support
 */
object JournalEditorContract {

    /**
     * User Intents for Journal Editor Screen.
     */
    sealed interface Intent : MviIntent {
        /**
         * Load entry for editing (if entryId is provided).
         */
        data class LoadEntry(val entryId: String?) : Intent

        /**
         * Title changed.
         */
        data class TitleChanged(val title: String) : Intent

        /**
         * Content changed.
         */
        data class ContentChanged(val content: String) : Intent

        /**
         * Mood selected.
         */
        data class MoodSelected(val mood: JournalMood?) : Intent

        /**
         * Tag added.
         */
        data class TagAdded(val tag: String) : Intent

        /**
         * Tag removed.
         */
        data class TagRemoved(val tag: String) : Intent

        /**
         * Toggle encryption.
         */
        data object ToggleEncryption : Intent

        /**
         * Apply Markdown formatting.
         */
        data class ApplyFormatting(val formatting: MarkdownFormatting) : Intent

        /**
         * Save entry.
         */
        data object Save : Intent

        /**
         * Auto-save triggered.
         */
        data object AutoSave : Intent

        /**
         * Discard changes and go back.
         */
        data object DiscardChanges : Intent

        /**
         * Insert prompt text.
         */
        data class InsertPrompt(val promptText: String) : Intent

        /**
         * Request random prompt.
         */
        data object RequestRandomPrompt : Intent
    }

    /**
     * Markdown formatting types.
     */
    enum class MarkdownFormatting {
        BOLD,
        ITALIC,
        CODE,
        HEADING_1,
        HEADING_2,
        HEADING_3,
        BULLET_LIST,
        NUMBERED_LIST,
        BLOCKQUOTE,
        CODE_BLOCK
    }

    /**
     * UI State for Journal Editor Screen.
     */
    data class State(
        /**
         * Entry ID (null for new entry).
         */
        val entryId: String? = null,

        /**
         * Entry title.
         */
        val title: String = "",

        /**
         * Entry content (Markdown).
         */
        val content: String = "",

        /**
         * Selected mood.
         */
        val mood: JournalMood? = null,

        /**
         * List of tags.
         */
        val tags: List<String> = emptyList(),

        /**
         * Related quote ID (if entry was inspired by a quote).
         */
        val relatedQuoteId: String? = null,

        /**
         * Whether the entry should be encrypted.
         */
        val isEncrypted: Boolean = false,

        /**
         * Word count.
         */
        val wordCount: Int = 0,

        /**
         * Character count.
         */
        val charCount: Int = 0,

        /**
         * Whether the entry has unsaved changes.
         */
        val hasUnsavedChanges: Boolean = false,

        /**
         * Loading state.
         */
        val isLoading: Boolean = false,

        /**
         * Saving state.
         */
        val isSaving: Boolean = false,

        /**
         * Error message, if any.
         */
        val error: String? = null,

        /**
         * Last auto-save timestamp.
         */
        val lastAutoSaveTime: Long? = null
    ) : MviState {
        /**
         * Whether the entry can be saved.
         */
        val canSave: Boolean
            get() = content.isNotBlank() && !isSaving

        /**
         * Whether this is a new entry.
         */
        val isNewEntry: Boolean
            get() = entryId == null
    }

    /**
     * One-time Effects for Journal Editor Screen.
     */
    sealed interface Effect : MviEffect {
        /**
         * Navigate back after successful save.
         */
        data object NavigateBack : Effect

        /**
         * Show save success message.
         */
        data class ShowSaveSuccess(val message: String) : Effect

        /**
         * Show error message.
         */
        data class ShowError(val error: String) : Effect

        /**
         * Show discard confirmation dialog.
         */
        data object ShowDiscardConfirmation : Effect

        /**
         * Request biometric authentication for encryption.
         */
        data object RequestBiometricAuth : Effect

        /**
         * Insert text at cursor position.
         */
        data class InsertText(val text: String) : Effect

        /**
         * Show random prompt bottom sheet.
         */
        data class ShowRandomPrompt(val promptText: String) : Effect
    }
}
