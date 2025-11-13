package com.po4yka.heauton.di

import android.content.Context
import androidx.room.Room
import com.po4yka.heauton.data.local.database.Converters
import com.po4yka.heauton.data.local.database.HeautonDatabase
import com.po4yka.heauton.data.local.database.dao.AchievementDao
import com.po4yka.heauton.data.local.database.dao.ExerciseDao
import com.po4yka.heauton.data.local.database.dao.JournalDao
import com.po4yka.heauton.data.local.database.dao.ProgressDao
import com.po4yka.heauton.data.local.database.dao.QuoteDao
import com.po4yka.heauton.data.local.database.dao.ScheduleDao
import com.po4yka.heauton.data.local.database.dao.UserEventDao
import com.po4yka.heauton.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the main Room database instance.
     *
     * ## Migration Strategy:
     * - This is version 1.0.0 (pre-release), starting at database version 5
     * - No migrations needed for initial release
     * - Future schema changes must include proper Migration objects
     * - Never use fallbackToDestructiveMigration() in production as it deletes user data
     *
     * ## Adding Migrations:
     * When bumping the database version, add migrations like this:
     * ```
     * val MIGRATION_5_6 = object : Migration(5, 6) {
     *     override fun migrate(database: SupportSQLiteDatabase) {
     *         // SQL statements to migrate from version 5 to 6
     *         database.execSQL("ALTER TABLE ...")
     *     }
     * }
     * ```
     * Then add to the builder: `.addMigrations(MIGRATION_5_6)`
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        converters: Converters
    ): HeautonDatabase {
        return Room.databaseBuilder(
            context,
            HeautonDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .addTypeConverter(converters)
            // Add future migrations here: .addMigrations(MIGRATION_5_6, MIGRATION_6_7, ...)
            .build()
    }

    /**
     * Provides the type converters for Room.
     */
    @Provides
    @Singleton
    fun provideConverters(): Converters {
        return Converters()
    }

    /**
     * Provides the QuoteDao.
     */
    @Provides
    @Singleton
    fun provideQuoteDao(database: HeautonDatabase): QuoteDao {
        return database.quoteDao()
    }

    /**
     * Provides the UserEventDao.
     */
    @Provides
    @Singleton
    fun provideUserEventDao(database: HeautonDatabase): UserEventDao {
        return database.userEventDao()
    }

    /**
     * Provides the JournalDao.
     */
    @Provides
    @Singleton
    fun provideJournalDao(database: HeautonDatabase): JournalDao {
        return database.journalDao()
    }

    /**
     * Provides the ExerciseDao.
     */
    @Provides
    @Singleton
    fun provideExerciseDao(database: HeautonDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    /**
     * Provides the AchievementDao.
     */
    @Provides
    @Singleton
    fun provideAchievementDao(database: HeautonDatabase): AchievementDao {
        return database.achievementDao()
    }

    /**
     * Provides the ProgressDao.
     */
    @Provides
    @Singleton
    fun provideProgressDao(database: HeautonDatabase): ProgressDao {
        return database.progressDao()
    }

    /**
     * Provides the ScheduleDao.
     */
    @Provides
    @Singleton
    fun provideScheduleDao(database: HeautonDatabase): ScheduleDao {
        return database.scheduleDao()
    }
}
