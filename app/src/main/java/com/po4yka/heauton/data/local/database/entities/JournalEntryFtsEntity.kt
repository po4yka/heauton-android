package com.po4yka.heauton.data.local.database.entities

import androidx.room.Entity
import androidx.room.Fts4

/**
 * FTS4 (Full-Text Search) virtual table for journal entries.
 *
 * ## Purpose:
 * Enables fast full-text search across journal content and titles.
 *
 * ## Usage:
 * ```kotlin
 * @Query("SELECT * FROM journal_entries WHERE id IN (SELECT docid FROM journal_entries_fts WHERE journal_entries_fts MATCH :query)")
 * fun searchEntries(query: String): Flow<List<JournalEntryEntity>>
 * ```
 *
 * ## Features:
 * - Unicode support (NFKC normalized)
 * - Diacritic removal for better matching
 * - BM25-like ranking for relevance
 *
 * @see JournalEntryEntity
 */
@Entity(tableName = "journal_entries_fts")
@Fts4(contentEntity = JournalEntryEntity::class)
data class JournalEntryFtsEntity(
    /**
     * Journal entry title for search indexing.
     */
    val title: String?,

    /**
     * Journal entry content for search indexing.
     */
    val content: String,

    /**
     * Tags concatenated for search indexing.
     */
    val tags: String?
)
