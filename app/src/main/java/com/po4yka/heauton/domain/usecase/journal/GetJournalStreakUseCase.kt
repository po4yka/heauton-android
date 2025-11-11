package com.po4yka.heauton.domain.usecase.journal

import com.po4yka.heauton.domain.repository.JournalRepository
import javax.inject.Inject

/**
 * Use case to get journal writing streaks.
 *
 * ## Streak Definition:
 * A streak is the number of consecutive days with at least one journal entry.
 * The streak breaks if there's a gap of more than 1 day.
 *
 * ## Features:
 * - Calculates current streak (must include today or yesterday)
 * - Calculates longest streak across all time
 */
class GetJournalStreakUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    /**
     * Get current and longest streaks.
     *
     * @return Pair of (currentStreak, longestStreak)
     */
    suspend operator fun invoke(): Pair<Int, Int> {
        val currentStreak = repository.getCurrentStreak()
        val longestStreak = repository.getLongestStreak()

        return Pair(currentStreak, longestStreak)
    }

    /**
     * Get only the current streak.
     */
    suspend fun getCurrentStreak(): Int {
        return repository.getCurrentStreak()
    }

    /**
     * Get only the longest streak.
     */
    suspend fun getLongestStreak(): Int {
        return repository.getLongestStreak()
    }
}
