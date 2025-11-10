package com.po4yka.heauton.domain.usecase.quotes

import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all quotes.
 */
class GetQuotesUseCase @Inject constructor(
    private val repository: QuotesRepository
) {
    /**
     * Retrieves all quotes as a Flow for reactive updates.
     * @return Flow of all quotes
     */
    operator fun invoke(): Flow<List<Quote>> {
        return repository.getAllQuotes()
    }
}
