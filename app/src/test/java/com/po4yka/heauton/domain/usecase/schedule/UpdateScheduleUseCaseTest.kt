package com.po4yka.heauton.domain.usecase.schedule

import com.po4yka.heauton.data.local.database.entities.DeliveryMethod
import com.po4yka.heauton.domain.model.QuoteSchedule
import com.po4yka.heauton.domain.repository.ScheduleRepository
import com.po4yka.heauton.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for UpdateScheduleUseCase.
 */
class UpdateScheduleUseCaseTest {

    private lateinit var repository: ScheduleRepository
    private lateinit var useCase: UpdateScheduleUseCase

    private val testSchedule = QuoteSchedule(
        id = "test-schedule",
        scheduledTime = 9 * 60 * 60 * 1000L, // 9:00 AM
        isEnabled = true,
        lastDeliveredQuoteId = null,
        lastDeliveryDate = null,
        deliveryMethod = DeliveryMethod.BOTH,
        categories = null,
        excludeRecentDays = 7,
        activeDays = null,
        favoritesOnly = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = UpdateScheduleUseCase(repository)
    }

    @Test
    fun `invoke updates entire schedule successfully`() = runTest {
        // Given
        coEvery { repository.updateSchedule(testSchedule) } returns Result.Success(Unit)

        // When
        val result = useCase(testSchedule)

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { repository.updateSchedule(testSchedule) }
    }

    @Test
    fun `updateEnabled updates enabled state successfully`() = runTest {
        // Given
        val scheduleId = "test-schedule"
        val isEnabled = true
        coEvery { repository.updateScheduleEnabled(scheduleId, isEnabled) } returns Result.Success(Unit)

        // When
        val result = useCase.updateEnabled(scheduleId, isEnabled)

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { repository.updateScheduleEnabled(scheduleId, isEnabled) }
    }

    @Test
    fun `updateTime updates scheduled time successfully`() = runTest {
        // Given
        val scheduleId = "test-schedule"
        val hour = 10
        val minute = 30
        coEvery { repository.updateScheduleTime(scheduleId, hour, minute) } returns Result.Success(Unit)

        // When
        val result = useCase.updateTime(scheduleId, hour, minute)

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { repository.updateScheduleTime(scheduleId, hour, minute) }
    }

    @Test
    fun `updateDeliveryMethod updates delivery method successfully`() = runTest {
        // Given
        val scheduleId = "test-schedule"
        val deliveryMethod = DeliveryMethod.NOTIFICATION
        coEvery { repository.updateDeliveryMethod(scheduleId, deliveryMethod) } returns Result.Success(Unit)

        // When
        val result = useCase.updateDeliveryMethod(scheduleId, deliveryMethod)

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { repository.updateDeliveryMethod(scheduleId, deliveryMethod) }
    }

    @Test
    fun `updateEnabled returns error when repository fails`() = runTest {
        // Given
        val scheduleId = "test-schedule"
        val isEnabled = true
        val errorMessage = "Database error"
        coEvery { repository.updateScheduleEnabled(scheduleId, isEnabled) } returns Result.Error(errorMessage)

        // When
        val result = useCase.updateEnabled(scheduleId, isEnabled)

        // Then
        assertTrue(result is Result.Error)
        assertEquals(errorMessage, (result as Result.Error).message)
    }

    @Test
    fun `updateTime handles invalid hour values`() = runTest {
        // Given
        val scheduleId = "test-schedule"
        val invalidHour = 25
        val minute = 0
        coEvery { repository.updateScheduleTime(scheduleId, invalidHour, minute) } returns Result.Success(Unit)

        // When
        val result = useCase.updateTime(scheduleId, invalidHour, minute)

        // Then
        // Repository should handle validation, use case just passes through
        assertTrue(result is Result.Success)
    }

    @Test
    fun `updateDeliveryMethod handles all delivery methods`() = runTest {
        // Given
        val scheduleId = "test-schedule"
        val deliveryMethods = listOf(
            DeliveryMethod.NOTIFICATION,
            DeliveryMethod.WIDGET,
            DeliveryMethod.BOTH
        )

        deliveryMethods.forEach { method ->
            coEvery { repository.updateDeliveryMethod(scheduleId, method) } returns Result.Success(Unit)

            // When
            val result = useCase.updateDeliveryMethod(scheduleId, method)

            // Then
            assertTrue(result is Result.Success)
        }

        coVerify(exactly = 3) { repository.updateDeliveryMethod(any(), any()) }
    }
}
