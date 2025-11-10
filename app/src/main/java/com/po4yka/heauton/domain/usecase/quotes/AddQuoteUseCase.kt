package com.po4yka.heauton.domain.usecase.quotes

import com.po4yka.heauton.data.local.search.TextNormalizer
import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for adding a new quote.
 */
class AddQuoteUseCase @Inject constructor(
    private val repository: QuotesRepository
) {
    /**
     * Validates and adds a new quote.
     * @param author The quote author
     * @param text The quote text
     * @param source Optional source reference
     * @param categories List of categories
     * @param tags List of tags
     * @param mood Optional mood association
     * @return Result indicating success or failure with validation errors
     */
    suspend operator fun invoke(
        author: String,
        text: String,
        source: String? = null,
        categories: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        mood: String? = null
    ): Result<String> {
        // Validation
        val validationError = validateQuote(author, text)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }

        // Calculate word count
        val wordCount = TextNormalizer.wordCount(text)

        // Create quote
        val quote = Quote(
            id = UUID.randomUUID().toString(),
            author = author.trim(),
            text = text.trim(),
            source = source?.trim(),
            categories = categories.map { it.trim() },
            tags = tags.map { it.trim() },
            mood = mood?.trim(),
            createdAt = System.currentTimeMillis(),
            wordCount = wordCount
        )

        return try {
            val id = repository.addQuote(quote)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validates quote data.
     * @return Error message if invalid, null if valid
     */
    private fun validateQuote(author: String, text: String): String? {
        return when {
            author.isBlank() -> "Author cannot be empty"
            text.isBlank() -> "Quote text cannot be empty"
            author.length > 200 -> "Author name too long (max 200 characters)"
            text.length > 10000 -> "Quote text too long (max 10,000 characters)"
            else -> null
        }
    }
}
