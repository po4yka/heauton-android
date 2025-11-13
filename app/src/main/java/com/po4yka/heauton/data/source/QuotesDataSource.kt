package com.po4yka.heauton.data.source

import com.po4yka.heauton.data.local.database.entities.QuoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Abstract data source for quotes.
 *
 * Following Open/Closed Principle and Dependency Inversion Principle:
 * - Repositories depend on this abstraction, not concrete implementations
 * - Easy to swap Room for Firestore, Network API, etc.
 * - Testable with fake implementations
 *
 * ## Implementations:
 * - [RoomQuotesDataSource] - Local Room database
 * - Future: NetworkQuotesDataSource, FirestoreQuotesDataSource, etc.
 */
interface QuotesDataSource {

    // ========== Query Operations ==========

    /**
     * Retrieves all quotes as a Flow for reactive updates.
     * @return Flow of all quote entities
     */
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    /**
     * Retrieves a quote by its ID.
     * @param id The unique identifier
     * @return The quote entity, or null if not found
     */
    suspend fun getQuoteById(id: String): QuoteEntity?

    /**
     * Retrieves favorite quotes.
     * @return Flow of favorite quote entities
     */
    fun getFavoriteQuotes(): Flow<List<QuoteEntity>>

    /**
     * Retrieves quotes by author.
     * @param author The author name
     * @return Flow of quote entities by this author
     */
    fun getQuotesByAuthor(author: String): Flow<List<QuoteEntity>>

    /**
     * Searches quotes using full-text search.
     * @param query The search query (pre-processed)
     * @return Flow of matching quote entities
     */
    fun searchQuotes(query: String): Flow<List<QuoteEntity>>

    /**
     * Gets a random quote.
     * @return A random quote entity, or null if none available
     */
    suspend fun getRandomQuote(): QuoteEntity?

    /**
     * Gets a random quote excluding specific IDs.
     * @param excludedIds List of quote IDs to exclude
     * @return A random quote not in the excluded list
     */
    suspend fun getRandomQuoteExcluding(excludedIds: List<String>): QuoteEntity?

    // ========== Mutation Operations ==========

    /**
     * Inserts a new quote.
     * @param quote The quote entity to insert
     * @return The row ID of the inserted quote
     */
    suspend fun insert(quote: QuoteEntity): Long

    /**
     * Inserts multiple quotes.
     * @param quotes List of quote entities to insert
     */
    suspend fun insertAll(quotes: List<QuoteEntity>)

    /**
     * Updates an existing quote.
     * @param quote The quote entity with updated values
     */
    suspend fun update(quote: QuoteEntity)

    /**
     * Deletes a quote by ID.
     * @param quoteId The ID of the quote to delete
     */
    suspend fun delete(quoteId: String)

    /**
     * Updates the favorite status of a quote.
     * @param id The quote ID
     * @param isFavorite The new favorite status
     */
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    /**
     * Increments the read count for a quote.
     * @param id The quote ID
     */
    suspend fun incrementReadCount(id: String)

    /**
     * Updates the last read timestamp.
     * @param id The quote ID
     * @param timestamp The timestamp in milliseconds
     */
    suspend fun updateLastReadAt(id: String, timestamp: Long)

    // ========== Statistics Operations ==========

    /**
     * Gets the total count of quotes.
     * @return The total number of quotes
     */
    suspend fun getCount(): Int

    /**
     * Checks if the database is empty.
     * @return True if no quotes exist, false otherwise
     */
    suspend fun isEmpty(): Boolean
}
