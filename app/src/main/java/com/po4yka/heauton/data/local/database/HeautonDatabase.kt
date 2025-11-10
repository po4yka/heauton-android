package com.po4yka.heauton.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.po4yka.heauton.data.local.database.dao.QuoteDao
import com.po4yka.heauton.data.local.database.dao.UserEventDao
import com.po4yka.heauton.data.local.database.entities.QuoteEntity
import com.po4yka.heauton.data.local.database.entities.QuoteFtsEntity
import com.po4yka.heauton.data.local.database.entities.UserEventEntity

/**
 * Main Room database for the Heauton application.
 * Contains all entities and provides access to DAOs.
 */
@Database(
    entities = [
        QuoteEntity::class,
        QuoteFtsEntity::class,
        UserEventEntity::class
    ],
    version = 1,
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
}
