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

class ToggleFavoriteExerciseUseCaseTest {

    private lateinit var repository: ExerciseRepository
    private lateinit var useCase: ToggleFavoriteExerciseUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ToggleFavoriteExerciseUseCase(repository)
    }

    @Test
    fun `invoke toggles favorite status`() = runTest {
        // Given
        val exerciseId = "exercise-123"
        coEvery { repository.toggleFavorite(exerciseId) } returns Result.Success(Unit)

        // When
        val result = useCase(exerciseId)

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { repository.toggleFavorite(exerciseId) }
    }

    @Test
    fun `invoke handles multiple toggles`() = runTest {
        // Given
        val exerciseId = "exercise-123"
        coEvery { repository.toggleFavorite(exerciseId) } returns Result.Success(Unit)

        // When
        repeat(3) {
            useCase(exerciseId)
        }

        // Then
        coVerify(exactly = 3) { repository.toggleFavorite(exerciseId) }
    }
}
