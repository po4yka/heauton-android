package com.po4yka.heauton.domain.repository

/**
 * Repository interface for quote statistics and metadata operations.
 *
 * Following Interface Segregation Principle - contains only statistics/metadata operations.
 * Clients that only need statistics don't depend on query or mutation methods.
 */
interface QuotesStatisticsRepository {

    /**
     * Updates read statistics for a quote.
     * @param quoteId The quote ID
     */
    suspend fun markAsRead(quoteId: String)

    /**
     * Gets the total count of quotes.
     * @return The total number of quotes
     */
    suspend fun getQuoteCount(): Int

    /**
     * Seeds the database with initial sample quotes.
     * Only seeds if the database is empty.
     */
    suspend fun seedSampleQuotes()
}
