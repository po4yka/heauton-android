package com.po4yka.heauton.data.mapper

import com.po4yka.heauton.data.local.database.entities.QuoteEntity
import com.po4yka.heauton.domain.model.Quote

/**
 * Extension functions for mapping between Quote entities and domain models.
 */

/**
 * Converts a QuoteEntity to a Quote domain model.
 */
fun QuoteEntity.toDomain(): Quote {
    return Quote(
        id = id,
        author = author,
        text = text,
        source = source,
        categories = categories ?: emptyList(),
        tags = tags ?: emptyList(),
        mood = mood,
        readCount = readCount,
        lastReadAt = lastReadAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorite = isFavorite,
        wordCount = wordCount
    )
}

/**
 * Converts a Quote domain model to a QuoteEntity.
 */
fun Quote.toEntity(): QuoteEntity {
    return QuoteEntity(
        id = id,
        author = author,
        text = text,
        source = source,
        categories = categories.takeIf { it.isNotEmpty() },
        tags = tags.takeIf { it.isNotEmpty() },
        mood = mood,
        readCount = readCount,
        lastReadAt = lastReadAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorite = isFavorite,
        wordCount = wordCount
    )
}

/**
 * Converts a list of QuoteEntity to a list of Quote domain models.
 */
fun List<QuoteEntity>.toDomain(): List<Quote> {
    return map { it.toDomain() }
}

/**
 * Converts a list of Quote domain models to a list of QuoteEntity.
 */
fun List<Quote>.toEntity(): List<QuoteEntity> {
    return map { it.toEntity() }
}
