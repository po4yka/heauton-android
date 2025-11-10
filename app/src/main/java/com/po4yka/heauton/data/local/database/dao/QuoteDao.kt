package com.po4yka.heauton.data.local.database.dao

import androidx.room.*
import com.po4yka.heauton.data.local.database.entities.QuoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Quote operations.
 * Provides methods for CRUD operations and queries on quotes.
 */
@Dao
interface QuoteDao {

    // ========== Create ==========

    /**
     * Inserts a new quote into the database.
     * @param quote The quote entity to insert
     * @return The row ID of the inserted quote
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quote: QuoteEntity): Long

    /**
     * Inserts multiple quotes into the database.
     * @param quotes List of quote entities to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quotes: List<QuoteEntity>)

    // ========== Read ==========

    /**
     * Retrieves all quotes as a Flow for reactive updates.
     * @return Flow of all quotes ordered by creation date descending
     */
    @Query("SELECT * FROM quotes ORDER BY createdAt DESC")
    fun getAllQuotes(): Flow<List<QuoteEntity>>

    /**
     * Retrieves a quote by its ID.
     * @param id The unique identifier of the quote
     * @return The quote entity, or null if not found
     */
    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: String): QuoteEntity?

    /**
     * Retrieves favorite quotes as a Flow.
     * @return Flow of favorite quotes ordered by creation date descending
     */
    @Query("SELECT * FROM quotes WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteQuotes(): Flow<List<QuoteEntity>>

    /**
     * Retrieves quotes by author.
     * @param author The author name to filter by
     * @return Flow of quotes by the specified author
     */
    @Query("SELECT * FROM quotes WHERE author = :author ORDER BY createdAt DESC")
    fun getQuotesByAuthor(author: String): Flow<List<QuoteEntity>>

    /**
     * Gets a random quote from the database.
     * @return A random quote entity, or null if no quotes exist
     */
    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuote(): QuoteEntity?

    /**
     * Gets a random quote excluding specific IDs (e.g., recently shown quotes).
     * @param excludedIds List of quote IDs to exclude
     * @return A random quote not in the excluded list
     */
    @Query("SELECT * FROM quotes WHERE id NOT IN (:excludedIds) ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuoteExcluding(excludedIds: List<String>): QuoteEntity?

    /**
     * Gets quotes by category.
     * This is a simplified query - for proper implementation, we'd need to handle the list properly.
     */
    @Query("SELECT * FROM quotes ORDER BY createdAt DESC")
    fun getAllQuotesForFiltering(): Flow<List<QuoteEntity>>

    // ========== Update ==========

    /**
     * Updates an existing quote.
     * @param quote The quote entity with updated values
     */
    @Update
    suspend fun update(quote: QuoteEntity)

    /**
     * Toggles the favorite status of a quote.
     * @param id The quote ID
     * @param isFavorite The new favorite status
     */
    @Query("UPDATE quotes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    /**
     * Updates the read statistics for a quote.
     * @param id The quote ID
     * @param readCount The new read count
     * @param lastReadAt The timestamp of the last read
     */
    @Query("UPDATE quotes SET readCount = :readCount, lastReadAt = :lastReadAt WHERE id = :id")
    suspend fun updateReadStats(id: String, readCount: Int, lastReadAt: Long)

    // ========== Delete ==========

    /**
     * Deletes a quote from the database.
     * @param quote The quote entity to delete
     */
    @Delete
    suspend fun delete(quote: QuoteEntity)

    /**
     * Deletes a quote by its ID.
     * @param id The quote ID to delete
     */
    @Query("DELETE FROM quotes WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Deletes all quotes from the database.
     */
    @Query("DELETE FROM quotes")
    suspend fun deleteAll()

    // ========== Statistics ==========

    /**
     * Gets the total count of quotes.
     * @return The total number of quotes in the database
     */
    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun getQuoteCount(): Int

    /**
     * Gets the count of favorite quotes.
     * @return The number of favorite quotes
     */
    @Query("SELECT COUNT(*) FROM quotes WHERE isFavorite = 1")
    suspend fun getFavoriteCount(): Int

    // ========== Search ==========

    /**
     * Searches quotes using full-text search.
     * @param query The search query
     * @return Flow of matching quotes
     */
    @Query("""
        SELECT quotes.* FROM quotes
        JOIN quotes_fts ON quotes.rowid = quotes_fts.rowid
        WHERE quotes_fts MATCH :query
        ORDER BY quotes.createdAt DESC
    """)
    fun searchQuotes(query: String): Flow<List<QuoteEntity>>
}
