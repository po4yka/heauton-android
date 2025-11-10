package com.po4yka.heauton.domain.model

/**
 * Domain model representing a quote.
 * This is independent of the database entity and represents business logic.
 */
data class Quote(
    val id: String,
    val author: String,
    val text: String,
    val source: String? = null,
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val mood: String? = null,
    val readCount: Int = 0,
    val lastReadAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long? = null,
    val isFavorite: Boolean = false,
    val wordCount: Int = 0
) {
    /**
     * Returns a formatted display of the quote with author.
     */
    fun getDisplayText(): String = "$text\n\n— $author"

    /**
     * Returns a short preview of the quote (first 100 characters).
     */
    fun getPreview(): String {
        return if (text.length > 100) {
            "${text.take(100)}..."
        } else {
            text
        }
    }

    /**
     * Checks if the quote was read recently (within 24 hours).
     */
    fun isRecentlyRead(): Boolean {
        return lastReadAt?.let {
            System.currentTimeMillis() - it < 24 * 60 * 60 * 1000
        } ?: false
    }
}
