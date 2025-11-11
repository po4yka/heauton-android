package com.po4yka.heauton.domain.usecase.progress

import com.po4yka.heauton.domain.model.ProgressSnapshot
import com.po4yka.heauton.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving progress snapshots.
 */
class GetProgressSnapshotsUseCase @Inject constructor(
    private val repository: ProgressRepository
) {
    /**
     * Get all snapshots.
     */
    operator fun invoke(): Flow<List<ProgressSnapshot>> {
        return repository.getAllSnapshots()
    }

    /**
     * Get snapshots for current week.
     */
    fun currentWeek(): Flow<List<ProgressSnapshot>> {
        return repository.getCurrentWeekSnapshots()
    }

    /**
     * Get snapshots for current month.
     */
    fun currentMonth(): Flow<List<ProgressSnapshot>> {
        return repository.getCurrentMonthSnapshots()
    }

    /**
     * Get snapshots for current year.
     */
    fun currentYear(): Flow<List<ProgressSnapshot>> {
        return repository.getCurrentYearSnapshots()
    }

    /**
     * Get active days only.
     */
    fun activeDays(): Flow<List<ProgressSnapshot>> {
        return repository.getActiveDays()
    }

    /**
     * Get snapshots in date range.
     */
    fun inRange(startDate: Long, endDate: Long): Flow<List<ProgressSnapshot>> {
        return repository.getSnapshotsInRange(startDate, endDate)
    }
}
