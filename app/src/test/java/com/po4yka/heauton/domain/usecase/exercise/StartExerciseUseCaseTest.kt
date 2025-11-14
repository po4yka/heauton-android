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

class StartExerciseUseCaseTest {

    private lateinit var repository: ExerciseRepository
    private lateinit var useCase: StartExerciseUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = StartExerciseUseCase(repository)
    }

    @Test
    fun `invoke starts exercise session`() = runTest {
        // Given
        val exerciseId = "exercise-123"
        val sessionId = "session-456"
        coEvery { repository.startSession(exerciseId, null) } returns Result.Success(sessionId)

        // When
        val result = useCase(exerciseId)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(sessionId, (result as Result.Success).data)
        coVerify(exactly = 1) { repository.startSession(exerciseId, null) }
    }

    @Test
    fun `invoke starts exercise session with mood`() = runTest {
        // Given
        val exerciseId = "exercise-123"
        val moodBefore = "anxious"
        val sessionId = "session-456"
        coEvery { repository.startSession(exerciseId, moodBefore) } returns Result.Success(sessionId)

        // When
        val result = useCase(exerciseId, moodBefore)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(sessionId, (result as Result.Success).data)
        coVerify(exactly = 1) { repository.startSession(exerciseId, moodBefore) }
    }
}
