package com.po4yka.heauton.domain.usecase.exercise

import app.cash.turbine.test
import com.po4yka.heauton.data.local.database.entities.Difficulty
import com.po4yka.heauton.data.local.database.entities.ExerciseType
import com.po4yka.heauton.domain.model.Exercise
import com.po4yka.heauton.domain.repository.ExerciseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetExercisesUseCaseTest {

    private lateinit var repository: ExerciseRepository
    private lateinit var useCase: GetExercisesUseCase

    private val testExercise = Exercise(
        id = "1",
        title = "Box Breathing",
        description = "A breathing technique",
        type = ExerciseType.BREATHING,
        difficulty = Difficulty.BEGINNER,
        duration = 300, // 5 minutes in seconds
        instructions = listOf("Step 1", "Step 2"),
        category = "Breathing",
        relatedQuoteId = null,
        isFavorite = false,
        breathingPattern = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetExercisesUseCase(repository)
    }

    @Test
    fun `invoke returns all exercises`() = runTest {
        // Given
        every { repository.getAllExercises() } returns flowOf(listOf(testExercise))

        // When & Then
        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(testExercise, result[0])
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty list when no exercises exist`() = runTest {
        // Given
        every { repository.getAllExercises() } returns flowOf(emptyList())

        // When & Then
        useCase().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }
    }
}
