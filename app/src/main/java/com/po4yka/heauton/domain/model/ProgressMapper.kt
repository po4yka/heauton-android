package com.po4yka.heauton.domain.model

import com.po4yka.heauton.data.local.database.entities.AchievementEntity
import com.po4yka.heauton.data.local.database.entities.ProgressSnapshotEntity

/**
 * Converts AchievementEntity to Achievement domain model.
 */
fun AchievementEntity.toDomain(): Achievement {
    return Achievement(
        id = id,
        title = title,
        description = description,
        icon = icon,
        category = category,
        requirement = requirement,
        progress = progress,
        unlockedAt = unlockedAt,
        isHidden = isHidden,
        tier = tier,
        points = points,
        createdAt = createdAt
    )
}

/**
 * Converts Achievement domain model to AchievementEntity.
 */
fun Achievement.toEntity(): AchievementEntity {
    return AchievementEntity(
        id = id,
        title = title,
        description = description,
        icon = icon,
        category = category,
        requirement = requirement,
        progress = progress,
        unlockedAt = unlockedAt,
        isHidden = isHidden,
        tier = tier,
        points = points,
        createdAt = createdAt
    )
}

/**
 * Converts list of AchievementEntity to list of Achievement domain models.
 */
fun List<AchievementEntity>.toDomain(): List<Achievement> {
    return map { it.toDomain() }
}

/**
 * Converts ProgressSnapshotEntity to ProgressSnapshot domain model.
 */
fun ProgressSnapshotEntity.toDomain(): ProgressSnapshot {
    return ProgressSnapshot(
        id = id,
        date = date,
        quotesViewed = quotesViewed,
        quotesFavorited = quotesFavorited,
        journalEntries = journalEntries,
        journalWords = journalWords,
        meditationSessions = meditationSessions,
        meditationMinutes = meditationMinutes,
        breathingSessions = breathingSessions,
        breathingMinutes = breathingMinutes,
        currentStreak = currentStreak,
        mood = mood,
        activityScore = activityScore,
        updatedAt = updatedAt
    )
}

/**
 * Converts ProgressSnapshot domain model to ProgressSnapshotEntity.
 */
fun ProgressSnapshot.toEntity(): ProgressSnapshotEntity {
    return ProgressSnapshotEntity(
        id = id,
        date = date,
        quotesViewed = quotesViewed,
        quotesFavorited = quotesFavorited,
        journalEntries = journalEntries,
        journalWords = journalWords,
        meditationSessions = meditationSessions,
        meditationMinutes = meditationMinutes,
        breathingSessions = breathingSessions,
        breathingMinutes = breathingMinutes,
        currentStreak = currentStreak,
        mood = mood,
        activityScore = activityScore,
        updatedAt = updatedAt
    )
}

/**
 * Converts list of ProgressSnapshotEntity to list of ProgressSnapshot domain models.
 */
fun List<ProgressSnapshotEntity>.toProgressSnapshots(): List<ProgressSnapshot> {
    return map { it.toDomain() }
}
