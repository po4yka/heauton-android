package com.po4yka.heauton.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for storing quotes in the database.
 */
@Entity(
    tableName = "quotes",
    indices = [
        Index(value = ["author"]),
        Index(value = ["isFavorite"]),
        Index(value = ["createdAt"]),
        Index(value = ["lastReadAt"])
    ]
)
data class QuoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val author: String,
    val text: String,
    val source: String? = null,
    val categories: List<String>? = null,
    val tags: List<String>? = null,
    val mood: String? = null,
    val readCount: Int = 0,
    val lastReadAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null,
    val isFavorite: Boolean = false,
    val textFilePath: String? = null,
    val isChunked: Boolean = false,
    val wordCount: Int = 0
)
