package com.po4yka.heauton.domain.usecase.data

import android.content.Context
import android.net.Uri
import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class RestoreDataUseCaseTest {

    private lateinit var context: Context
    private lateinit var quotesRepository: QuotesRepository
    private lateinit var useCase: RestoreDataUseCase

    private val validBackupJson = """
        {
          "version": "1.0",
          "exportDate": "2024-01-15T12:00:00.000Z",
          "appName": "Heauton",
          "platform": "Android",
          "counts": {
            "quotes": 2,
            "journalEntries": 1,
            "schedules": 0
          },
          "quotes": [
            {
              "id": "quote-1",
              "author": "Marcus Aurelius",
              "text": "You have power over your mind.",
              "source": "Meditations",
              "categories": ["Philosophy"],
              "tags": ["stoicism"],
              "mood": "MOTIVATED",
              "isFavorite": true,
              "createdAt": "2024-01-15T12:00:00.000Z"
            },
            {
              "id": "quote-2",
              "author": "Seneca",
              "text": "We suffer more in imagination than in reality.",
              "source": null,
              "categories": [],
              "tags": [],
              "mood": null,
              "isFavorite": false,
              "createdAt": "2024-01-15T12:00:00.000Z"
            }
          ],
          "journalEntriesMetadata": []
        }
    """.trimIndent()

    private lateinit var contentResolver: android.content.ContentResolver

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        contentResolver = mockk(relaxed = true)
        every { context.contentResolver } returns contentResolver
        quotesRepository = mockk()
        useCase = RestoreDataUseCase(context, quotesRepository)
    }

    @Test
    fun `invoke with SKIP_EXISTING imports only new quotes`() = runTest {
        // Given
        val inputUri = mockk<Uri>()
        val inputStream = ByteArrayInputStream(validBackupJson.toByteArray())
        every { contentResolver.openInputStream(any()) } returns inputStream

        // quote-1 exists, quote-2 doesn't
        coEvery { quotesRepository.getQuoteByIdResult("quote-1") } returns Result.Success(mockk())
        coEvery { quotesRepository.getQuoteByIdResult("quote-2") } returns Result.Success(null)
        coEvery { quotesRepository.addQuoteResult(any()) } returns Result.Success(Unit)

        // When
        val result = useCase(inputUri, RestoreDataUseCase.RestoreStrategy.SKIP_EXISTING)

        // Then
        assertTrue(result is Result.Success)
        val summary = (result as Result.Success).data
        assertEquals(1, summary.quotesImported)
        assertEquals(1, summary.quotesSkipped)
        assertEquals(0, summary.quotesReplaced)
        assertEquals(2, summary.totalQuotes)

        coVerify(exactly = 1) { quotesRepository.addQuoteResult(match { it.id == "quote-2" }) }
        coVerify(exactly = 0) { quotesRepository.addQuoteResult(match { it.id == "quote-1" }) }
    }

    @Test
    fun `invoke with REPLACE_EXISTING replaces all quotes`() = runTest {
        // Given
        val inputUri = mockk<Uri>()
        val inputStream = ByteArrayInputStream(validBackupJson.toByteArray())
        every { contentResolver.openInputStream(any()) } returns inputStream

        coEvery { quotesRepository.deleteQuoteResult(any()) } returns Result.Success(Unit)
        coEvery { quotesRepository.addQuoteResult(any()) } returns Result.Success(Unit)

        // When
        val result = useCase(inputUri, RestoreDataUseCase.RestoreStrategy.REPLACE_EXISTING)

        // Then
        assertTrue(result is Result.Success)
        val summary = (result as Result.Success).data
        assertEquals(0, summary.quotesImported)
        assertEquals(0, summary.quotesSkipped)
        assertEquals(2, summary.quotesReplaced)

        coVerify(exactly = 2) { quotesRepository.deleteQuoteResult(any()) }
        coVerify(exactly = 2) { quotesRepository.addQuoteResult(any()) }
    }

    @Test
    fun `invoke with IMPORT_AS_NEW imports all with new IDs`() = runTest {
        // Given
        val inputUri = mockk<Uri>()
        val inputStream = ByteArrayInputStream(validBackupJson.toByteArray())
        every { contentResolver.openInputStream(any()) } returns inputStream

        coEvery { quotesRepository.addQuoteResult(any()) } returns Result.Success(Unit)

        // When
        val result = useCase(inputUri, RestoreDataUseCase.RestoreStrategy.IMPORT_AS_NEW)

        // Then
        assertTrue(result is Result.Success)
        val summary = (result as Result.Success).data
        assertEquals(2, summary.quotesImported)
        assertEquals(0, summary.quotesSkipped)
        assertEquals(0, summary.quotesReplaced)

        // Verify new IDs were generated (not the original ones)
        coVerify(exactly = 2) { quotesRepository.addQuoteResult(match { it.id != "quote-1" && it.id != "quote-2" }) }
    }

    @Test
    fun `invoke properly parses quote fields`() = runTest {
        // Given
        val inputUri = mockk<Uri>()
        val inputStream = ByteArrayInputStream(validBackupJson.toByteArray())
        every { contentResolver.openInputStream(any()) } returns inputStream

        coEvery { quotesRepository.getQuoteByIdResult(any()) } returns Result.Success(null)
        coEvery { quotesRepository.addQuoteResult(any()) } returns Result.Success(Unit)

        // When
        val result = useCase(inputUri, RestoreDataUseCase.RestoreStrategy.SKIP_EXISTING)

        // Then
        assertTrue(result is Result.Success)

        // Verify quote-1 fields
        coVerify {
            quotesRepository.addQuoteResult(match { quote ->
                quote.id == "quote-1" &&
                quote.author == "Marcus Aurelius" &&
                quote.text == "You have power over your mind." &&
                quote.source == "Meditations" &&
                quote.categories == listOf("Philosophy") &&
                quote.tags == listOf("stoicism") &&
                quote.mood == "MOTIVATED" &&
                quote.isFavorite == true
            })
        }

        // Verify quote-2 with nulls
        coVerify {
            quotesRepository.addQuoteResult(match { quote ->
                quote.id == "quote-2" &&
                quote.source == null &&
                quote.categories.isEmpty() &&
                quote.tags.isEmpty() &&
                quote.mood == null &&
                quote.isFavorite == false
            })
        }
    }

    @Test
    fun `invoke returns error for unsupported version`() = runTest {
        // Given
        val invalidVersionJson = """{"version": "2.0", "quotes": []}"""
        val inputUri = mockk<Uri>()
        val inputStream = ByteArrayInputStream(invalidVersionJson.toByteArray())
        every { contentResolver.openInputStream(any()) } returns inputStream

        // When
        val result = useCase(inputUri)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Unsupported backup version"))
    }

    @Test
    fun `invoke returns error for invalid JSON`() = runTest {
        // Given
        val invalidJson = """{"invalid": json syntax"""
        val inputUri = mockk<Uri>()
        val inputStream = ByteArrayInputStream(invalidJson.toByteArray())
        every { contentResolver.openInputStream(any()) } returns inputStream

        // When
        val result = useCase(inputUri)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("Failed to restore data"))
    }

    @Test
    fun `invoke returns error when input stream is null`() = runTest {
        // Given
        val inputUri = mockk<Uri>()
        every { context.contentResolver.openInputStream(inputUri) } returns null

        // When
        val result = useCase(inputUri)

        // Then
        assertTrue(result is Result.Error)
        assertEquals("Failed to read backup file", (result as Result.Error).message)
    }

    @Test
    fun `invoke handles empty quotes array`() = runTest {
        // Given
        val emptyBackupJson = """
            {
              "version": "1.0",
              "quotes": [],
              "journalEntriesMetadata": []
            }
        """.trimIndent()
        val inputUri = mockk<Uri>()
        val inputStream = ByteArrayInputStream(emptyBackupJson.toByteArray())
        every { contentResolver.openInputStream(any()) } returns inputStream

        // When
        val result = useCase(inputUri)

        // Then
        assertTrue(result is Result.Success)
        val summary = (result as Result.Success).data
        assertEquals(0, summary.quotesImported)
        assertEquals(0, summary.quotesSkipped)
        assertEquals(0, summary.quotesReplaced)
    }

    @Test
    fun `invoke properly handles escaped JSON characters`() = runTest {
        // Given
        val backupWithEscapedChars = """
            {
              "version": "1.0",
              "quotes": [
                {
                  "id": "quote-1",
                  "author": "Author \"Nickname\" Name",
                  "text": "Text with\n\"quotes\"\nand\\backslash",
                  "source": null,
                  "categories": [],
                  "tags": [],
                  "mood": null,
                  "isFavorite": false,
                  "createdAt": "2024-01-15T12:00:00.000Z"
                }
              ]
            }
        """.trimIndent()
        val inputUri = mockk<Uri>()
        val inputStream = ByteArrayInputStream(backupWithEscapedChars.toByteArray())
        every { contentResolver.openInputStream(any()) } returns inputStream

        coEvery { quotesRepository.getQuoteByIdResult(any()) } returns Result.Success(null)
        coEvery { quotesRepository.addQuoteResult(any()) } returns Result.Success(Unit)

        // When
        val result = useCase(inputUri)

        // Then
        assertTrue(result is Result.Success)

        coVerify {
            quotesRepository.addQuoteResult(match { quote ->
                quote.author.contains("\"Nickname\"") &&
                quote.text.contains("\"quotes\"") &&
                quote.text.contains("\\")
            })
        }
    }

    @Test
    fun `RestoreSummary getSummaryText formats properly`() {
        // Given
        val summary = RestoreSummary(
            quotesImported = 5,
            quotesSkipped = 2,
            quotesReplaced = 0,
            journalEntriesNote = "Journal content cannot be restored due to encryption"
        )

        // When
        val text = summary.getSummaryText()

        // Then
        assertTrue(text.contains("Restore completed:"))
        assertTrue(text.contains("Imported: 5"))
        assertTrue(text.contains("Skipped: 2"))
        assertTrue(text.contains("Total: 7"))
        assertTrue(text.contains("Note: Journal content cannot be restored"))
    }
}
