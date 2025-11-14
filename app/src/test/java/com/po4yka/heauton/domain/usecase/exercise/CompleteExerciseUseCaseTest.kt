package com.po4yka.heauton.domain.usecase.exercise

import com.po4yka.heauton.domain.repository.ExerciseRepository
import com.po4yka.heauton.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CompleteExerciseUseCaseTest {

    private lateinit var repository: ExerciseRepository
    private lateinit var useCase: CompleteExerciseUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = CompleteExerciseUseCase(repository)
    }

    @Test
    fun `invoke successfully completes exercise`() = runTest {
        // Given
        val sessionId = "session-1"
        val actualDuration = 300
        val moodAfter = "peaceful"
        val notes = "Great session"

        coEvery {
            repository.completeSession(sessionId, actualDuration, moodAfter, notes)
        } returns Result.Success(Unit)

        // When
        val result = useCase(
            sessionId = sessionId,
            actualDuration = actualDuration,
            moodAfter = moodAfter,
            notes = notes
        )

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) {
            repository.completeSession(sessionId, actualDuration, moodAfter, notes)
        }
    }

    @Test
    fun `invoke completes exercise without optional parameters`() = runTest {
        // Given
        val sessionId = "session-1"
        val actualDuration = 180

        coEvery {
            repository.completeSession(sessionId, actualDuration, null, null)
        } returns Result.Success(Unit)

        // When
        val result = useCase(
            sessionId = sessionId,
            actualDuration = actualDuration,
            moodAfter = null,
            notes = null
        )

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) {
            repository.completeSession(sessionId, actualDuration, null, null)
        }
    }

    @Test
    fun `invoke returns error on repository error`() = runTest {
        // Given
        val sessionId = "session-1"
        val actualDuration = 100
        val errorMessage = "Database error"

        coEvery {
            repository.completeSession(sessionId, actualDuration, null, null)
        } returns Result.Error(errorMessage)

        // When
        val result = useCase(
            sessionId = sessionId,
            actualDuration = actualDuration,
            moodAfter = null,
            notes = null
        )

        // Then
        assertTrue(result is Result.Error)
    }
}
