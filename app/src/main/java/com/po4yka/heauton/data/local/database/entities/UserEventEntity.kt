package com.po4yka.heauton.data.local.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity for tracking user events and activity.
 */
@Entity(
    tableName = "user_events",
    indices = [
        Index(value = ["eventType"]),
        Index(value = ["timestamp"]),
        Index(value = ["relatedEntityId"])
    ]
)
data class UserEventEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val eventType: String, // "quote_viewed", "quote_favorited", "journal_created", etc.
    val relatedEntityId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: String? = null // JSON string for additional data
)
