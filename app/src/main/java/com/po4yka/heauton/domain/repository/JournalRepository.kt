package com.po4yka.heauton.domain.repository

import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.model.JournalPrompt
import com.po4yka.heauton.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for journal operations.
 *
 * ## Responsibilities:
 * - CRUD operations for journal entries
 * - Full-text search across entries
 * - Mood filtering and analytics
 * - Prompt management
 * - Streak calculation
 * - Encryption/decryption coordination
 */
interface JournalRepository {

    // ========== Journal Entry Operations ==========

    /**
     * Get all journal entries as a Flow.
     * Entries are ordered by pinned status and creation date (newest first).
     */
    fun getAllEntries(): Flow<List<JournalEntry>>

    /**
     * Get a single journal entry by ID.
     */
    suspend fun getEntryById(entryId: String): JournalEntry?

    /**
     * Get a single journal entry by ID as Flow.
     */
    fun getEntryByIdFlow(entryId: String): Flow<JournalEntry?>

    /**
     * Get all favorite journal entries.
     */
    fun getFavoriteEntries(): Flow<List<JournalEntry>>

    /**
     * Get entries filtered by mood.
     */
    fun getEntriesByMood(mood: String): Flow<List<JournalEntry>>

    /**
     * Get entries created within a date range.
     */
    fun getEntriesInDateRange(startDate: Long, endDate: Long): Flow<List<JournalEntry>>

    /**
     * Get entries related to a specific quote.
     */
    fun getEntriesForQuote(quoteId: String): Flow<List<JournalEntry>>

    /**
     * Search journal entries using full-text search.
     * @param query Search query (supports FTS4 syntax)
     */
    fun searchEntries(query: String): Flow<List<JournalEntry>>

    /**
     * Create a new journal entry.
     * @param entry Journal entry to create
     * @param encrypt Whether to encrypt the entry
     * @return Result with entry ID if successful
     */
    suspend fun createEntry(entry: JournalEntry, encrypt: Boolean = false): Result<String>

    /**
     * Update an existing journal entry.
     */
    suspend fun updateEntry(entry: JournalEntry): Result<Unit>

    /**
     * Delete a journal entry.
     */
    suspend fun deleteEntry(entryId: String): Result<Unit>

    /**
     * Toggle favorite status of an entry.
     */
    suspend fun toggleFavorite(entryId: String, isFavorite: Boolean): Result<Unit>

    /**
     * Toggle pinned status of an entry.
     */
    suspend fun togglePinned(entryId: String, isPinned: Boolean): Result<Unit>

    // ========== Statistics ==========

    /**
     * Get total count of journal entries.
     */
    fun getEntryCount(): Flow<Int>

    /**
     * Get total count of favorite entries.
     */
    fun getFavoriteEntryCount(): Flow<Int>

    /**
     * Get total word count across all entries.
     */
    fun getTotalWordCount(): Flow<Int>

    /**
     * Get current journaling streak (consecutive days with entries).
     */
    suspend fun getCurrentStreak(): Int

    /**
     * Get longest journaling streak.
     */
    suspend fun getLongestStreak(): Int

    // ========== Prompt Operations ==========

    /**
     * Get all prompts.
     */
    fun getAllPrompts(): Flow<List<JournalPrompt>>

    /**
     * Get prompts by category.
     */
    fun getPromptsByCategory(category: String): Flow<List<JournalPrompt>>

    /**
     * Get prompts by difficulty.
     */
    fun getPromptsByDifficulty(difficulty: String): Flow<List<JournalPrompt>>

    /**
     * Get favorite prompts.
     */
    fun getFavoritePrompts(): Flow<List<JournalPrompt>>

    /**
     * Get a random prompt.
     */
    suspend fun getRandomPrompt(): JournalPrompt?

    /**
     * Get a random prompt by category.
     */
    suspend fun getRandomPromptByCategory(category: String): JournalPrompt?

    /**
     * Increment usage count for a prompt.
     */
    suspend fun incrementPromptUsage(promptId: String)

    /**
     * Toggle prompt favorite status.
     */
    suspend fun togglePromptFavorite(promptId: String, isFavorite: Boolean): Result<Unit>

    /**
     * Seed initial prompts (called on first app launch).
     */
    suspend fun seedPrompts(): Result<Unit>

    // ========== One-shot methods for export/backup ==========

    /**
     * Get all entries as a one-shot operation (non-Flow).
     * Used for backup and export operations.
     */
    suspend fun getAllEntriesOneShot(): Result<List<JournalEntry>>

    /**
     * Get entry by ID with Result wrapper.
     * Used for error-aware operations.
     */
    suspend fun getEntryByIdResult(entryId: String): Result<JournalEntry?>
}
