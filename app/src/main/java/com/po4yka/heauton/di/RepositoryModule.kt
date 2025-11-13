package com.po4yka.heauton.di

import com.po4yka.heauton.data.repository.ExerciseRepositoryImpl
import com.po4yka.heauton.data.repository.JournalRepositoryImpl
import com.po4yka.heauton.data.repository.ProgressRepositoryImpl
import com.po4yka.heauton.data.repository.QuotesRepositoryImpl
import com.po4yka.heauton.data.repository.ScheduleRepositoryImpl
import com.po4yka.heauton.data.repository.decorator.CachedQuotesRepository
import com.po4yka.heauton.data.repository.decorator.PerformanceMonitoringQuotesRepository
import com.po4yka.heauton.domain.repository.ExerciseRepository
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.domain.repository.ProgressRepository
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.domain.repository.ScheduleRepository
import com.po4yka.heauton.util.MemoryCache
import com.po4yka.heauton.util.PerformanceMonitor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations.
 *
 * ## Repository Decorator Pattern:
 * The QuotesRepository uses the Decorator pattern to add cross-cutting concerns:
 * 1. Base implementation (QuotesRepositoryImpl) - core data access
 * 2. Caching decorator (CachedQuotesRepository) - adds memory caching
 * 3. Performance monitoring decorator (PerformanceMonitoringQuotesRepository) - tracks metrics
 *
 * This approach follows:
 * - Single Responsibility Principle (each decorator has one concern)
 * - Open/Closed Principle (extend functionality without modifying base)
 * - Decorator Pattern (compose behaviors at runtime)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    companion object {
        /**
         * Provides the QuotesRepository with decorators applied.
         *
         * Decoration chain: PerformanceMonitoring -> Caching -> Base
         * - Performance monitoring logs all operations
         * - Caching reduces database hits
         * - Base implementation handles actual data access
         */
        @Provides
        @Singleton
        fun provideQuotesRepository(
            baseImpl: QuotesRepositoryImpl,
            cache: MemoryCache,
            performanceMonitor: PerformanceMonitor
        ): QuotesRepository {
            // Build decorator chain from inside out
            val cached = CachedQuotesRepository(
                delegate = baseImpl,
                cache = cache
            )

            return PerformanceMonitoringQuotesRepository(
                delegate = cached,
                performanceMonitor = performanceMonitor
            )
        }
    }

    /**
     * Binds the JournalRepository implementation.
     */
    @Binds
    @Singleton
    abstract fun bindJournalRepository(
        impl: JournalRepositoryImpl
    ): JournalRepository

    /**
     * Binds the ExerciseRepository implementation.
     */
    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        impl: ExerciseRepositoryImpl
    ): ExerciseRepository

    /**
     * Binds the ProgressRepository implementation.
     */
    @Binds
    @Singleton
    abstract fun bindProgressRepository(
        impl: ProgressRepositoryImpl
    ): ProgressRepository

    /**
     * Binds the ScheduleRepository implementation.
     */
    @Binds
    @Singleton
    abstract fun bindScheduleRepository(
        impl: ScheduleRepositoryImpl
    ): ScheduleRepository
}
