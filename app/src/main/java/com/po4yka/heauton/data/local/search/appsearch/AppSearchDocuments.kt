package com.po4yka.heauton.data.local.search.appsearch

import androidx.appsearch.annotation.Document
import com.po4yka.heauton.data.local.database.entities.JournalMood
import com.po4yka.heauton.data.local.search.TextNormalizer
import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.model.Quote

/**
 * AppSearch document representation for journal entries.
 */
@Document
data class JournalEntryDocument(
    @Document.Namespace val namespace: String = NAMESPACE,
    @Document.Id val id: String,
    @Document.StringProperty val title: String? = null,
    @Document.StringProperty val content: String,
    @Document.StringProperty val tags: List<String> = emptyList(),
    @Document.StringProperty val relatedQuoteId: String? = null,
    @Document.StringProperty val mood: String? = null,
    @Document.LongProperty val createdAt: Long,
    @Document.LongProperty val updatedAt: Long,
    @Document.BooleanProperty val isFavorite: Boolean = false,
    @Document.BooleanProperty val isPinned: Boolean = false,
    @Document.BooleanProperty val isEncrypted: Boolean = false,
    @Document.BooleanProperty val isStoredInFile: Boolean = false
) {
    fun toDomain(): JournalEntry = JournalEntry(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        mood = mood?.let { runCatching { JournalMood.valueOf(it) }.getOrNull() },
        relatedQuoteId = relatedQuoteId,
        tags = tags,
        isFavorite = isFavorite,
        isPinned = isPinned,
        wordCount = TextNormalizer.wordCount(content),
        isEncrypted = isEncrypted,
        isStoredInFile = isStoredInFile
    )

    companion object {
        const val NAMESPACE = "journal_entries"

        fun from(entry: JournalEntry): JournalEntryDocument {
            return JournalEntryDocument(
                id = entry.id,
                title = entry.title,
                content = entry.content,
                tags = entry.tags,
                relatedQuoteId = entry.relatedQuoteId,
                mood = entry.mood?.name,
                createdAt = entry.createdAt,
                updatedAt = entry.updatedAt,
                isFavorite = entry.isFavorite,
                isPinned = entry.isPinned,
                isEncrypted = entry.isEncrypted,
                isStoredInFile = entry.isStoredInFile
            )
        }
    }
}

/**
 * AppSearch document representation for quotes.
 */
@Document
data class QuoteDocument(
    @Document.Namespace val namespace: String = NAMESPACE,
    @Document.Id val id: String,
    @Document.StringProperty val author: String,
    @Document.StringProperty val text: String,
    @Document.StringProperty val source: String? = null,
    @Document.StringProperty val categories: List<String> = emptyList(),
    @Document.StringProperty val tags: List<String> = emptyList(),
    @Document.StringProperty val mood: String? = null,
    @Document.LongProperty val createdAt: Long,
    @Document.LongProperty val updatedAt: Long? = null,
    @Document.BooleanProperty val isFavorite: Boolean = false,
    @Document.LongProperty val lastReadAt: Long? = null,
    @Document.LongProperty val readCount: Long = 0
) {
    fun toDomain(): Quote = Quote(
        id = id,
        author = author,
        text = text,
        source = source,
        categories = categories,
        tags = tags,
        mood = mood,
        readCount = readCount.toInt(),
        lastReadAt = lastReadAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorite = isFavorite,
        wordCount = TextNormalizer.wordCount(text)
    )

    companion object {
        const val NAMESPACE = "quotes"

        fun from(quote: Quote): QuoteDocument {
            return QuoteDocument(
                id = quote.id,
                author = quote.author,
                text = quote.text,
                source = quote.source,
                categories = quote.categories,
                tags = quote.tags,
                mood = quote.mood,
                createdAt = quote.createdAt,
                updatedAt = quote.updatedAt,
                isFavorite = quote.isFavorite,
                lastReadAt = quote.lastReadAt,
                readCount = quote.readCount.toLong()
            )
        }
    }
}
