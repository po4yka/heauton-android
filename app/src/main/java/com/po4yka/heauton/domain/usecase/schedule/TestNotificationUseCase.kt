package com.po4yka.heauton.domain.usecase.schedule

import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.util.NotificationHelper
import com.po4yka.heauton.util.Result
import javax.inject.Inject

/**
 * Use case for testing notification delivery.
 *
 * Sends a test notification with a random quote.
 */
class TestNotificationUseCase @Inject constructor(
    private val quotesRepository: QuotesRepository,
    private val notificationHelper: NotificationHelper
) {
    /**
     * Sends a test notification with a random quote.
     * Returns true if successful, false otherwise.
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Get a random quote
            val result = quotesRepository.getRandomQuote()
            when (result) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val quote = result.data as? com.po4yka.heauton.domain.model.Quote
                    if (quote != null) {
                        // Show notification
                        notificationHelper.showDailyQuoteNotification(
                            quoteId = quote.id,
                            author = quote.author,
                            text = quote.text
                        )
                        Result.Success(Unit)
                    } else {
                        Result.Error("No quotes available")
                    }
                }
                is Result.Error -> Result.Error("Failed to get quote: ${result.message}")
                else -> Result.Error("Unknown error occurred")
            }
        } catch (e: Exception) {
            Result.Error("Failed to send test notification: ${e.message}")
        }
    }
}
