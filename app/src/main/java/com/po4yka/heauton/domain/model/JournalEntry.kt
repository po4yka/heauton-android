package com.po4yka.heauton.domain.model

import com.po4yka.heauton.data.local.database.entities.JournalMood

/**
 * Domain model for a journal entry.
 *
 * ## Immutable Domain Model:
 * Represents a journal entry in the domain layer, decoupled from database implementation.
 */
data class JournalEntry(
    val id: String,
    val title: String?,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val mood: JournalMood?,
    val relatedQuoteId: String?,
    val tags: List<String>,
    val isFavorite: Boolean,
    val isPinned: Boolean,
    val wordCount: Int,
    val isEncrypted: Boolean,
    val isStoredInFile: Boolean
) {
    /**
     * Get display title - uses title if available, otherwise first line of content.
     */
    val displayTitle: String
        get() = title ?: content.lines().firstOrNull()?.take(50) ?: "Untitled"

    /**
     * Get content preview (first 150 characters).
     */
    val preview: String
        get() = if (content.length > 150) {
            content.take(150) + "..."
        } else {
            content
        }

    /**
     * Get formatted date string for display.
     */
    fun getFormattedDate(): String {
        val date = java.util.Date(createdAt)
        val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return format.format(date)
    }

    /**
     * Get formatted time string for display.
     */
    fun getFormattedTime(): String {
        val date = java.util.Date(createdAt)
        val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        return format.format(date)
    }

    /**
     * Check if entry was updated (different from creation time).
     */
    val wasUpdated: Boolean
        get() = updatedAt > createdAt + 1000 // Allow 1 second grace period

    /**
     * Get days since creation.
     */
    fun getDaysSinceCreation(): Long {
        val now = System.currentTimeMillis()
        val diff = now - createdAt
        return diff / (1000 * 60 * 60 * 24)
    }

    companion object {
        /**
         * Create a new empty journal entry.
         */
        fun empty() = JournalEntry(
            id = java.util.UUID.randomUUID().toString(),
            title = null,
            content = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            mood = null,
            relatedQuoteId = null,
            tags = emptyList(),
            isFavorite = false,
            isPinned = false,
            wordCount = 0,
            isEncrypted = false,
            isStoredInFile = false
        )
    }
}
