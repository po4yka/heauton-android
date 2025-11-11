package com.po4yka.heauton.domain.usecase.journal

import com.po4yka.heauton.data.local.database.entities.JournalMood
import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.util.MarkdownFormatter
import javax.inject.Inject

/**
 * Use case to create a new journal entry.
 *
 * ## Features:
 * - Auto-calculates word count
 * - Supports optional encryption
 * - Validates entry data
 * - Returns Result with entry ID
 */
class CreateJournalEntryUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    /**
     * Create a new journal entry.
     *
     * @param title Optional title for the entry
     * @param content Markdown content
     * @param mood Optional mood
     * @param tags List of tags
     * @param relatedQuoteId Optional quote that inspired this entry
     * @param encrypt Whether to encrypt the entry (default: false)
     * @return Result with entry ID if successful
     */
    suspend operator fun invoke(
        title: String?,
        content: String,
        mood: JournalMood?,
        tags: List<String> = emptyList(),
        relatedQuoteId: String? = null,
        encrypt: Boolean = false
    ): Result<String> {
        // Validate content
        if (content.isBlank()) {
            return Result.failure(IllegalArgumentException("Content cannot be empty"))
        }

        // Calculate word count
        val wordCount = MarkdownFormatter.getWordCount(content)

        // Create entry
        val entry = JournalEntry(
            id = java.util.UUID.randomUUID().toString(),
            title = title?.takeIf { it.isNotBlank() },
            content = content,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            mood = mood,
            relatedQuoteId = relatedQuoteId,
            tags = tags,
            isFavorite = false,
            isPinned = false,
            wordCount = wordCount,
            isEncrypted = encrypt,
            isStoredInFile = false
        )

        return repository.createEntry(entry, encrypt)
    }
}
