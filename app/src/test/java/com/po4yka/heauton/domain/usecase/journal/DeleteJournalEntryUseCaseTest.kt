package com.po4yka.heauton.domain.usecase.journal

import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteJournalEntryUseCaseTest {

    private lateinit var repository: JournalRepository
    private lateinit var useCase: DeleteJournalEntryUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DeleteJournalEntryUseCase(repository)
    }

    @Test
    fun `invoke successfully deletes entry`() = runTest {
        // Given
        val entryId = "test-entry-id"
        coEvery { repository.deleteEntry(entryId) } returns Result.Success(Unit)

        // When
        useCase(entryId)

        // Then
        coVerify(exactly = 1) { repository.deleteEntry(entryId) }
    }

    @Test
    fun `invoke deletes multiple entries`() = runTest {
        // Given
        val entryIds = listOf("id1", "id2", "id3")
        entryIds.forEach { id ->
            coEvery { repository.deleteEntry(id) } returns Result.Success(Unit)
        }

        // When
        entryIds.forEach { useCase(it) }

        // Then
        entryIds.forEach { id ->
            coVerify(exactly = 1) { repository.deleteEntry(id) }
        }
    }
}
