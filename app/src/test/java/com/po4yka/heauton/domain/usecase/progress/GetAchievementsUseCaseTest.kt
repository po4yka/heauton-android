package com.po4yka.heauton.domain.usecase.progress

import app.cash.turbine.test
import com.po4yka.heauton.data.local.database.entities.AchievementCategory
import com.po4yka.heauton.domain.model.Achievement
import com.po4yka.heauton.domain.repository.ProgressRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAchievementsUseCaseTest {

    private lateinit var repository: ProgressRepository
    private lateinit var useCase: GetAchievementsUseCase

    private val testAchievement = Achievement(
        id = "1",
        title = "First Entry",
        description = "Create your first journal entry",
        icon = "journal",
        category = AchievementCategory.JOURNALING,
        requirement = 1,
        progress = 1,
        unlockedAt = System.currentTimeMillis(),
        isHidden = false,
        tier = 1,
        points = 10,
        createdAt = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetAchievementsUseCase(repository)
    }

    @Test
    fun `invoke returns all achievements`() = runTest {
        // Given
        every { repository.getAllAchievements() } returns flowOf(listOf(testAchievement))

        // When & Then
        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(testAchievement, result[0])
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty list when no achievements exist`() = runTest {
        // Given
        every { repository.getAllAchievements() } returns flowOf(emptyList())

        // When & Then
        useCase().test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
            awaitComplete()
        }
    }
}
