package com.po4yka.heauton.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.po4yka.heauton.data.local.database.dao.ExerciseDao
import com.po4yka.heauton.data.local.database.dao.JournalDao
import com.po4yka.heauton.data.local.database.dao.QuoteDao
import com.po4yka.heauton.data.local.database.dao.UserEventDao
import com.po4yka.heauton.data.local.database.entities.ExerciseEntity
import com.po4yka.heauton.data.local.database.entities.ExerciseSessionEntity
import com.po4yka.heauton.data.local.database.entities.JournalEntryEntity
import com.po4yka.heauton.data.local.database.entities.JournalEntryFtsEntity
import com.po4yka.heauton.data.local.database.entities.JournalPromptEntity
import com.po4yka.heauton.data.local.database.entities.QuoteEntity
import com.po4yka.heauton.data.local.database.entities.QuoteFtsEntity
import com.po4yka.heauton.data.local.database.entities.UserEventEntity

/**
 * Main Room database for the Heauton application.
 * Contains all entities and provides access to DAOs.
 *
 * ## Version History:
 * - Version 1: Initial release with quotes and user events
 * - Version 2: Added journal entries and prompts (Phase 2)
 * - Version 3: Added exercises and exercise sessions (Phase 3)
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
        ExerciseSessionEntity::class
    ],
    version = 3,
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
}
