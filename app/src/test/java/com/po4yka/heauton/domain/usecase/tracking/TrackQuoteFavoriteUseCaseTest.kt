package com.po4yka.heauton.domain.usecase.tracking

import com.po4yka.heauton.data.local.database.dao.UserEventDao
import com.po4yka.heauton.data.local.database.entities.UserEventTypes
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TrackQuoteFavoriteUseCaseTest {

    private lateinit var userEventDao: UserEventDao
    private lateinit var useCase: TrackQuoteFavoriteUseCase

    @Before
    fun setup() {
        userEventDao = mockk(relaxed = true)
        useCase = TrackQuoteFavoriteUseCase(userEventDao)
    }

    @Test
    fun `invoke tracks quote favorite successfully`() = runTest {
        // Given
        val quoteId = "quote-123"
        val isFavorited = true

        // When
        useCase(quoteId, isFavorited)

        // Then
        coVerify(exactly = 1) {
            userEventDao.insert(match {
                it.eventType == UserEventTypes.QUOTE_FAVORITED &&
                it.relatedEntityId == quoteId
            })
        }
    }

    @Test
    fun `invoke tracks unfavorite action`() = runTest {
        // Given
        val quoteId = "quote-123"
        val isFavorited = false

        // When
        useCase(quoteId, isFavorited)

        // Then
        coVerify(exactly = 1) {
            userEventDao.insert(match {
                it.eventType == UserEventTypes.QUOTE_UNFAVORITED &&
                it.relatedEntityId == quoteId
            })
        }
    }
}
