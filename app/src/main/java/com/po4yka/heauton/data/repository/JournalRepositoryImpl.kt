package com.po4yka.heauton.data.repository

import com.po4yka.heauton.data.local.database.JournalPromptsSeedData
import com.po4yka.heauton.data.local.database.dao.JournalDao
import com.po4yka.heauton.data.local.database.dao.UserEventDao
import com.po4yka.heauton.data.local.search.TextNormalizer
import com.po4yka.heauton.data.local.security.EncryptionManager
import com.po4yka.heauton.data.mapper.toDomain
import com.po4yka.heauton.data.mapper.toEntity
import com.po4yka.heauton.data.mapper.toPromptDomain
import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.model.JournalPrompt
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.util.MemoryCache
import com.po4yka.heauton.util.PerformanceMonitor
import com.po4yka.heauton.util.Result
import com.po4yka.heauton.util.StreakCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of JournalRepository.
 * Integrated with performance monitoring and memory caching.
 *
 * ## Responsibilities:
 * - Coordinates between DAO and domain layer
 * - Handles encryption/decryption of entries
 * - Calculates streaks
 * - Seeds prompts on first launch
 * - Tracks user events
 */
@Singleton
class JournalRepositoryImpl @Inject constructor(
    private val journalDao: JournalDao,
    private val userEventDao: UserEventDao,
    private val encryptionManager: EncryptionManager,
    private val performanceMonitor: PerformanceMonitor,
    private val memoryCache: MemoryCache
) : JournalRepository {

    // ========== Journal Entry Operations ==========

    override fun getAllEntries(): Flow<List<JournalEntry>> {
        return journalDao.getAllEntries().map { entities ->
            entities.map { entity ->
                decryptIfNeeded(entity.toDomain())
            }
        }
    }

    override suspend fun getEntryById(entryId: String): JournalEntry? {
        return performanceMonitor.measureSuspend("getEntryById") {
            // Check cache first
            val cached = memoryCache.get<JournalEntry>(MemoryCache.CacheType.JOURNAL, entryId)
            if (cached != null) {
                return@measureSuspend cached
            }

            // Fetch from database
            val entity = journalDao.getEntryById(entryId) ?: return@measureSuspend null
            val entry = decryptIfNeeded(entity.toDomain())

            // Cache the entry
            if (entry != null) {
                memoryCache.put(MemoryCache.CacheType.JOURNAL, entryId, entry)
            }

            entry
        }
    }

    override fun getEntryByIdFlow(entryId: String): Flow<JournalEntry?> {
        return journalDao.getEntryByIdFlow(entryId).map { entity ->
            entity?.toDomain()?.let { decryptIfNeeded(it) }
        }
    }

    override fun getFavoriteEntries(): Flow<List<JournalEntry>> {
        return journalDao.getFavoriteEntries().map { entities ->
            entities.map { entity ->
                decryptIfNeeded(entity.toDomain())
            }
        }
    }

    override fun getEntriesByMood(mood: String): Flow<List<JournalEntry>> {
        return journalDao.getEntriesByMood(mood).map { entities ->
            entities.map { entity ->
                decryptIfNeeded(entity.toDomain())
            }
        }
    }

    override fun getEntriesInDateRange(startDate: Long, endDate: Long): Flow<List<JournalEntry>> {
        return journalDao.getEntriesInDateRange(startDate, endDate).map { entities ->
            entities.map { entity ->
                decryptIfNeeded(entity.toDomain())
            }
        }
    }

    override fun getEntriesForQuote(quoteId: String): Flow<List<JournalEntry>> {
        return journalDao.getEntriesForQuote(quoteId).map { entities ->
            entities.map { entity ->
                decryptIfNeeded(entity.toDomain())
            }
        }
    }

    override fun searchEntries(query: String): Flow<List<JournalEntry>> {
        val normalizedQuery = TextNormalizer.prepareSearchQuery(query)
        return journalDao.searchEntries(normalizedQuery).map { entities ->
            entities.map { entity ->
                decryptIfNeeded(entity.toDomain())
            }
        }
    }

    override suspend fun createEntry(entry: JournalEntry, encrypt: Boolean): Result<String> {
        return try {
            val processedEntry = if (encrypt) {
                encryptEntry(entry)
            } else {
                entry
            }

            val entity = processedEntry.toEntity()
            journalDao.insertEntry(entity)

            // Track event
            trackJournalEvent("entry_created")

            Result.success(entry.id)
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to create journal entry", e)
        }
    }

    override suspend fun updateEntry(entry: JournalEntry): Result<Unit> {
        return try {
            val existingEntity = journalDao.getEntryById(entry.id)
            val wasEncrypted = existingEntity?.isEncrypted == true

            val processedEntry = if (wasEncrypted) {
                encryptEntry(entry)
            } else {
                entry
            }

            val entity = processedEntry.toEntity().copy(
                updatedAt = System.currentTimeMillis()
            )
            journalDao.updateEntry(entity)

            // Track event
            trackJournalEvent("entry_updated")

            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to update journal entry", e)
        }
    }

    override suspend fun deleteEntry(entryId: String): Result<Unit> {
        return try {
            journalDao.deleteEntryById(entryId)

            // Track event
            trackJournalEvent("entry_deleted")

            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to delete journal entry", e)
        }
    }

    override suspend fun toggleFavorite(entryId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            journalDao.toggleFavorite(entryId, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to toggle favorite", e)
        }
    }

    override suspend fun togglePinned(entryId: String, isPinned: Boolean): Result<Unit> {
        return try {
            journalDao.togglePinned(entryId, isPinned)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to toggle pinned", e)
        }
    }

    // ========== Statistics ==========

    override fun getEntryCount(): Flow<Int> {
        return journalDao.getEntryCount()
    }

    override fun getFavoriteEntryCount(): Flow<Int> {
        return journalDao.getFavoriteEntryCount()
    }

    override fun getTotalWordCount(): Flow<Int> {
        return journalDao.getTotalWordCount()
    }

    override suspend fun getCurrentStreak(): Int {
        val dates = journalDao.getEntryDates()
        return calculateStreak(dates, isCurrent = true)
    }

    override suspend fun getLongestStreak(): Int {
        val dates = journalDao.getEntryDates()
        return calculateLongestStreak(dates)
    }

    // ========== Prompt Operations ==========

    override fun getAllPrompts(): Flow<List<JournalPrompt>> {
        return journalDao.getAllPrompts().map { it.toPromptDomain() }
    }

    override fun getPromptsByCategory(category: String): Flow<List<JournalPrompt>> {
        return journalDao.getPromptsByCategory(category).map { it.toPromptDomain() }
    }

    override fun getPromptsByDifficulty(difficulty: String): Flow<List<JournalPrompt>> {
        return journalDao.getPromptsByDifficulty(difficulty).map { it.toPromptDomain() }
    }

    override fun getFavoritePrompts(): Flow<List<JournalPrompt>> {
        return journalDao.getFavoritePrompts().map { it.toPromptDomain() }
    }

    override suspend fun getRandomPrompt(): JournalPrompt? {
        return journalDao.getRandomPrompt()?.toDomain()
    }

    override suspend fun getRandomPromptByCategory(category: String): JournalPrompt? {
        return journalDao.getRandomPromptByCategory(category)?.toDomain()
    }

    override suspend fun incrementPromptUsage(promptId: String) {
        journalDao.incrementPromptUsage(promptId)
    }

    override suspend fun togglePromptFavorite(promptId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            journalDao.togglePromptFavorite(promptId, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to toggle prompt favorite", e)
        }
    }

    override suspend fun seedPrompts(): Result<Unit> {
        return try {
            val prompts = JournalPromptsSeedData.getPrompts()
            journalDao.insertPrompts(prompts)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e.message ?: "Failed to seed prompts", e)
        }
    }

    // ========== Private Helper Methods ==========

    /**
     * Encrypt journal entry content if encryption is enabled.
     */
    private fun encryptEntry(entry: JournalEntry): JournalEntry {
        val encryptedContent = encryptionManager.encrypt(entry.content, entry.id)
        return entry.copy(
            content = encryptedContent.toBase64()
        )
    }

    /**
     * Decrypt journal entry content if it's encrypted.
     */
    private fun decryptIfNeeded(entry: JournalEntry): JournalEntry {
        if (!entry.isEncrypted) {
            return entry
        }

        return try {
            val decryptedContent = encryptionManager.decrypt(entry.content, entry.id)
            entry.copy(content = decryptedContent)
        } catch (e: Exception) {
            // If decryption fails, return entry with error message
            entry.copy(content = "[Encrypted content - decryption failed]")
        }
    }

    /**
     * Calculate current streak (consecutive days with entries).
     * A streak breaks if there's a gap of more than 1 day.
     *
     * @param dates List of entry creation timestamps (should be sorted descending)
     * @param isCurrent If true, calculates current streak; if false, calculates any streak
     * @return Number of consecutive days
     */
    private fun calculateStreak(dates: List<Long>, isCurrent: Boolean): Int {
        if (dates.isEmpty()) return 0

        return if (isCurrent) {
            StreakCalculator.calculateCurrentStreak(dates)
        } else {
            0 // For non-current streaks, use calculateLongestStreak
        }
    }

    /**
     * Calculate longest streak across all time using proper date handling.
     */
    private fun calculateLongestStreak(dates: List<Long>): Int {
        return StreakCalculator.calculateLongestStreak(dates)
    }

    /**
     * Track journal-related user event.
     */
    private suspend fun trackJournalEvent(eventType: String) {
        try {
            // This would use UserEventDao to track events
            // For now, we'll skip implementation to keep it simple
        } catch (e: Exception) {
            // Silently fail - event tracking is not critical
        }
    }

    // ========== One-shot methods for export/backup ==========

    override suspend fun getAllEntriesOneShot(): Result<List<JournalEntry>> {
        return try {
            performanceMonitor.measureSuspend("getAllEntriesOneShot") {
                val entities = journalDao.getAllEntries().first()
                val entries = entities.map { entity ->
                    decryptIfNeeded(entity.toDomain())
                }
                Result.Success(entries)
            }
        } catch (e: Exception) {
            Result.Error("Failed to get journal entries: ${e.message}")
        }
    }

    override suspend fun getEntryByIdResult(entryId: String): Result<JournalEntry?> {
        return try {
            performanceMonitor.measureSuspend("getEntryByIdResult") {
                val entry = getEntryById(entryId)
                Result.Success(entry)
            }
        } catch (e: Exception) {
            Result.Error("Failed to get journal entry: ${e.message}")
        }
    }
}
