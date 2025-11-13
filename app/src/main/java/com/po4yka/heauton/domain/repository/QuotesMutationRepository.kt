package com.po4yka.heauton.domain.repository

import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.util.Result

/**
 * Repository interface for quote mutation operations.
 *
 * Following Interface Segregation Principle - contains only write operations.
 * Clients that only need to modify quotes don't depend on query methods.
 */
interface QuotesMutationRepository {

    /**
     * Adds a new quote.
     * @param quote The quote to add
     * @return The ID of the added quote
     */
    suspend fun addQuote(quote: Quote): String

    /**
     * Updates an existing quote.
     * @param quote The quote with updated values
     */
    suspend fun updateQuote(quote: Quote)

    /**
     * Deletes a quote.
     * @param quoteId The ID of the quote to delete
     */
    suspend fun deleteQuote(quoteId: String)

    /**
     * Toggles the favorite status of a quote.
     * @param quoteId The quote ID
     * @param isFavorite The new favorite status
     */
    suspend fun toggleFavorite(quoteId: String, isFavorite: Boolean)

    /**
     * Adds a new quote with Result wrapper.
     * @param quote The quote to add
     * @return Result with Unit or error
     */
    suspend fun addQuoteResult(quote: Quote): Result<Unit>

    /**
     * Deletes a quote with Result wrapper.
     * @param quoteId The ID of the quote to delete
     * @return Result with Unit or error
     */
    suspend fun deleteQuoteResult(quoteId: String): Result<Unit>
}
