package com.po4yka.heauton.domain.repository

import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.model.QuoteFilter
import com.po4yka.heauton.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for quote operations.
 * Defines the contract for data operations independent of implementation.
 */
interface QuotesRepository {

    /**
     * Retrieves all quotes as a Flow for reactive updates.
     * @return Flow of all quotes
     */
    fun getAllQuotes(): Flow<List<Quote>>

    /**
     * Retrieves a quote by its ID.
     * @param id The unique identifier
     * @return The quote, or null if not found
     */
    suspend fun getQuoteById(id: String): Quote?

    /**
     * Retrieves favorite quotes.
     * @return Flow of favorite quotes
     */
    fun getFavoriteQuotes(): Flow<List<Quote>>

    /**
     * Searches quotes using full-text search.
     * @param query The search query
     * @return Flow of matching quotes
     */
    fun searchQuotes(query: String): Flow<List<Quote>>

    /**
     * Filters quotes based on criteria.
     * @param filter The filter criteria
     * @return Flow of filtered quotes
     */
    fun getFilteredQuotes(filter: QuoteFilter): Flow<List<Quote>>

    /**
     * Gets a random quote.
     * @param excludeRecentIds Optional list of IDs to exclude
     * @return A random quote, or null if none available
     */
    suspend fun getRandomQuote(excludeRecentIds: List<String> = emptyList()): Quote?

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
     * Updates read statistics for a quote.
     * @param quoteId The quote ID
     */
    suspend fun markAsRead(quoteId: String)

    /**
     * Gets the total count of quotes.
     * @return The total number of quotes
     */
    suspend fun getQuoteCount(): Int

    /**
     * Seeds the database with initial sample quotes.
     * Only seeds if the database is empty.
     */
    suspend fun seedSampleQuotes()

    // Result-based methods for error handling

    /**
     * Retrieves all quotes as a one-shot (non-Flow) operation.
     * @return Result with list of all quotes or error
     */
    suspend fun getAllQuotesOneShot(): Result<List<Quote>>

    /**
     * Retrieves a quote by its ID with Result wrapper.
     * @param id The unique identifier
     * @return Result with the quote or error
     */
    suspend fun getQuoteByIdResult(id: String): Result<Quote?>

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
