package com.po4yka.heauton.domain.repository

/**
 * Aggregated repository interface for all quote operations.
 *
 * Following Interface Segregation Principle - this interface composes
 * smaller, focused interfaces for better separation of concerns.
 *
 * Clients can depend on:
 * - [QuotesQueryRepository] if they only need read operations
 * - [QuotesMutationRepository] if they only need write operations
 * - [QuotesStatisticsRepository] if they only need statistics
 * - [QuotesRepository] if they need all operations (convenience)
 *
 * ## Benefits:
 * - Better testability (mock only what you need)
 * - Clearer contracts
 * - Follows ISP - clients don't depend on methods they don't use
 */
interface QuotesRepository :
    QuotesQueryRepository,
    QuotesMutationRepository,
    QuotesStatisticsRepository
