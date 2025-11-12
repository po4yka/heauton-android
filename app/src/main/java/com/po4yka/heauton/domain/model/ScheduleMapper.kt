package com.po4yka.heauton.domain.model

import com.po4yka.heauton.data.local.database.entities.QuoteScheduleEntity

/**
 * Mapper functions for converting between Schedule entities and domain models.
 */

/**
 * Converts QuoteScheduleEntity to QuoteSchedule domain model.
 */
fun QuoteScheduleEntity.toDomain(): QuoteSchedule {
    return QuoteSchedule(
        id = id,
        scheduledTime = scheduledTime,
        isEnabled = isEnabled,
        lastDeliveredQuoteId = lastDeliveredQuoteId,
        lastDeliveryDate = lastDeliveryDate,
        deliveryMethod = deliveryMethod,
        categories = categories,
        excludeRecentDays = excludeRecentDays,
        activeDays = activeDays,
        favoritesOnly = favoritesOnly,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Converts QuoteSchedule domain model to QuoteScheduleEntity.
 */
fun QuoteSchedule.toEntity(): QuoteScheduleEntity {
    return QuoteScheduleEntity(
        id = id,
        scheduledTime = scheduledTime,
        isEnabled = isEnabled,
        lastDeliveredQuoteId = lastDeliveredQuoteId,
        lastDeliveryDate = lastDeliveryDate,
        deliveryMethod = deliveryMethod,
        categories = categories,
        excludeRecentDays = excludeRecentDays,
        activeDays = activeDays,
        favoritesOnly = favoritesOnly,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Converts a list of QuoteScheduleEntity to a list of QuoteSchedule domain models.
 */
fun List<QuoteScheduleEntity>.toDomain(): List<QuoteSchedule> {
    return map { it.toDomain() }
}

/**
 * Converts a list of QuoteSchedule domain models to a list of QuoteScheduleEntity.
 */
fun List<QuoteSchedule>.toEntity(): List<QuoteScheduleEntity> {
    return map { it.toEntity() }
}
