package com.po4yka.heauton.domain.usecase.quotes

import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import javax.inject.Inject

/**
 * Use case for retrieving a quote by ID.
 */
class GetQuoteByIdUseCase @Inject constructor(
    private val repository: QuotesRepository
) {
    /**
     * Retrieves a quote by its ID and marks it as read.
     * @param id The quote ID
     * @param markAsRead Whether to track this as a read event
     * @return The quote, or null if not found
     */
    suspend operator fun invoke(id: String, markAsRead: Boolean = true): Quote? {
        val quote = repository.getQuoteById(id)

        if (quote != null && markAsRead) {
            // Track that the quote was read
            repository.markAsRead(id)
        }

        return quote
    }
}
