package com.po4yka.heauton.domain.usecase.quotes

import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for searching quotes.
 */
class SearchQuotesUseCase @Inject constructor(
    private val repository: QuotesRepository
) {
    /**
     * Searches for quotes matching the query.
     * @param query The search query
     * @return Flow of matching quotes
     */
    operator fun invoke(query: String): Flow<List<Quote>> {
        // Validate query
        val trimmedQuery = query.trim()

        if (trimmedQuery.length < 2) {
            // Return all quotes if query is too short
            return repository.getAllQuotes()
        }

        return repository.searchQuotes(trimmedQuery)
    }
}
