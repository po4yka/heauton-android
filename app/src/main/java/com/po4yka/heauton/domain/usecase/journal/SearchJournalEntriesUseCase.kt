package com.po4yka.heauton.domain.usecase.journal

import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case to search journal entries using full-text search.
 *
 * ## Features:
 * - Full-text search across titles and content
 * - FTS4 search with ranking
 * - Returns empty flow for blank queries
 */
class SearchJournalEntriesUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    /**
     * Search journal entries.
     *
     * @param query Search query (supports FTS4 syntax)
     * @return Flow of matching entries
     */
    operator fun invoke(query: String): Flow<List<JournalEntry>> {
        if (query.isBlank()) {
            return flow { emit(emptyList()) }
        }

        return repository.searchEntries(query)
    }
}
