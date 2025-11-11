package com.po4yka.heauton.domain.usecase.journal

import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all journal entries.
 * Entries are ordered by pinned status and creation date (newest first).
 */
class GetJournalEntriesUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    /**
     * Get all journal entries as a Flow.
     */
    operator fun invoke(): Flow<List<JournalEntry>> {
        return repository.getAllEntries()
    }
}
