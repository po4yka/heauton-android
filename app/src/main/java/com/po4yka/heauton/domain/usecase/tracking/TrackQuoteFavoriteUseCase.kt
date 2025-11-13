package com.po4yka.heauton.domain.usecase.tracking

import com.po4yka.heauton.data.local.database.dao.UserEventDao
import com.po4yka.heauton.data.local.database.entities.UserEventEntity
import com.po4yka.heauton.data.local.database.entities.UserEventTypes
import javax.inject.Inject

/**
 * Use case for tracking quote favorite/unfavorite actions.
 *
 * Logs when a user favorites or unfavorites a quote for analytics and progress tracking.
 */
class TrackQuoteFavoriteUseCase @Inject constructor(
    private val userEventDao: UserEventDao
) {
    /**
     * Track that a quote was favorited or unfavorited.
     *
     * @param quoteId The ID of the quote
     * @param isFavorited True if the quote was favorited, false if unfavorited
     */
    suspend operator fun invoke(quoteId: String, isFavorited: Boolean) {
        val eventType = if (isFavorited) {
            UserEventTypes.QUOTE_FAVORITED
        } else {
            UserEventTypes.QUOTE_UNFAVORITED
        }

        val event = UserEventEntity(
            eventType = eventType,
            relatedEntityId = quoteId,
            timestamp = System.currentTimeMillis()
        )
        userEventDao.insert(event)
    }
}
