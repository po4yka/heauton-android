package com.po4yka.heauton.domain.usecase.journal

import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.util.MarkdownFormatter
import com.po4yka.heauton.util.Result
import javax.inject.Inject

/**
 * Use case to update an existing journal entry.
 *
 * ## Features:
 * - Auto-updates timestamp
 * - Recalculates word count
 * - Preserves encryption state
 */
class UpdateJournalEntryUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    /**
     * Update an existing journal entry.
     *
     * @param entry The entry to update (with modifications)
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(entry: JournalEntry): Result<Unit> {
        // Validate content
        if (entry.content.isBlank()) {
            return Result.error("Content cannot be empty")
        }

        // Recalculate word count
        val wordCount = MarkdownFormatter.getWordCount(entry.content)

        // Update entry with new timestamp and word count
        val updatedEntry = entry.copy(
            updatedAt = System.currentTimeMillis(),
            wordCount = wordCount
        )

        return repository.updateEntry(updatedEntry)
    }
}
