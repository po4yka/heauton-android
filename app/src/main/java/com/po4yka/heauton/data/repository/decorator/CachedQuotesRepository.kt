package com.po4yka.heauton.data.repository.decorator

import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.model.QuoteFilter
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.util.MemoryCache
import com.po4yka.heauton.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Decorator that adds caching capabilities to a QuotesRepository.
 *
 * Following Decorator Pattern and Open/Closed Principle:
 * - Adds caching functionality without modifying the base repository
 * - Can be composed with other decorators
 * - Implements the same interface as the decorated object
 *
 * ## Usage:
 * ```kotlin
 * val repository = CachedQuotesRepository(
 *     delegate = BaseQuotesRepository(...),
 *     cache = MemoryCache()
 * )
 * ```
 *
 * ## Benefits:
 * - Separation of concerns (caching logic separate from data access)
 * - Can be enabled/disabled via DI configuration
 * - Easy to test (can test with/without caching)
 * - Follows Single Responsibility Principle
 */
class CachedQuotesRepository @Inject constructor(
    private val delegate: QuotesRepository,
    private val cache: MemoryCache
) : QuotesRepository {

    // ========== Query Operations (with caching) ==========

    override fun getAllQuotes(): Flow<List<Quote>> {
        // Flow-based methods don't benefit from simple caching
        // (they're already reactive and up-to-date)
        return delegate.getAllQuotes()
    }

    override suspend fun getQuoteById(id: String): Quote? {
        // Check cache first
        val cached = cache.get<Quote>(MemoryCache.CacheType.QUOTE, id)
        if (cached != null) {
            return cached
        }

        // Cache miss - fetch from delegate
        val quote = delegate.getQuoteById(id)

        // Cache the result if found
        if (quote != null) {
            cache.put(MemoryCache.CacheType.QUOTE, id, quote)
        }

        return quote
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
        // Random quotes shouldn't be cached
        return delegate.getRandomQuote(excludeRecentIds)
    }

    override suspend fun getAllQuotesOneShot(): Result<List<Quote>> {
        return delegate.getAllQuotesOneShot()
    }

    override suspend fun getQuoteByIdResult(id: String): Result<Quote?> {
        // Check cache first
        val cached = cache.get<Quote>(MemoryCache.CacheType.QUOTE, id)
        if (cached != null) {
            return Result.success(cached)
        }

        // Cache miss - fetch from delegate
        val result = delegate.getQuoteByIdResult(id)

        // Cache successful results
        if (result is Result.Success && result.data != null) {
            cache.put(MemoryCache.CacheType.QUOTE, id, result.data)
        }

        return result
    }

    // ========== Mutation Operations (invalidate cache) ==========

    override suspend fun addQuote(quote: Quote): String {
        val id = delegate.addQuote(quote)
        // Cache the newly added quote
        cache.put(MemoryCache.CacheType.QUOTE, id, quote)
        return id
    }

    override suspend fun updateQuote(quote: Quote) {
        delegate.updateQuote(quote)
        // Invalidate cache for this quote
        cache.remove(MemoryCache.CacheType.QUOTE, quote.id)
    }

    override suspend fun deleteQuote(quoteId: String) {
        delegate.deleteQuote(quoteId)
        // Invalidate cache for deleted quote
        cache.remove(MemoryCache.CacheType.QUOTE, quoteId)
    }

    override suspend fun toggleFavorite(quoteId: String, isFavorite: Boolean) {
        delegate.toggleFavorite(quoteId, isFavorite)
        // Invalidate cache since favorite status changed
        cache.remove(MemoryCache.CacheType.QUOTE, quoteId)
    }

    override suspend fun addQuoteResult(quote: Quote): Result<Unit> {
        val result = delegate.addQuoteResult(quote)
        if (result is Result.Success) {
            cache.put(MemoryCache.CacheType.QUOTE, quote.id, quote)
        }
        return result
    }

    override suspend fun deleteQuoteResult(quoteId: String): Result<Unit> {
        val result = delegate.deleteQuoteResult(quoteId)
        if (result is Result.Success) {
            cache.remove(MemoryCache.CacheType.QUOTE, quoteId)
        }
        return result
    }

    // ========== Statistics Operations (pass-through) ==========

    override suspend fun markAsRead(quoteId: String) {
        delegate.markAsRead(quoteId)
        // Invalidate cache since read stats changed
        cache.remove(MemoryCache.CacheType.QUOTE, quoteId)
    }

    override suspend fun getQuoteCount(): Int {
        return delegate.getQuoteCount()
    }

    override suspend fun seedSampleQuotes() {
        delegate.seedSampleQuotes()
        // Clear all quote caches after seeding
        cache.clear(MemoryCache.CacheType.QUOTE)
    }
}
