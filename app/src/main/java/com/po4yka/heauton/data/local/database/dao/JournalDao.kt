package com.po4yka.heauton.data.local.database.dao

import androidx.room.*
import com.po4yka.heauton.data.local.database.entities.JournalEntryEntity
import com.po4yka.heauton.data.local.database.entities.JournalPromptEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for journal entries and prompts.
 *
 * ## Features:
 * - Full CRUD operations for journal entries
 * - Full-text search with FTS4
 * - Mood filtering
 * - Tag-based queries
 * - Favorites and pinning support
 * - Prompt management
 * - Streak calculation
 */
@Dao
interface JournalDao {

    // ========== Journal Entry Queries ==========

    /**
     * Get all journal entries ordered by creation date (newest first).
     * Pinned entries appear first.
     */
    @Query("""
        SELECT * FROM journal_entries
        ORDER BY isPinned DESC, createdAt DESC
    """)
    fun getAllEntries(): Flow<List<JournalEntryEntity>>

    /**
     * Get a single journal entry by ID.
     */
    @Query("SELECT * FROM journal_entries WHERE id = :entryId")
    suspend fun getEntryById(entryId: String): JournalEntryEntity?

    /**
     * Get a single journal entry by ID as Flow.
     */
    @Query("SELECT * FROM journal_entries WHERE id = :entryId")
    fun getEntryByIdFlow(entryId: String): Flow<JournalEntryEntity?>

    /**
     * Get all favorite journal entries.
     */
    @Query("""
        SELECT * FROM journal_entries
        WHERE isFavorite = 1
        ORDER BY isPinned DESC, createdAt DESC
    """)
    fun getFavoriteEntries(): Flow<List<JournalEntryEntity>>

    /**
     * Get entries filtered by mood.
     */
    @Query("""
        SELECT * FROM journal_entries
        WHERE mood = :mood
        ORDER BY isPinned DESC, createdAt DESC
    """)
    fun getEntriesByMood(mood: String): Flow<List<JournalEntryEntity>>

    /**
     * Get entries created within a date range.
     * @param startDate Start timestamp (inclusive)
     * @param endDate End timestamp (inclusive)
     */
    @Query("""
        SELECT * FROM journal_entries
        WHERE createdAt >= :startDate AND createdAt <= :endDate
        ORDER BY createdAt DESC
    """)
    fun getEntriesInDateRange(startDate: Long, endDate: Long): Flow<List<JournalEntryEntity>>

    /**
     * Get entries related to a specific quote.
     */
    @Query("""
        SELECT * FROM journal_entries
        WHERE relatedQuoteId = :quoteId
        ORDER BY createdAt DESC
    """)
    fun getEntriesForQuote(quoteId: String): Flow<List<JournalEntryEntity>>

    /**
     * Search journal entries using full-text search.
     * @param query Search query (supports FTS4 syntax)
     */
    @Query("""
        SELECT journal_entries.*
        FROM journal_entries
        JOIN journal_entries_fts ON journal_entries.id = journal_entries_fts.docid
        WHERE journal_entries_fts MATCH :query
        ORDER BY journal_entries.isPinned DESC, journal_entries.createdAt DESC
    """)
    fun searchEntries(query: String): Flow<List<JournalEntryEntity>>

    /**
     * Get total count of journal entries.
     */
    @Query("SELECT COUNT(*) FROM journal_entries")
    fun getEntryCount(): Flow<Int>

    /**
     * Get total count of favorite entries.
     */
    @Query("SELECT COUNT(*) FROM journal_entries WHERE isFavorite = 1")
    fun getFavoriteEntryCount(): Flow<Int>

    /**
     * Get dates with journal entries for streak calculation.
     * Returns list of creation date timestamps.
     */
    @Query("""
        SELECT createdAt
        FROM journal_entries
        ORDER BY createdAt DESC
    """)
    suspend fun getEntryDates(): List<Long>

    /**
     * Get total word count across all entries.
     */
    @Query("SELECT SUM(wordCount) FROM journal_entries")
    fun getTotalWordCount(): Flow<Int>

