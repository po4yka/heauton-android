package com.po4yka.heauton.domain.usecase.journal

import com.po4yka.heauton.domain.repository.JournalRepository
import javax.inject.Inject

/**
 * Use case to delete a journal entry.
 *
 * ## Features:
 * - Permanently deletes entry from database
 * - No undo functionality (handle confirmation in UI)
 */
class DeleteJournalEntryUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    /**
     * Delete a journal entry by ID.
     *
     * @param entryId ID of the entry to delete
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(entryId: String): Result<Unit> {
        return repository.deleteEntry(entryId)
    }
}
