package com.po4yka.heauton.data.repository

import com.po4yka.heauton.data.local.database.SampleData
import com.po4yka.heauton.data.local.database.dao.UserEventDao
import com.po4yka.heauton.data.local.database.entities.UserEventEntity
import com.po4yka.heauton.data.local.search.SearchRanker
import com.po4yka.heauton.data.local.search.TextNormalizer
import com.po4yka.heauton.data.local.search.appsearch.AppSearchManager
import com.po4yka.heauton.data.mapper.toDomain
import com.po4yka.heauton.data.mapper.toEntity
import com.po4yka.heauton.data.source.QuotesDataSource
import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.model.QuoteFilter
import com.po4yka.heauton.domain.model.SortOption
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.util.Result
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Base implementation of QuotesRepository.
 *
 * Following Single Responsibility Principle:
 * - ONLY handles data access logic
 * - Does NOT handle caching (delegated to CachedQuotesRepository decorator)
 * - Does NOT handle performance monitoring (delegated to PerformanceMonitoringQuotesRepository decorator)
 *
 * Following Dependency Inversion Principle:
 * - Depends on QuotesDataSource abstraction, not concrete Room DAO
 * - Can work with any data source (Room, Network, Firestore, etc.)
 *
 * See RepositoryModule for how decorators are applied.
 */
@Singleton
class QuotesRepositoryImpl @Inject constructor(
    private val dataSource: QuotesDataSource,
    private val userEventDao: UserEventDao,
    private val appSearchManager: AppSearchManager
) : QuotesRepository {

    // ========== Query Operations ==========

    override fun getAllQuotes(): Flow<List<Quote>> {
        return dataSource.getAllQuotes().map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun getQuoteById(id: String): Quote? {
        return dataSource.getQuoteById(id)?.toDomain()
    }

    override fun getFavoriteQuotes(): Flow<List<Quote>> {
        return dataSource.getFavoriteQuotes().map { entities ->
            entities.toDomain()
        }
    }

    override fun searchQuotes(query: String): Flow<List<Quote>> {
        if (query.isBlank() || query.length < 2) {
            return getAllQuotes()
        }

        // Prepare the query for FTS
        val preparedQuery = TextNormalizer.prepareSearchQuery(query)

        return dataSource.searchQuotes(preparedQuery).mapLatest { entities ->
            val rankedQuotes = SearchRanker.rankResults(
                quotes = entities,
                query = query
            ).toDomain()

            applyAppSearchRanking(query, rankedQuotes)
        }
    }

    override fun getFilteredQuotes(filter: QuoteFilter): Flow<List<Quote>> {
        // Start with the appropriate base query
        val baseFlow = when {
            filter.onlyFavorites -> dataSource.getFavoriteQuotes()
            filter.searchQuery != null -> {
                val preparedQuery = TextNormalizer.prepareSearchQuery(filter.searchQuery)
                dataSource.searchQuotes(preparedQuery)
            }
            filter.author != null -> dataSource.getQuotesByAuthor(filter.author)
            else -> dataSource.getAllQuotes()
        }

        return baseFlow.mapLatest { entities ->
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

            if (!filter.searchQuery.isNullOrBlank()) {
                quotes = applyAppSearchRanking(filter.searchQuery, quotes)
            }

            quotes
        }
    }

    override suspend fun getRandomQuote(excludeRecentIds: List<String>): Quote? {
        return if (excludeRecentIds.isEmpty()) {
            dataSource.getRandomQuote()?.toDomain()
        } else {
            dataSource.getRandomQuoteExcluding(excludeRecentIds)?.toDomain()
        }
    }

    // ========== Mutation Operations ==========

    override suspend fun addQuote(quote: Quote): String {
        val entity = quote.toEntity()
        dataSource.insert(entity)

        runCatching { appSearchManager.indexQuote(quote) }

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

        dataSource.update(entity)

        runCatching { appSearchManager.indexQuote(quote.copy(updatedAt = entity.updatedAt)) }

        // Track event
        userEventDao.insert(
            UserEventEntity(
                eventType = "quote_updated",
                relatedEntityId = quote.id
            )
        )
    }

    override suspend fun deleteQuote(quoteId: String) {
        dataSource.delete(quoteId)

        runCatching { appSearchManager.removeQuote(quoteId) }

        // Track event
        userEventDao.insert(
            UserEventEntity(
                eventType = "quote_deleted",
                relatedEntityId = quoteId
            )
        )
    }

    override suspend fun toggleFavorite(quoteId: String, isFavorite: Boolean) {
        dataSource.updateFavoriteStatus(quoteId, isFavorite)

        runCatching {
            getQuoteById(quoteId)?.let { appSearchManager.indexQuote(it) }
        }

        // Track event
        userEventDao.insert(
            UserEventEntity(
                eventType = if (isFavorite) "quote_favorited" else "quote_unfavorited",
                relatedEntityId = quoteId
            )
        )
    }

    override suspend fun addQuoteResult(quote: Quote): Result<Unit> {
        return try {
            addQuote(quote)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to add quote: ${e.message}")
        }
    }

    override suspend fun deleteQuoteResult(quoteId: String): Result<Unit> {
        return try {
            deleteQuote(quoteId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to delete quote: ${e.message}")
        }
    }

    // ========== Statistics Operations ==========

    override suspend fun markAsRead(quoteId: String) {
        val quote = dataSource.getQuoteById(quoteId)
        if (quote != null) {
            dataSource.incrementReadCount(quoteId)
            dataSource.updateLastReadAt(quoteId, System.currentTimeMillis())

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
        return dataSource.getCount()
    }

    override suspend fun seedSampleQuotes() {
        // Only seed if database is empty
        if (dataSource.isEmpty()) {
            dataSource.insertAll(SampleData.sampleQuotes)
        }
    }

    // ========== Result-based methods ==========

    override suspend fun getAllQuotesOneShot(): Result<List<Quote>> {
        return try {
            val quotes = dataSource.getAllQuotes().first().toDomain()
            Result.success(quotes)
        } catch (e: Exception) {
            Result.error("Failed to get quotes: ${e.message}")
        }
    }

    override suspend fun getQuoteByIdResult(id: String): Result<Quote?> {
        return try {
            val quote = getQuoteById(id)
            Result.success(quote)
        } catch (e: Exception) {
            Result.error("Failed to get quote: ${e.message}")
        }
    }

    private suspend fun applyAppSearchRanking(
        query: String,
        quotes: List<Quote>
    ): List<Quote> {
        val appSearchMatches = runCatching { appSearchManager.searchQuotes(query) }
            .getOrDefault(emptyList())
        if (appSearchMatches.isEmpty()) {
            return quotes
        }

        val scores = appSearchMatches.associate { it.id to it.score }
        return quotes.sortedByDescending { quote ->
            scores[quote.id] ?: Double.NEGATIVE_INFINITY
        }
    }
}
