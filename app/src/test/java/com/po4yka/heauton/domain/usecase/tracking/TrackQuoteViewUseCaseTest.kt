package com.po4yka.heauton.domain.usecase.tracking

import com.po4yka.heauton.data.local.database.dao.UserEventDao
import com.po4yka.heauton.data.local.database.entities.UserEventTypes
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TrackQuoteViewUseCaseTest {

    private lateinit var userEventDao: UserEventDao
    private lateinit var useCase: TrackQuoteViewUseCase

    @Before
    fun setup() {
        userEventDao = mockk(relaxed = true)
        useCase = TrackQuoteViewUseCase(userEventDao)
    }

    @Test
    fun `invoke tracks quote view successfully`() = runTest {
        // Given
        val quoteId = "quote-123"

        // When
        useCase(quoteId)

        // Then
        coVerify(exactly = 1) {
            userEventDao.insert(match {
                it.eventType == UserEventTypes.QUOTE_VIEWED &&
                it.relatedEntityId == quoteId
            })
        }
    }

    @Test
    fun `invoke tracks multiple quote views`() = runTest {
        // Given
        val quoteIds = listOf("quote-1", "quote-2", "quote-3")

        // When
        quoteIds.forEach { useCase(it) }

        // Then
        quoteIds.forEach { id ->
            coVerify(exactly = 1) {
                userEventDao.insert(match {
                    it.eventType == UserEventTypes.QUOTE_VIEWED &&
                    it.relatedEntityId == id
                })
            }
        }
    }
}
