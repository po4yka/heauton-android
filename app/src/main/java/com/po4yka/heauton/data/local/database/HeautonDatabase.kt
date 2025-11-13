package com.po4yka.heauton.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.po4yka.heauton.data.local.database.dao.AchievementDao
import com.po4yka.heauton.data.local.database.dao.ExerciseDao
import com.po4yka.heauton.data.local.database.dao.JournalDao
import com.po4yka.heauton.data.local.database.dao.ProgressDao
import com.po4yka.heauton.data.local.database.dao.QuoteDao
import com.po4yka.heauton.data.local.database.dao.ScheduleDao
import com.po4yka.heauton.data.local.database.dao.UserEventDao
import com.po4yka.heauton.data.local.database.entities.AchievementEntity
import com.po4yka.heauton.data.local.database.entities.DeliveredQuoteEntity
import com.po4yka.heauton.data.local.database.entities.ExerciseEntity
import com.po4yka.heauton.data.local.database.entities.ExerciseSessionEntity
import com.po4yka.heauton.data.local.database.entities.JournalEntryEntity
import com.po4yka.heauton.data.local.database.entities.JournalEntryFtsEntity
import com.po4yka.heauton.data.local.database.entities.JournalPromptEntity
import com.po4yka.heauton.data.local.database.entities.ProgressSnapshotEntity
import com.po4yka.heauton.data.local.database.entities.QuoteEntity
import com.po4yka.heauton.data.local.database.entities.QuoteFtsEntity
import com.po4yka.heauton.data.local.database.entities.QuoteScheduleEntity
import com.po4yka.heauton.data.local.database.entities.UserEventEntity

/**
 * Main Room database for the Heauton application.
 * Contains all entities and provides access to DAOs.
 *
 * ## Version History:
 * - Version 1: Initial release with quotes and user events
 * - Version 2: Added journal entries and prompts (Phase 2)
 * - Version 3: Added exercises and exercise sessions (Phase 3)
 * - Version 4: Added achievements and progress snapshots (Phase 4)
 * - Version 5: Added quote schedules (Phase 5)
 * - Version 6: Added delivered quotes tracking for better quote rotation
 */
@Database(
    entities = [
        QuoteEntity::class,
        QuoteFtsEntity::class,
        UserEventEntity::class,
        JournalEntryEntity::class,
        JournalEntryFtsEntity::class,
        JournalPromptEntity::class,
        ExerciseEntity::class,
        ExerciseSessionEntity::class,
        AchievementEntity::class,
        ProgressSnapshotEntity::class,
        QuoteScheduleEntity::class,
        DeliveredQuoteEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HeautonDatabase : RoomDatabase() {

    /**
     * Provides access to quote operations.
     */
    abstract fun quoteDao(): QuoteDao

    /**
     * Provides access to user event tracking operations.
     */
    abstract fun userEventDao(): UserEventDao

    /**
     * Provides access to journal operations.
     */
    abstract fun journalDao(): JournalDao

    /**
     * Provides access to exercise operations.
     */
    abstract fun exerciseDao(): ExerciseDao

    /**
     * Provides access to achievement operations.
     */
    abstract fun achievementDao(): AchievementDao

    /**
     * Provides access to progress snapshot operations.
     */
    abstract fun progressDao(): ProgressDao

    /**
     * Provides access to schedule operations.
     */
    abstract fun scheduleDao(): ScheduleDao
}
