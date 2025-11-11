package com.po4yka.heauton.data.local.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity representing a journal entry.
 *
 * ## Features:
 * - Markdown content support
 * - Mood tracking
 * - Tag system
 * - Optional quote inspiration
 * - File storage for large entries
 * - Encryption support
 * - Favorites and pinning
 *
 * ## Relationships:
 * - Optional foreign key to QuoteEntity (inspiration quote)
 */
@Entity(
    tableName = "journal_entries",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["updatedAt"]),
        Index(value = ["mood"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isPinned"]),
        Index(value = ["relatedQuoteId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["relatedQuoteId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class JournalEntryEntity(
    /**
     * Unique identifier for the journal entry.
     */
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /**
     * Optional title for the entry.
     * If null, the first line of content is used.
     */
    val title: String? = null,

    /**
     * Main content in Markdown format.
     * For large entries (>10KB), content is stored in a file and this field contains a reference.
     */
    val content: String,

    /**
     * Timestamp when the entry was created (milliseconds since epoch).
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * Timestamp when the entry was last updated (milliseconds since epoch).
     */
    val updatedAt: Long = System.currentTimeMillis(),

    /**
     * Mood associated with this entry.
     * Stored as string name of JournalMood enum.
     */
    val mood: String? = null,

    /**
     * ID of the quote that inspired this entry (optional).
     */
    val relatedQuoteId: String? = null,

    /**
     * Tags associated with this entry for categorization.
     */
    val tags: List<String> = emptyList(),

    /**
     * Whether this entry is marked as favorite.
     */
    val isFavorite: Boolean = false,

    /**
     * Whether this entry is pinned to the top of the list.
     */
    val isPinned: Boolean = false,

    /**
     * Word count of the content (excluding Markdown syntax).
     */
    val wordCount: Int = 0,

    /**
     * Path to external file containing content (for large entries).
     * If not null, the content field contains metadata/preview only.
     */
    val contentFilePath: String? = null,

    /**
     * ID of the encryption key used to encrypt this entry (if encrypted).
     * If null, the entry is not encrypted.
     */
    val encryptionKeyId: String? = null
) {
    /**
     * Check if this entry is encrypted.
     */
    val isEncrypted: Boolean
        get() = encryptionKeyId != null

    /**
     * Get the mood enum from the string value.
     */
    fun getMood(): JournalMood? {
        return mood?.let { JournalMood.fromString(it) }
    }

    /**
     * Check if content is stored in an external file.
     */
    val isStoredInFile: Boolean
        get() = contentFilePath != null
}
