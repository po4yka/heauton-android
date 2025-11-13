package com.po4yka.heauton.domain.usecase.data

import android.content.Context
import android.net.Uri
import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

/**
 * Use case for restoring app data from a backup JSON file.
 *
 * Imports:
 * - Quotes (with merge strategy to avoid duplicates)
 * - Journal entries metadata (content cannot be restored due to encryption)
 */
class RestoreDataUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val quotesRepository: QuotesRepository
) {
    /**
     * Restore strategy for handling existing data.
     */
    enum class RestoreStrategy {
        /**
         * Skip items that already exist (by ID).
         */
        SKIP_EXISTING,

        /**
         * Replace existing items with backup data.
         */
        REPLACE_EXISTING,

        /**
         * Import all items as new (generate new IDs).
         */
        IMPORT_AS_NEW
    }

    /**
     * Restores data from a backup file.
     *
     * @param inputUri URI of the backup file to read
     * @param strategy Strategy for handling existing data
     * @return Result with summary of restored data
     */
    suspend operator fun invoke(
        inputUri: Uri,
        strategy: RestoreStrategy = RestoreStrategy.SKIP_EXISTING
    ): Result<RestoreSummary> {
        return try {
            // Read backup file
            val backupJson = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: return@withContext null
            } ?: return Result.Error("Failed to read backup file")

            // Parse JSON
            val json = JSONObject(backupJson)

            // Verify version
            val version = json.optString("version", "unknown")
            if (version != "1.0") {
                return Result.Error("Unsupported backup version: $version")
            }

            var quotesImported = 0
            var quotesSkipped = 0
            var quotesReplaced = 0

            // Restore quotes
            val quotesArray = json.optJSONArray("quotes")
            if (quotesArray != null) {
                for (i in 0 until quotesArray.length()) {
                    val quoteJson = quotesArray.getJSONObject(i)

                    val quote = Quote(
                        id = quoteJson.getString("id"),
                        author = quoteJson.getString("author"),
                        text = quoteJson.getString("text"),
                        source = quoteJson.optString("source").takeIf { it.isNotBlank() },
                        categories = parseJsonArray(quoteJson.optJSONArray("categories")),
                        tags = parseJsonArray(quoteJson.optJSONArray("tags")),
                        mood = quoteJson.optString("mood").takeIf { it.isNotBlank() },
                        isFavorite = quoteJson.optBoolean("isFavorite", false),
                        readCount = 0,
                        lastReadAt = null,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = null,
                        textFilePath = null,
                        isChunked = false,
                        wordCount = quoteJson.getString("text").split("\\s+".toRegex()).size
                    )

                    when (strategy) {
                        RestoreStrategy.SKIP_EXISTING -> {
                            // Check if quote exists
                            val exists = when (val result = quotesRepository.getQuoteByIdResult(quote.id)) {
                                is Result.Success -> result.data != null
                                is Result.Error -> false
                            }

                            if (!exists) {
                                quotesRepository.addQuoteResult(quote)
                                quotesImported++
                            } else {
                                quotesSkipped++
                            }
                        }

                        RestoreStrategy.REPLACE_EXISTING -> {
                            // Delete if exists, then add
                            quotesRepository.deleteQuoteResult(quote.id)
                            quotesRepository.addQuoteResult(quote)
                            quotesReplaced++
                        }

                        RestoreStrategy.IMPORT_AS_NEW -> {
                            // Generate new ID
                            val newQuote = quote.copy(id = java.util.UUID.randomUUID().toString())
                            quotesRepository.addQuoteResult(newQuote)
                            quotesImported++
                        }
                    }
                }
            }

            val summary = RestoreSummary(
                quotesImported = quotesImported,
                quotesSkipped = quotesSkipped,
                quotesReplaced = quotesReplaced,
                journalEntriesNote = "Journal content cannot be restored due to encryption"
            )

            Result.Success(summary)
        } catch (e: Exception) {
            Result.Error("Failed to restore data: ${e.message}")
        }
    }

    /**
     * Parses a JSON array to a list of strings.
     */
    private fun parseJsonArray(jsonArray: org.json.JSONArray?): List<String>? {
        if (jsonArray == null || jsonArray.length() == 0) return null

        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }
}

/**
 * Summary of restored data.
 */
data class RestoreSummary(
    val quotesImported: Int,
    val quotesSkipped: Int,
    val quotesReplaced: Int,
    val journalEntriesNote: String
) {
    val totalQuotes: Int
        get() = quotesImported + quotesSkipped + quotesReplaced

    fun getSummaryText(): String {
        return buildString {
            append("Restore completed:\n\n")
            append("Quotes:\n")
            append("  • Imported: $quotesImported\n")
            if (quotesSkipped > 0) {
                append("  • Skipped: $quotesSkipped\n")
            }
            if (quotesReplaced > 0) {
                append("  • Replaced: $quotesReplaced\n")
            }
            append("  • Total: $totalQuotes\n\n")
            append("Note: $journalEntriesNote")
        }
    }
}
