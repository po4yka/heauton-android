package com.po4yka.heauton.data.repository

import com.po4yka.heauton.data.local.database.SampleData
import com.po4yka.heauton.data.local.database.dao.QuoteDao
import com.po4yka.heauton.data.local.database.dao.UserEventDao
import com.po4yka.heauton.data.local.database.entities.UserEventEntity
import com.po4yka.heauton.data.local.search.SearchRanker
import com.po4yka.heauton.data.local.search.TextNormalizer
import com.po4yka.heauton.data.mapper.toDomain
import com.po4yka.heauton.data.mapper.toEntity
import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.model.QuoteFilter
import com.po4yka.heauton.domain.model.SortOption
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.util.MemoryCache
import com.po4yka.heauton.util.PerformanceMonitor
import com.po4yka.heauton.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of QuotesRepository using Room database.
 * Integrated with performance monitoring and memory caching.
 */
@Singleton
class QuotesRepositoryImpl @Inject constructor(
    private val quoteDao: QuoteDao,
    private val userEventDao: UserEventDao,
    private val performanceMonitor: PerformanceMonitor,
    private val memoryCache: MemoryCache
) : QuotesRepository {

    override fun getAllQuotes(): Flow<List<Quote>> {
        return quoteDao.getAllQuotes().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getQuoteById(id: String): Quote? {
        return performanceMonitor.measureSuspend("getQuoteById") {
            // Check cache first
            val cached = memoryCache.get<Quote>(MemoryCache.CacheType.QUOTE, id)
            if (cached != null) {
                return@measureSuspend cached
            }

            // Fetch from database
            val quote = quoteDao.getQuoteById(id)?.toDomain()

            // Cache if found
            if (quote != null) {
                memoryCache.put(MemoryCache.CacheType.QUOTE, id, quote)
            }

            quote
        }
    }

    override fun getFavoriteQuotes(): Flow<List<Quote>> {
        return quoteDao.getFavoriteQuotes().map { entities ->
            entities.toDomain()
        }
    }

    override fun searchQuotes(query: String): Flow<List<Quote>> {
        if (query.isBlank() || query.length < 2) {
            return getAllQuotes()
        }

        // Prepare the query for FTS
        val preparedQuery = TextNormalizer.prepareSearchQuery(query)

        return quoteDao.searchQuotes(preparedQuery).map { entities ->
            val quotes = entities.toDomain()

            // Apply additional ranking
            SearchRanker.rankResults(
                quotes = entities,
                query = query
            ).toDomain()
        }
    }

    override fun getFilteredQuotes(filter: QuoteFilter): Flow<List<Quote>> {
        // Start with the appropriate base query
        val baseFlow = when {
            filter.onlyFavorites -> quoteDao.getFavoriteQuotes()
            filter.searchQuery != null -> {
                val preparedQuery = TextNormalizer.prepareSearchQuery(filter.searchQuery)
                quoteDao.searchQuotes(preparedQuery)
            }
            filter.author != null -> quoteDao.getQuotesByAuthor(filter.author)
            else -> quoteDao.getAllQuotes()
        }

        return baseFlow.map { entities ->
            var quotes = entities.toDomain()

            // Apply additional filters
            if (filter.category != null) {
                quotes = quotes.filter { quote ->
                    quote.categories.any { it.equals(filter.category, ignoreCase = true) }
                }
            }

            if (filter.tags.isNotEmpty()) {
                quotes = quotes.filter { quote ->
                    filter.tags.any { filterTag ->
                        quote.tags.any { quoteTag ->
                            quoteTag.equals(filterTag, ignoreCase = true)
                        }
                    }
                }
            }

            if (filter.mood != null) {
                quotes = quotes.filter { it.mood?.equals(filter.mood, ignoreCase = true) == true }
            }

            // Apply sorting
            quotes = when (filter.sortBy) {
                SortOption.CREATED_DESC -> quotes.sortedByDescending { it.createdAt }
                SortOption.CREATED_ASC -> quotes.sortedBy { it.createdAt }
                SortOption.AUTHOR_ASC -> quotes.sortedBy { it.author }
                SortOption.AUTHOR_DESC -> quotes.sortedByDescending { it.author }
                SortOption.READ_COUNT_DESC -> quotes.sortedByDescending { it.readCount }
            }

            quotes
        }
    }

    override suspend fun getRandomQuote(excludeRecentIds: List<String>): Quote? {
        return if (excludeRecentIds.isEmpty()) {
            quoteDao.getRandomQuote()?.toDomain()
        } else {
            quoteDao.getRandomQuoteExcluding(excludeRecentIds)?.toDomain()
        }
    }

    override suspend fun addQuote(quote: Quote): String {
        val entity = quote.toEntity()
        quoteDao.insert(entity)

        // Track event
        userEventDao.insert(
            UserEventEntity(
                eventType = "quote_added",
                relatedEntityId = quote.id
            )
        )

        return quote.id
    }

    override suspend fun updateQuote(quote: Quote) {
        val entity = quote.copy(
            updatedAt = System.currentTimeMillis()
        ).toEntity()

        quoteDao.update(entity)

        // Track event
        userEventDao.insert(
            UserEventEntity(
                eventType = "quote_updated",
                relatedEntityId = quote.id
            )
        )
    }

    override suspend fun deleteQuote(quoteId: String) {
        quoteDao.deleteById(quoteId)

        // Invalidate cache
        memoryCache.remove(MemoryCache.CacheType.QUOTE, quoteId)

        // Track event
        userEventDao.insert(
            UserEventEntity(
                eventType = "quote_deleted",
                relatedEntityId = quoteId
            )
        )
    }

    override suspend fun toggleFavorite(quoteId: String, isFavorite: Boolean) {
        quoteDao.updateFavoriteStatus(quoteId, isFavorite)

        // Track event
        userEventDao.insert(
            UserEventEntity(
                eventType = if (isFavorite) "quote_favorited" else "quote_unfavorited",
                relatedEntityId = quoteId
            )
        )
    }

    override suspend fun markAsRead(quoteId: String) {
        val quote = quoteDao.getQuoteById(quoteId)
        if (quote != null) {
            quoteDao.updateReadStats(
                id = quoteId,
                readCount = quote.readCount + 1,
                lastReadAt = System.currentTimeMillis()
            )

            // Track event
            userEventDao.insert(
                UserEventEntity(
                    eventType = "quote_viewed",
                    relatedEntityId = quoteId
                )
            )
        }
    }

    override suspend fun getQuoteCount(): Int {
        return quoteDao.getQuoteCount()
    }

    override suspend fun seedSampleQuotes() {
        // Only seed if database is empty
        val count = quoteDao.getQuoteCount()
        if (count == 0) {
            quoteDao.insertAll(SampleData.sampleQuotes)
        }
    }

    // Result-based methods for error handling

    override suspend fun getAllQuotesOneShot(): Result<List<Quote>> {
        return try {
            performanceMonitor.measureSuspend("getAllQuotesOneShot") {
                val quotes = quoteDao.getAllQuotes().first().toDomain()
                Result.Success(quotes)
            }
        } catch (e: Exception) {
            Result.Error("Failed to get quotes: ${e.message}")
        }
    }

    override suspend fun getQuoteByIdResult(id: String): Result<Quote?> {
        return try {
            performanceMonitor.measureSuspend("getQuoteByIdResult") {
                val quote = getQuoteById(id)
                Result.Success(quote)
            }
        } catch (e: Exception) {
            Result.Error("Failed to get quote: ${e.message}")
        }
    }

    override suspend fun addQuoteResult(quote: Quote): Result<Unit> {
        return try {
            performanceMonitor.measureSuspend("addQuoteResult") {
                addQuote(quote)
                Result.Success(Unit)
            }
        } catch (e: Exception) {
            Result.Error("Failed to add quote: ${e.message}")
        }
    }

    override suspend fun deleteQuoteResult(quoteId: String): Result<Unit> {
        return try {
            performanceMonitor.measureSuspend("deleteQuoteResult") {
                deleteQuote(quoteId)
                Result.Success(Unit)
            }
        } catch (e: Exception) {
            Result.Error("Failed to delete quote: ${e.message}")
        }
    }
}
