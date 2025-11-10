package com.po4yka.heauton.domain.usecase.quotes

import com.po4yka.heauton.domain.repository.QuotesRepository
import javax.inject.Inject

/**
 * Use case for deleting a quote.
 */
class DeleteQuoteUseCase @Inject constructor(
    private val repository: QuotesRepository
) {
    /**
     * Deletes a quote by ID.
     * @param quoteId The ID of the quote to delete
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(quoteId: String): Result<Unit> {
        return try {
            repository.deleteQuote(quoteId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
