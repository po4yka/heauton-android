package com.po4yka.heauton.domain.usecase.tracking

import com.po4yka.heauton.data.local.database.dao.UserEventDao
import com.po4yka.heauton.data.local.database.entities.UserEventEntity
import com.po4yka.heauton.data.local.database.entities.UserEventTypes
import javax.inject.Inject

/**
 * Use case for tracking quote views.
 *
 * Logs when a user views a quote for analytics and progress tracking.
 */
class TrackQuoteViewUseCase @Inject constructor(
    private val userEventDao: UserEventDao
) {
    /**
     * Track that a quote was viewed.
     *
     * @param quoteId The ID of the quote that was viewed
     */
    suspend operator fun invoke(quoteId: String) {
        val event = UserEventEntity(
            eventType = UserEventTypes.QUOTE_VIEWED,
            relatedEntityId = quoteId,
            timestamp = System.currentTimeMillis()
        )
        userEventDao.insert(event)
    }
}
