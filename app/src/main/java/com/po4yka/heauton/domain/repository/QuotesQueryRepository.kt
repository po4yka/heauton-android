package com.po4yka.heauton.domain.repository

import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.model.QuoteFilter
import com.po4yka.heauton.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for quote query operations.
 *
 * Following Interface Segregation Principle - contains only read operations.
 * Clients that only need to read quotes don't depend on mutation methods.
 */
interface QuotesQueryRepository {

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
}
