package com.po4yka.heauton.di

import android.content.Context
import androidx.room.Room
import com.po4yka.heauton.data.local.database.Converters
import com.po4yka.heauton.data.local.database.HeautonDatabase
import com.po4yka.heauton.data.local.database.dao.QuoteDao
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
            .fallbackToDestructiveMigration() // TODO: Add proper migrations for production
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
}
