package com.po4yka.heauton.domain.repository

import com.po4yka.heauton.data.local.database.entities.AchievementCategory
import com.po4yka.heauton.domain.model.Achievement
import com.po4yka.heauton.domain.model.Insight
import com.po4yka.heauton.domain.model.ProgressSnapshot
import com.po4yka.heauton.domain.model.ProgressStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for progress tracking and achievements.
 *
 * Provides access to achievements, progress snapshots, statistics,
 * and insights generation.
 */
interface ProgressRepository {

    // ============================================================
    // ACHIEVEMENT OPERATIONS
    // ============================================================

    /**
     * Get all achievements as Flow.
     */
    fun getAllAchievements(): Flow<List<Achievement>>

    /**
     * Get achievement by ID.
     */
    suspend fun getAchievementById(id: String): Result<Achievement>

    /**
     * Get achievements by category.
     */
    fun getAchievementsByCategory(category: AchievementCategory): Flow<List<Achievement>>

    /**
     * Get unlocked achievements.
     */
    fun getUnlockedAchievements(): Flow<List<Achievement>>

    /**
     * Get locked achievements.
     */
    fun getLockedAchievements(): Flow<List<Achievement>>

    /**
     * Get recently unlocked achievements.
     */
    fun getRecentlyUnlockedAchievements(daysSince: Int = 7): Flow<List<Achievement>>

    /**
     * Get achievements close to unlocking.
     */
    suspend fun getAlmostUnlockedAchievements(limit: Int = 5): Result<List<Achievement>>

    /**
     * Unlock achievement.
     */
    suspend fun unlockAchievement(achievementId: String): Result<Unit>

    /**
     * Update achievement progress.
     */
    suspend fun updateAchievementProgress(achievementId: String, progress: Int): Result<Unit>

    /**
     * Increment achievement progress.
     */
    suspend fun incrementAchievementProgress(achievementId: String, increment: Int = 1): Result<Unit>

    /**
     * Check and unlock achievements based on current progress.
     * Should be called after activities (journal entry, exercise completion, etc.).
     */
    suspend fun checkAndUnlockAchievements(): Result<List<Achievement>>

    /**
     * Seed achievements if database is empty.
     */
    suspend fun seedAchievementsIfNeeded(): Result<Unit>

    // ============================================================
    // PROGRESS SNAPSHOT OPERATIONS
    // ============================================================

    /**
     * Get all progress snapshots.
     */
    fun getAllSnapshots(): Flow<List<ProgressSnapshot>>

    /**
     * Get snapshot for a specific date.
     */
    suspend fun getSnapshotByDate(date: Long): Result<ProgressSnapshot?>

    /**
     * Get snapshots in date range.
     */
    fun getSnapshotsInRange(startDate: Long, endDate: Long): Flow<List<ProgressSnapshot>>

    /**
     * Get current week snapshots.
     */
    fun getCurrentWeekSnapshots(): Flow<List<ProgressSnapshot>>

    /**
     * Get current month snapshots.
     */
    fun getCurrentMonthSnapshots(): Flow<List<ProgressSnapshot>>

    /**
     * Get current year snapshots.
     */
    fun getCurrentYearSnapshots(): Flow<List<ProgressSnapshot>>

    /**
     * Get active days (days with activity > 0).
     */
    fun getActiveDays(): Flow<List<ProgressSnapshot>>

    /**
     * Create or update today's snapshot.
     */
    suspend fun upsertTodaysSnapshot(snapshot: ProgressSnapshot): Result<Unit>

    /**
     * Record journal entry activity.
     */
    suspend fun recordJournalEntry(words: Int = 0): Result<Unit>

    /**
     * Record meditation session activity.
     */
    suspend fun recordMeditationSession(durationMinutes: Int): Result<Unit>

    /**
     * Record breathing session activity.
     */
    suspend fun recordBreathingSession(durationMinutes: Int): Result<Unit>

    /**
     * Record quote viewed activity.
     */
    suspend fun recordQuoteViewed(): Result<Unit>

    /**
     * Record quote favorited activity.
     */
    suspend fun recordQuoteFavorited(): Result<Unit>

    // ============================================================
    // STATISTICS OPERATIONS
    // ============================================================

    /**
     * Get overall progress statistics.
     */
    suspend fun getProgressStats(): Result<ProgressStats>

    /**
     * Get current streak.
     */
    suspend fun getCurrentStreak(): Result<Int>

    /**
     * Get longest streak.
     */
    suspend fun getLongestStreak(): Result<Int>

    /**
     * Calculate streak from active dates.
     */
    suspend fun calculateStreak(): Result<Int>

    /**
     * Get total active days.
     */
    suspend fun getTotalActiveDays(): Result<Int>

    /**
     * Get mood distribution.
     */
    suspend fun getMoodDistribution(): Result<Map<String, Int>>

    // ============================================================
    // INSIGHTS OPERATIONS
    // ============================================================

    /**
     * Generate insights based on user's progress and patterns.
     */
    suspend fun generateInsights(): Result<List<Insight>>

    /**
     * Get mood trend insights.
     */
    suspend fun getMoodTrendInsights(): Result<List<Insight>>

    /**
     * Get activity pattern insights (best time of day, etc.).
     */
    suspend fun getActivityPatternInsights(): Result<List<Insight>>

    /**
     * Get streak-related insights.
     */
    suspend fun getStreakInsights(): Result<List<Insight>>

    /**
     * Get achievement progress insights.
     */
    suspend fun getAchievementInsights(): Result<List<Insight>>

    /**
     * Get personalized recommendations.
     */
    suspend fun getRecommendations(): Result<List<Insight>>
}
