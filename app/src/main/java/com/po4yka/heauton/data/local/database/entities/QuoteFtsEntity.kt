package com.po4yka.heauton.data.local.database.entities

import androidx.room.Entity
import androidx.room.Fts4

/**
 * Full-Text Search entity for quotes.
 * Uses FTS4 for efficient text searching.
 */
@Entity(tableName = "quotes_fts")
@Fts4(contentEntity = QuoteEntity::class)
data class QuoteFtsEntity(
    val text: String,
    val author: String,
    val source: String?
)
