package com.po4yka.heauton.di

import javax.inject.Qualifier

/**
 * Qualifier annotations for repository implementations.
 *
 * These qualifiers allow us to differentiate between different
 * implementations of the same interface in the DI container.
 *
 * ## Usage:
 * ```kotlin
 * @Provides
 * @BaseQuotesRepo
 * fun provideBaseRepository(...): QuotesRepository = QuotesRepositoryImpl(...)
 *
 * @Provides
 * @Singleton
 * fun provideRepository(@BaseQuotesRepo base: QuotesRepository, ...): QuotesRepository {
 *     return PerformanceMonitoringQuotesRepository(
 *         delegate = CachedQuotesRepository(
 *             delegate = base,
 *             cache = cache
 *         ),
 *         monitor = monitor
 *     )
 * }
 * ```
 */

/**
 * Qualifier for the base repository implementation (without decorators).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseQuotesRepo

/**
 * Qualifier for the cached repository implementation.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CachedQuotesRepo

/**
 * Qualifier for the performance-monitored repository implementation.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MonitoredQuotesRepo
