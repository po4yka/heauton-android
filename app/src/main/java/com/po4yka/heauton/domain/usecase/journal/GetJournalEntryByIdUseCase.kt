package com.po4yka.heauton.domain.usecase.journal

import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.repository.JournalRepository
import javax.inject.Inject

/**
 * Use case to get a single journal entry by ID.
 *
 * ## Features:
 * - Retrieves entry with automatic decryption if needed
 * - Returns null if entry not found
 */
class GetJournalEntryByIdUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    /**
     * Get a journal entry by ID.
     *
     * @param entryId ID of the entry to retrieve
     * @return Journal entry, or null if not found
     */
    suspend operator fun invoke(entryId: String): JournalEntry? {
        return repository.getEntryById(entryId)
    }
}
