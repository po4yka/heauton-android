package com.po4yka.heauton.domain.usecase.quotes

import com.po4yka.heauton.data.local.search.TextNormalizer
import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import javax.inject.Inject

/**
 * Use case for updating an existing quote.
 */
class UpdateQuoteUseCase @Inject constructor(
    private val repository: QuotesRepository
) {
    /**
     * Updates an existing quote.
     * @param quote The quote with updated values
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(quote: Quote): Result<Unit> {
        // Validation
        val validationError = validateQuote(quote)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }

        // Recalculate word count
        val wordCount = TextNormalizer.wordCount(quote.text)

        val updatedQuote = quote.copy(
            author = quote.author.trim(),
            text = quote.text.trim(),
            source = quote.source?.trim(),
            categories = quote.categories.map { it.trim() },
            tags = quote.tags.map { it.trim() },
            mood = quote.mood?.trim(),
            wordCount = wordCount,
            updatedAt = System.currentTimeMillis()
        )

        return try {
            repository.updateQuote(updatedQuote)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validates quote data.
     * @return Error message if invalid, null if valid
     */
    private fun validateQuote(quote: Quote): String? {
        return when {
            quote.author.isBlank() -> "Author cannot be empty"
            quote.text.isBlank() -> "Quote text cannot be empty"
            quote.author.length > 200 -> "Author name too long (max 200 characters)"
            quote.text.length > 10000 -> "Quote text too long (max 10,000 characters)"
            else -> null
        }
    }
}
