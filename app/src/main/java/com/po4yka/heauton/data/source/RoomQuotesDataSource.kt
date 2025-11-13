package com.po4yka.heauton.data.source

import com.po4yka.heauton.data.local.database.dao.QuoteDao
import com.po4yka.heauton.data.local.database.entities.QuoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room database implementation of [QuotesDataSource].
 *
 * This class bridges the abstraction layer with the concrete Room DAO implementation.
 * It allows the repository to work with any data source, not just Room.
 *
 * ## Benefits:
 * - Repository is decoupled from Room-specific details
 * - Easy to add other data sources (network, cache, etc.)
 * - Better testability with fake data sources
 * - Follows Dependency Inversion Principle
 */
@Singleton
class RoomQuotesDataSource @Inject constructor(
    private val quoteDao: QuoteDao
) : QuotesDataSource {

    // ========== Query Operations ==========

    override fun getAllQuotes(): Flow<List<QuoteEntity>> {
        return quoteDao.getAllQuotes()
    }

    override suspend fun getQuoteById(id: String): QuoteEntity? {
        return quoteDao.getQuoteById(id)
    }

    override fun getFavoriteQuotes(): Flow<List<QuoteEntity>> {
        return quoteDao.getFavoriteQuotes()
    }

    override fun getQuotesByAuthor(author: String): Flow<List<QuoteEntity>> {
        return quoteDao.getQuotesByAuthor(author)
    }

    override fun searchQuotes(query: String): Flow<List<QuoteEntity>> {
        return quoteDao.searchQuotes(query)
    }

    override suspend fun getRandomQuote(): QuoteEntity? {
        return quoteDao.getRandomQuote()
    }

    override suspend fun getRandomQuoteExcluding(excludedIds: List<String>): QuoteEntity? {
        return quoteDao.getRandomQuoteExcluding(excludedIds)
    }

    // ========== Mutation Operations ==========

    override suspend fun insert(quote: QuoteEntity): Long {
        return quoteDao.insert(quote)
    }

    override suspend fun insertAll(quotes: List<QuoteEntity>) {
        quoteDao.insertAll(quotes)
    }

    override suspend fun update(quote: QuoteEntity) {
        quoteDao.update(quote)
    }

    override suspend fun delete(quoteId: String) {
        quoteDao.delete(quoteId)
    }

    override suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean) {
        quoteDao.updateFavoriteStatus(id, isFavorite)
    }

    override suspend fun incrementReadCount(id: String) {
        quoteDao.incrementReadCount(id)
    }

    override suspend fun updateLastReadAt(id: String, timestamp: Long) {
        quoteDao.updateLastReadAt(id, timestamp)
    }

    // ========== Statistics Operations ==========

    override suspend fun getCount(): Int {
        return quoteDao.getCount()
    }

    override suspend fun isEmpty(): Boolean {
        return quoteDao.getAllQuotes().first().isEmpty()
    }
}
