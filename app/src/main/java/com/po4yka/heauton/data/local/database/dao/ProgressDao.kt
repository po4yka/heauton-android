package com.po4yka.heauton.data.local.database.dao

import androidx.room.*
import com.po4yka.heauton.data.local.database.entities.ProgressSnapshotEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for progress snapshots.
 *
 * Manages daily progress aggregation for analytics and visualizations.
 */
@Dao
interface ProgressDao {

    // ============================================================
    // QUERY OPERATIONS
    // ============================================================

    /**
     * Get all progress snapshots ordered by date.
     */
    @Query("SELECT * FROM progress_snapshots ORDER BY date DESC")
    fun getAllSnapshots(): Flow<List<ProgressSnapshotEntity>>

    /**
     * Get snapshot by ID.
     */
    @Query("SELECT * FROM progress_snapshots WHERE id = :id")
    suspend fun getSnapshotById(id: String): ProgressSnapshotEntity?

    /**
     * Get snapshot for a specific date.
     */
    @Query("SELECT * FROM progress_snapshots WHERE date = :date")
    suspend fun getSnapshotByDate(date: Long): ProgressSnapshotEntity?

    /**
     * Get snapshots in date range.
     */
    @Query("""
        SELECT * FROM progress_snapshots
        WHERE date >= :startDate AND date <= :endDate
        ORDER BY date DESC
    """)
    fun getSnapshotsInRange(startDate: Long, endDate: Long): Flow<List<ProgressSnapshotEntity>>

    /**
     * Get last N snapshots.
     */
    @Query("SELECT * FROM progress_snapshots ORDER BY date DESC LIMIT :limit")
    suspend fun getLastNSnapshots(limit: Int): List<ProgressSnapshotEntity>

    /**
     * Get snapshots for current week.
     */
    @Query("""
        SELECT * FROM progress_snapshots
        WHERE date >= :weekStart
        ORDER BY date DESC
    """)
    fun getCurrentWeekSnapshots(weekStart: Long): Flow<List<ProgressSnapshotEntity>>

    /**
     * Get snapshots for current month.
     */
    @Query("""
        SELECT * FROM progress_snapshots
        WHERE date >= :monthStart
        ORDER BY date DESC
    """)
    fun getCurrentMonthSnapshots(monthStart: Long): Flow<List<ProgressSnapshotEntity>>

    /**
     * Get snapshots for current year.
     */
    @Query("""
        SELECT * FROM progress_snapshots
        WHERE date >= :yearStart
        ORDER BY date DESC
    """)
    fun getCurrentYearSnapshots(yearStart: Long): Flow<List<ProgressSnapshotEntity>>

    /**
     * Get active days (days with activityScore > 0).
     */
    @Query("""
        SELECT * FROM progress_snapshots
        WHERE activityScore > 0
        ORDER BY date DESC
    """)
    fun getActiveDays(): Flow<List<ProgressSnapshotEntity>>

    /**
     * Get days with specific activity.
     */
    @Query("""
        SELECT * FROM progress_snapshots
        WHERE journalEntries > 0
        ORDER BY date DESC
    """)
    fun getDaysWithJournalEntries(): Flow<List<ProgressSnapshotEntity>>

    @Query("""
        SELECT * FROM progress_snapshots
        WHERE meditationSessions > 0
        ORDER BY date DESC
    """)
    fun getDaysWithMeditation(): Flow<List<ProgressSnapshotEntity>>

    @Query("""
        SELECT * FROM progress_snapshots
        WHERE breathingSessions > 0
        ORDER BY date DESC
    """)
    fun getDaysWithBreathing(): Flow<List<ProgressSnapshotEntity>>

    // ============================================================
    // STATISTICS QUERIES
    // ============================================================

    /**
     * Get total journal entries count.
     */
    @Query("SELECT SUM(journalEntries) FROM progress_snapshots")
    suspend fun getTotalJournalEntries(): Int?

    /**
     * Get total journal words written.
     */
    @Query("SELECT SUM(journalWords) FROM progress_snapshots")
    suspend fun getTotalJournalWords(): Int?

    /**
     * Get total meditation sessions.
     */
    @Query("SELECT SUM(meditationSessions) FROM progress_snapshots")
    suspend fun getTotalMeditationSessions(): Int?

    /**
     * Get total meditation minutes.
     */
    @Query("SELECT SUM(meditationMinutes) FROM progress_snapshots")
    suspend fun getTotalMeditationMinutes(): Int?

    /**
     * Get total breathing sessions.
     */
    @Query("SELECT SUM(breathingSessions) FROM progress_snapshots")
    suspend fun getTotalBreathingSessions(): Int?

