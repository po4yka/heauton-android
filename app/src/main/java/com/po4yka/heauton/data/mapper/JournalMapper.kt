package com.po4yka.heauton.data.mapper

import com.po4yka.heauton.data.local.database.entities.JournalEntryEntity
import com.po4yka.heauton.data.local.database.entities.JournalMood
import com.po4yka.heauton.data.local.database.entities.JournalPromptEntity
import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.model.JournalPrompt

/**
 * Extension functions to convert between Journal entity and domain models.
 */

// ========== JournalEntry Mapping ==========

/**
 * Convert JournalEntryEntity to JournalEntry domain model.
 */
fun JournalEntryEntity.toDomain(): JournalEntry {
    return JournalEntry(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        mood = mood?.let { JournalMood.fromString(it) },
        relatedQuoteId = relatedQuoteId,
        tags = tags,
        isFavorite = isFavorite,
        isPinned = isPinned,
        wordCount = wordCount,
        isEncrypted = isEncrypted,
        isStoredInFile = isStoredInFile
    )
}

/**
 * Convert JournalEntry domain model to JournalEntryEntity.
 */
fun JournalEntry.toEntity(): JournalEntryEntity {
    return JournalEntryEntity(
        id = id,
        title = title,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        mood = mood?.name,
        relatedQuoteId = relatedQuoteId,
        tags = tags,
        isFavorite = isFavorite,
        isPinned = isPinned,
        wordCount = wordCount,
        contentFilePath = if (isStoredInFile) "files/$id.md" else null,
        encryptionKeyId = if (isEncrypted) "default" else null
    )
}

/**
 * Convert list of JournalEntryEntity to list of JournalEntry domain models.
 */
fun List<JournalEntryEntity>.toDomain(): List<JournalEntry> {
    return map { it.toDomain() }
}

/**
 * Convert list of JournalEntry domain models to list of JournalEntryEntity.
 */
fun List<JournalEntry>.toEntity(): List<JournalEntryEntity> {
    return map { it.toEntity() }
}

// ========== JournalPrompt Mapping ==========

/**
 * Convert JournalPromptEntity to JournalPrompt domain model.
 */
fun JournalPromptEntity.toDomain(): JournalPrompt {
    return JournalPrompt(
        id = id,
        text = text,
        category = category,
        difficulty = difficulty,
        tags = tags,
        usageCount = usageCount,
        isFavorite = isFavorite
    )
}

/**
 * Convert JournalPrompt domain model to JournalPromptEntity.
 */
fun JournalPrompt.toEntity(): JournalPromptEntity {
    return JournalPromptEntity(
        id = id,
        text = text,
        category = category,
        difficulty = difficulty,
        tags = tags,
        usageCount = usageCount,
        isFavorite = isFavorite
    )
}

/**
 * Convert list of JournalPromptEntity to list of JournalPrompt domain models.
 */
fun List<JournalPromptEntity>.toPromptDomain(): List<JournalPrompt> {
    return map { it.toDomain() }
}

/**
 * Convert list of JournalPrompt domain models to list of JournalPromptEntity.
 */
fun List<JournalPrompt>.toPromptEntity(): List<JournalPromptEntity> {
    return map { it.toEntity() }
}