    // ========== Journal Entry Mutations ==========

    /**
     * Insert a new journal entry.
     * @return ID of the inserted entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntryEntity): Long

    /**
     * Insert multiple journal entries.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<JournalEntryEntity>)

    /**
     * Update an existing journal entry.
     */
    @Update
    suspend fun updateEntry(entry: JournalEntryEntity)

    /**
     * Delete a journal entry.
     */
    @Delete
    suspend fun deleteEntry(entry: JournalEntryEntity)

    /**
     * Delete a journal entry by ID.
     */
    @Query("DELETE FROM journal_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: String)

    /**
     * Delete all journal entries.
     */
    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllEntries()

    /**
     * Toggle favorite status of an entry.
     */
    @Query("UPDATE journal_entries SET isFavorite = :isFavorite WHERE id = :entryId")
    suspend fun toggleFavorite(entryId: String, isFavorite: Boolean)

    /**
     * Toggle pinned status of an entry.
     */
    @Query("UPDATE journal_entries SET isPinned = :isPinned WHERE id = :entryId")
    suspend fun togglePinned(entryId: String, isPinned: Boolean)

    /**
     * Update the updated timestamp of an entry.
     */
    @Query("UPDATE journal_entries SET updatedAt = :timestamp WHERE id = :entryId")
    suspend fun updateTimestamp(entryId: String, timestamp: Long = System.currentTimeMillis())

    // ========== Prompt Queries ==========

    /**
     * Get all prompts.
     */
    @Query("SELECT * FROM journal_prompts ORDER BY category, usageCount DESC")
    fun getAllPrompts(): Flow<List<JournalPromptEntity>>

    /**
     * Get prompts by category.
     */
    @Query("""
        SELECT * FROM journal_prompts
        WHERE category = :category
        ORDER BY usageCount DESC
    """)
    fun getPromptsByCategory(category: String): Flow<List<JournalPromptEntity>>

    /**
     * Get prompts by difficulty.
     */
    @Query("""
        SELECT * FROM journal_prompts
        WHERE difficulty = :difficulty
        ORDER BY usageCount DESC
    """)
    fun getPromptsByDifficulty(difficulty: String): Flow<List<JournalPromptEntity>>

    /**
     * Get favorite prompts.
     */
    @Query("""
        SELECT * FROM journal_prompts
        WHERE isFavorite = 1
        ORDER BY usageCount DESC
    """)
    fun getFavoritePrompts(): Flow<List<JournalPromptEntity>>

    /**
     * Get a random prompt.
     */
    @Query("SELECT * FROM journal_prompts ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomPrompt(): JournalPromptEntity?

    /**
     * Get a random prompt by category.
     */
    @Query("""
        SELECT * FROM journal_prompts
        WHERE category = :category
        ORDER BY RANDOM()
        LIMIT 1
    """)
    suspend fun getRandomPromptByCategory(category: String): JournalPromptEntity?

    /**
     * Get prompt by ID.
     */
    @Query("SELECT * FROM journal_prompts WHERE id = :promptId")
    suspend fun getPromptById(promptId: String): JournalPromptEntity?

    // ========== Prompt Mutations ==========

    /**
     * Insert a new prompt.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: JournalPromptEntity)

    /**
     * Insert multiple prompts (for seeding).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompts(prompts: List<JournalPromptEntity>)

    /**
     * Update prompt usage count.
     */
    @Query("UPDATE journal_prompts SET usageCount = usageCount + 1 WHERE id = :promptId")
    suspend fun incrementPromptUsage(promptId: String)

    /**
     * Toggle prompt favorite status.
     */
    @Query("UPDATE journal_prompts SET isFavorite = :isFavorite WHERE id = :promptId")
    suspend fun togglePromptFavorite(promptId: String, isFavorite: Boolean)

    /**
     * Delete a prompt.
     */
    @Delete
    suspend fun deletePrompt(prompt: JournalPromptEntity)

    /**
     * Delete all prompts (for testing/reset).
     */
    @Query("DELETE FROM journal_prompts")
    suspend fun deleteAllPrompts()
}
