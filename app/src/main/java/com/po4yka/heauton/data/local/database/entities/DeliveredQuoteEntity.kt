package com.po4yka.heauton.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entity representing a delivered quote for tracking recent deliveries.
 *
 * This entity tracks which quotes have been delivered to avoid repetition
 * within the configured exclusion period.
 */
@Entity(
    tableName = "delivered_quotes",
    indices = [
        Index(value = ["quoteId"]),
        Index(value = ["deliveredAt"])
    ]
)
data class DeliveredQuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * ID of the quote that was delivered.
     */
    val quoteId: String,

    /**
     * Timestamp when the quote was delivered.
     */
    val deliveredAt: Long,

    /**
     * ID of the schedule that delivered this quote.
     */
    val scheduleId: String
)
