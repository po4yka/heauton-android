package com.po4yka.heauton.domain.usecase.journal

import com.po4yka.heauton.domain.repository.JournalRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetJournalStreakUseCaseTest {

    private lateinit var repository: JournalRepository
    private lateinit var useCase: GetJournalStreakUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetJournalStreakUseCase(repository)
    }

    @Test
    fun `invoke returns current and longest streak`() = runTest {
        // Given
        val currentStreak = 7
        val longestStreak = 30
        coEvery { repository.getCurrentStreak() } returns currentStreak
        coEvery { repository.getLongestStreak() } returns longestStreak

        // When
        val result = useCase()

        // Then
        assertEquals(currentStreak, result.first)
        assertEquals(longestStreak, result.second)
        coVerify(exactly = 1) { repository.getCurrentStreak() }
        coVerify(exactly = 1) { repository.getLongestStreak() }
    }

    @Test
    fun `invoke returns zero streaks when no entries exist`() = runTest {
        // Given
        coEvery { repository.getCurrentStreak() } returns 0
        coEvery { repository.getLongestStreak() } returns 0

        // When
        val result = useCase()

        // Then
        assertEquals(0, result.first)
        assertEquals(0, result.second)
    }

    @Test
    fun `invoke returns equal streaks when current equals longest`() = runTest {
        // Given
        val streak = 15
        coEvery { repository.getCurrentStreak() } returns streak
        coEvery { repository.getLongestStreak() } returns streak

        // When
        val result = useCase()

        // Then
        assertEquals(streak, result.first)
        assertEquals(streak, result.second)
    }
}
