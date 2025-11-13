package com.po4yka.heauton.data.repository.decorator

import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.model.QuoteFilter
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.util.PerformanceMonitor
import com.po4yka.heauton.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Decorator that adds performance monitoring to a QuotesRepository.
 *
 * Following Decorator Pattern and Single Responsibility Principle:
 * - Adds performance tracking without modifying the base repository
 * - Logs slow operations for debugging and optimization
 * - Can be easily enabled/disabled via DI configuration
 *
 * ## Usage:
 * ```kotlin
 * val repository = PerformanceMonitoringQuotesRepository(
 *     delegate = CachedQuotesRepository(...),
 *     performanceMonitor = PerformanceMonitor()
 * )
 * ```
 *
 * ## Benefits:
 * - Cross-cutting concern (performance) separated from business logic
 * - Can be composed with other decorators (caching, logging, etc.)
 * - Easy to disable in production if needed
 * - Provides valuable performance metrics
 */
class PerformanceMonitoringQuotesRepository @Inject constructor(
    private val delegate: QuotesRepository,
    private val performanceMonitor: PerformanceMonitor
) : QuotesRepository {

    // ========== Query Operations ==========

    override fun getAllQuotes(): Flow<List<Quote>> {
        // Flow operations are monitored at collection time
        return delegate.getAllQuotes()
    }

    override suspend fun getQuoteById(id: String): Quote? {
        return performanceMonitor.measureSuspend("QuotesRepository.getQuoteById") {
            delegate.getQuoteById(id)
        }
    }

    override fun getFavoriteQuotes(): Flow<List<Quote>> {
        return delegate.getFavoriteQuotes()
    }

    override fun searchQuotes(query: String): Flow<List<Quote>> {
        return delegate.searchQuotes(query)
    }

    override fun getFilteredQuotes(filter: QuoteFilter): Flow<List<Quote>> {
        return delegate.getFilteredQuotes(filter)
    }

    override suspend fun getRandomQuote(excludeRecentIds: List<String>): Quote? {
        return performanceMonitor.measureSuspend("QuotesRepository.getRandomQuote") {
            delegate.getRandomQuote(excludeRecentIds)
        }
    }

    override suspend fun getAllQuotesOneShot(): Result<List<Quote>> {
        return performanceMonitor.measureSuspend("QuotesRepository.getAllQuotesOneShot") {
            delegate.getAllQuotesOneShot()
        }
    }

    override suspend fun getQuoteByIdResult(id: String): Result<Quote?> {
        return performanceMonitor.measureSuspend("QuotesRepository.getQuoteByIdResult") {
            delegate.getQuoteByIdResult(id)
        }
    }

    // ========== Mutation Operations ==========

    override suspend fun addQuote(quote: Quote): String {
        return performanceMonitor.measureSuspend("QuotesRepository.addQuote") {
            delegate.addQuote(quote)
        }
    }

    override suspend fun updateQuote(quote: Quote) {
        return performanceMonitor.measureSuspend("QuotesRepository.updateQuote") {
            delegate.updateQuote(quote)
        }
    }

    override suspend fun deleteQuote(quoteId: String) {
        return performanceMonitor.measureSuspend("QuotesRepository.deleteQuote") {
            delegate.deleteQuote(quoteId)
        }
    }

    override suspend fun toggleFavorite(quoteId: String, isFavorite: Boolean) {
        return performanceMonitor.measureSuspend("QuotesRepository.toggleFavorite") {
            delegate.toggleFavorite(quoteId, isFavorite)
        }
    }

    override suspend fun addQuoteResult(quote: Quote): Result<Unit> {
        return performanceMonitor.measureSuspend("QuotesRepository.addQuoteResult") {
            delegate.addQuoteResult(quote)
        }
    }

    override suspend fun deleteQuoteResult(quoteId: String): Result<Unit> {
        return performanceMonitor.measureSuspend("QuotesRepository.deleteQuoteResult") {
            delegate.deleteQuoteResult(quoteId)
        }
    }

    // ========== Statistics Operations ==========

    override suspend fun markAsRead(quoteId: String) {
        return performanceMonitor.measureSuspend("QuotesRepository.markAsRead") {
            delegate.markAsRead(quoteId)
        }
    }

    override suspend fun getQuoteCount(): Int {
        return performanceMonitor.measureSuspend("QuotesRepository.getQuoteCount") {
            delegate.getQuoteCount()
        }
    }

    override suspend fun seedSampleQuotes() {
        return performanceMonitor.measureSuspend("QuotesRepository.seedSampleQuotes") {
            delegate.seedSampleQuotes()
        }
    }
}
