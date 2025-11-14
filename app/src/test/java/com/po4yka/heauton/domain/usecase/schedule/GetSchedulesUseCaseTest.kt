package com.po4yka.heauton.domain.usecase.schedule

import app.cash.turbine.test
import com.po4yka.heauton.data.local.database.entities.DeliveryMethod
import com.po4yka.heauton.domain.model.QuoteSchedule
import com.po4yka.heauton.domain.repository.ScheduleRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetSchedulesUseCaseTest {

    private lateinit var repository: ScheduleRepository
    private lateinit var useCase: GetSchedulesUseCase

    private val testSchedule = QuoteSchedule(
        id = "schedule-1",
        scheduledTime = 9 * 60 * 60 * 1000L, // 9:00 AM
        isEnabled = true,
        lastDeliveredQuoteId = null,
        lastDeliveryDate = null,
        deliveryMethod = DeliveryMethod.NOTIFICATION,
        categories = null,
        excludeRecentDays = 7,
        activeDays = listOf(1, 2, 3, 4, 5),
        favoritesOnly = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetSchedulesUseCase(repository)
    }

    @Test
    fun `invoke returns all schedules`() = runTest {
        // Given
        every { repository.getAllSchedules() } returns flowOf(listOf(testSchedule))

        // When & Then
        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(testSchedule, result[0])
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty list when no schedules exist`() = runTest {
        // Given
        every { repository.getAllSchedules() } returns flowOf(emptyList())

        // When & Then
        useCase().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns both enabled and disabled schedules`() = runTest {
        // Given
        val enabledSchedule = testSchedule
        val disabledSchedule = testSchedule.copy(id = "schedule-2", isEnabled = false)
        every { repository.getAllSchedules() } returns flowOf(listOf(enabledSchedule, disabledSchedule))

        // When & Then
        useCase().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertTrue(result.any { it.isEnabled })
            assertTrue(result.any { !it.isEnabled })
            awaitComplete()
        }
    }
}
