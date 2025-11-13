package com.po4yka.heauton.domain.usecase.data

import android.content.Context
import android.net.Uri
import com.po4yka.heauton.domain.model.JournalEntry
import com.po4yka.heauton.domain.model.Mood
import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.JournalRepository
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.domain.repository.ScheduleRepository
import com.po4yka.heauton.util.Result
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream

class BackupDataUseCaseTest {

    private lateinit var context: Context
    private lateinit var quotesRepository: QuotesRepository
    private lateinit var journalRepository: JournalRepository
    private lateinit var scheduleRepository: ScheduleRepository
    private lateinit var useCase: BackupDataUseCase

    private val testQuote = Quote(
        id = "quote-1",
        author = "Marcus Aurelius",
        text = "You have power over your mind.",
        source = "Meditations",
        categories = listOf("Philosophy"),
        tags = listOf("stoicism"),
        mood = "MOTIVATED",
        isFavorite = true,
        readCount = 5,
        lastReadAt = System.currentTimeMillis(),
        createdAt = System.currentTimeMillis(),
        updatedAt = null,
        textFilePath = null,
        isChunked = false,
        wordCount = 6
    )

    private val testEntry = JournalEntry(
        id = "entry-1",
        title = "My Entry",
        content = "Journal content",
        createdAt = System.currentTimeMillis(),
        updatedAt = null,
        mood = Mood.HAPPY,
        tags = listOf("personal"),
        isFavorite = false,
        isPinned = false,
        wordCount = 2
    )

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        quotesRepository = mockk()
        journalRepository = mockk()
        scheduleRepository = mockk()
        useCase = BackupDataUseCase(context, quotesRepository, journalRepository, scheduleRepository)
    }

    @Test
    fun `invoke creates backup with all data successfully`() = runTest {
        // Given
        val outputUri = mockk<Uri>()
        val outputStream = ByteArrayOutputStream()
        every { context.contentResolver.openOutputStream(outputUri) } returns outputStream
        coEvery { quotesRepository.getAllQuotesOneShot() } returns Result.Success(listOf(testQuote))
        coEvery { journalRepository.getAllEntriesOneShot() } returns Result.Success(listOf(testEntry))
        coEvery { scheduleRepository.getAllSchedules() } returns Result.Success(emptyList())

        // When
        val result = useCase(outputUri)

        // Then
        assertTrue(result is Result.Success)
        val output = outputStream.toString()

        // Verify backup structure
        assertTrue(output.contains("\"version\": \"1.0\""))
        assertTrue(output.contains("\"exportDate\""))
        assertTrue(output.contains("\"appName\": \"Heauton\""))
        assertTrue(output.contains("\"platform\": \"Android\""))

        // Verify counts
        assertTrue(output.contains("\"counts\""))
        assertTrue(output.contains("\"quotes\": 1"))
        assertTrue(output.contains("\"journalEntries\": 1"))

        // Verify quote data
        assertTrue(output.contains("\"quotes\""))
        assertTrue(output.contains("\"id\": \"quote-1\""))
        assertTrue(output.contains("\"author\": \"Marcus Aurelius\""))
        assertTrue(output.contains("\"text\": \"You have power over your mind.\""))
        assertTrue(output.contains("\"isFavorite\": true"))

        // Verify journal metadata (not content for privacy)
        assertTrue(output.contains("\"journalEntriesMetadata\""))
        assertTrue(output.contains("\"id\": \"entry-1\""))
        assertTrue(output.contains("\"title\": \"My Entry\""))
        assertFalse(output.contains("Journal content")) // Content excluded
    }

    @Test
    fun `invoke properly escapes JSON special characters`() = runTest {
        // Given
        val quoteWithSpecialChars = testQuote.copy(
            text = "Text with \"quotes\" and \nnewlines and \ttabs and \\ backslash",
            author = "Author \"Nickname\" Name"
        )
        val outputUri = mockk<Uri>()
        val outputStream = ByteArrayOutputStream()
        every { context.contentResolver.openOutputStream(outputUri) } returns outputStream
        coEvery { quotesRepository.getAllQuotesOneShot() } returns Result.Success(listOf(quoteWithSpecialChars))
        coEvery { journalRepository.getAllEntriesOneShot() } returns Result.Success(emptyList())
        coEvery { scheduleRepository.getAllSchedules() } returns Result.Success(emptyList())

        // When
        val result = useCase(outputUri)

        // Then
        assertTrue(result is Result.Success)
        val output = outputStream.toString()
        assertTrue(output.contains("\\\"")) // Escaped quotes
        assertTrue(output.contains("\\n")) // Escaped newlines
        assertTrue(output.contains("\\t")) // Escaped tabs
        assertTrue(output.contains("\\\\")) // Escaped backslash
    }

    @Test
    fun `invoke handles empty data collections`() = runTest {
        // Given
        val outputUri = mockk<Uri>()
        val outputStream = ByteArrayOutputStream()
        every { context.contentResolver.openOutputStream(outputUri) } returns outputStream
        coEvery { quotesRepository.getAllQuotesOneShot() } returns Result.Success(emptyList())
        coEvery { journalRepository.getAllEntriesOneShot() } returns Result.Success(emptyList())
        coEvery { scheduleRepository.getAllSchedules() } returns Result.Success(emptyList())

        // When
        val result = useCase(outputUri)

        // Then
        assertTrue(result is Result.Success)
        val output = outputStream.toString()
        assertTrue(output.contains("\"quotes\": 0"))
        assertTrue(output.contains("\"journalEntries\": 0"))
        assertTrue(output.contains("\"quotes\": []"))
        assertTrue(output.contains("\"journalEntriesMetadata\": []"))
    }

    @Test
    fun `invoke handles quotes with null optional fields`() = runTest {
        // Given
        val quoteWithNulls = testQuote.copy(
            source = null,
            categories = null,
            tags = null,
            mood = null
        )
        val outputUri = mockk<Uri>()
        val outputStream = ByteArrayOutputStream()
        every { context.contentResolver.openOutputStream(outputUri) } returns outputStream
        coEvery { quotesRepository.getAllQuotesOneShot() } returns Result.Success(listOf(quoteWithNulls))
        coEvery { journalRepository.getAllEntriesOneShot() } returns Result.Success(emptyList())
        coEvery { scheduleRepository.getAllSchedules() } returns Result.Success(emptyList())

        // When
        val result = useCase(outputUri)

        // Then
        assertTrue(result is Result.Success)
        val output = outputStream.toString()
        assertTrue(output.contains("\"source\": null"))
        assertTrue(output.contains("\"categories\": []"))
        assertTrue(output.contains("\"tags\": []"))
        assertTrue(output.contains("\"mood\": null"))
    }

    @Test
    fun `invoke returns error when quotes repository fails`() = runTest {
        // Given
        val outputUri = mockk<Uri>()
        val errorMessage = "Database error"
        coEvery { quotesRepository.getAllQuotesOneShot() } returns Result.Error(errorMessage)

        // When
        val result = useCase(outputUri)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains(errorMessage))
    }

    @Test
    fun `invoke returns error when journal repository fails`() = runTest {
        // Given
        val outputUri = mockk<Uri>()
        val errorMessage = "Database error"
        coEvery { quotesRepository.getAllQuotesOneShot() } returns Result.Success(emptyList())
        coEvery { journalRepository.getAllEntriesOneShot() } returns Result.Error(errorMessage)

        // When
        val result = useCase(outputUri)

        // Then
        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains(errorMessage))
    }

    @Test
    fun `invoke returns error when output stream is null`() = runTest {
        // Given
        val outputUri = mockk<Uri>()
        every { context.contentResolver.openOutputStream(outputUri) } returns null
        coEvery { quotesRepository.getAllQuotesOneShot() } returns Result.Success(emptyList())
        coEvery { journalRepository.getAllEntriesOneShot() } returns Result.Success(emptyList())
        coEvery { scheduleRepository.getAllSchedules() } returns Result.Success(emptyList())

        // When
        val result = useCase(outputUri)

        // Then
        assertTrue(result is Result.Error)
    }

    @Test
    fun `invoke handles multiple quotes with proper comma separation`() = runTest {
        // Given
        val quote1 = testQuote.copy(id = "quote-1")
        val quote2 = testQuote.copy(id = "quote-2")
        val quote3 = testQuote.copy(id = "quote-3")
        val outputUri = mockk<Uri>()
        val outputStream = ByteArrayOutputStream()
        every { context.contentResolver.openOutputStream(outputUri) } returns outputStream
        coEvery { quotesRepository.getAllQuotesOneShot() } returns Result.Success(listOf(quote1, quote2, quote3))
        coEvery { journalRepository.getAllEntriesOneShot() } returns Result.Success(emptyList())
        coEvery { scheduleRepository.getAllSchedules() } returns Result.Success(emptyList())

        // When
        val result = useCase(outputUri)

        // Then
        assertTrue(result is Result.Success)
        val output = outputStream.toString()
        assertTrue(output.contains("\"id\": \"quote-1\""))
        assertTrue(output.contains("\"id\": \"quote-2\""))
        assertTrue(output.contains("\"id\": \"quote-3\""))
        // Verify valid JSON structure
        assertTrue(output.matches(Regex(".*\"quotes\":\\s*\\[.*].*", RegexOption.DOT_MATCHES_ALL)))
    }
}