    /**
     * Get total breathing minutes.
     */
    @Query("SELECT SUM(breathingMinutes) FROM progress_snapshots")
    suspend fun getTotalBreathingMinutes(): Int?

    /**
     * Get count of active days.
     */
    @Query("SELECT COUNT(*) FROM progress_snapshots WHERE activityScore > 0")
    suspend fun getActiveDaysCount(): Int

    /**
     * Get longest streak.
     */
    @Query("SELECT MAX(currentStreak) FROM progress_snapshots")
    suspend fun getLongestStreak(): Int?

    /**
     * Get current streak (from most recent snapshot).
     */
    @Query("SELECT currentStreak FROM progress_snapshots ORDER BY date DESC LIMIT 1")
    suspend fun getCurrentStreak(): Int?

    /**
     * Get average activity score.
     */
    @Query("SELECT AVG(activityScore) FROM progress_snapshots WHERE activityScore > 0")
    suspend fun getAverageActivityScore(): Float?

    /**
     * Get distinct dates with completed sessions for streak calculation.
     */
    @Query("""
        SELECT DISTINCT date
        FROM progress_snapshots
        WHERE activityScore > 0
        ORDER BY date DESC
    """)
    suspend fun getDistinctActiveDates(): List<Long>

    // ============================================================
    // MOOD QUERIES
    // ============================================================

    /**
     * Get snapshots by mood.
     */
    @Query("""
        SELECT * FROM progress_snapshots
        WHERE mood = :mood
        ORDER BY date DESC
    """)
    fun getSnapshotsByMood(mood: String): Flow<List<ProgressSnapshotEntity>>

    /**
     * Get mood distribution (count of each mood).
     */
    @Query("""
        SELECT mood, COUNT(*) as count
        FROM progress_snapshots
        WHERE mood IS NOT NULL
        GROUP BY mood
        ORDER BY count DESC
    """)
    suspend fun getMoodDistribution(): List<MoodCount>

    // ============================================================
    // INSERT/UPDATE OPERATIONS
    // ============================================================

    /**
     * Insert snapshot.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: ProgressSnapshotEntity)

    /**
     * Insert multiple snapshots.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshots(snapshots: List<ProgressSnapshotEntity>)

    /**
     * Update snapshot.
     */
    @Update
    suspend fun updateSnapshot(snapshot: ProgressSnapshotEntity)

    /**
     * Upsert snapshot (insert or update).
     */
    @Transaction
    suspend fun upsertSnapshot(snapshot: ProgressSnapshotEntity) {
        val existing = getSnapshotByDate(snapshot.date)
        if (existing != null) {
            // Update existing snapshot
            updateSnapshot(snapshot.copy(id = existing.id))
        } else {
            // Insert new snapshot
            insertSnapshot(snapshot)
        }
    }

    /**
     * Increment journal entries for a date.
     */
    @Query("""
        UPDATE progress_snapshots
        SET journalEntries = journalEntries + 1,
            activityScore = activityScore + 2,
            updatedAt = :timestamp
        WHERE date = :date
    """)
    suspend fun incrementJournalEntries(date: Long, timestamp: Long)

    /**
     * Increment meditation session for a date.
     */
    @Query("""
        UPDATE progress_snapshots
        SET meditationSessions = meditationSessions + 1,
            meditationMinutes = meditationMinutes + :minutes,
            activityScore = activityScore + 3,
            updatedAt = :timestamp
        WHERE date = :date
    """)
    suspend fun incrementMeditationSession(date: Long, minutes: Int, timestamp: Long)

    /**
     * Increment breathing session for a date.
     */
    @Query("""
        UPDATE progress_snapshots
        SET breathingSessions = breathingSessions + 1,
            breathingMinutes = breathingMinutes + :minutes,
            activityScore = activityScore + 2,
            updatedAt = :timestamp
        WHERE date = :date
    """)
    suspend fun incrementBreathingSession(date: Long, minutes: Int, timestamp: Long)

    // ============================================================
    // DELETE OPERATIONS
    // ============================================================

    /**
     * Delete snapshot.
     */
    @Delete
    suspend fun deleteSnapshot(snapshot: ProgressSnapshotEntity)

    /**
     * Delete all snapshots.
     */
    @Query("DELETE FROM progress_snapshots")
    suspend fun deleteAllSnapshots()

    /**
     * Delete snapshots older than a certain date.
     */
    @Query("DELETE FROM progress_snapshots WHERE date < :beforeDate")
    suspend fun deleteSnapshotsOlderThan(beforeDate: Long)
}

/**
 * Data class for mood count statistics.
 */
data class MoodCount(
    val mood: String,
    val count: Int
)
