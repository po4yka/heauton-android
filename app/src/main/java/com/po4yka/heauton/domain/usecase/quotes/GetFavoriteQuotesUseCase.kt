package com.po4yka.heauton.domain.usecase.quotes

import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving favorite quotes.
 */
class GetFavoriteQuotesUseCase @Inject constructor(
    private val repository: QuotesRepository
) {
    /**
     * Retrieves all favorite quotes as a Flow.
     * @return Flow of favorite quotes
     */
    operator fun invoke(): Flow<List<Quote>> {
        return repository.getFavoriteQuotes()
    }
}
