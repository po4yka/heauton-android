package com.po4yka.heauton.domain.usecase.quotes

import com.po4yka.heauton.domain.repository.QuotesRepository
import javax.inject.Inject

/**
 * Use case for toggling the favorite status of a quote.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: QuotesRepository
) {
    /**
     * Toggles the favorite status of a quote.
     * @param quoteId The quote ID
     * @param isFavorite The new favorite status
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(quoteId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            repository.toggleFavorite(quoteId, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
