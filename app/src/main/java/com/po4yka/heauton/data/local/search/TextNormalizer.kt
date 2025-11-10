package com.po4yka.heauton.data.local.search

import java.text.Normalizer
import java.util.Locale

/**
 * Utility for normalizing text for search operations.
 * Handles Unicode normalization, diacritic removal, and tokenization.
 */
object TextNormalizer {

    /**
     * Normalizes text for search by:
     * - Converting to lowercase
     * - Applying NFKC normalization
     * - Removing diacritics
     * - Trimming whitespace
     *
     * @param text The text to normalize
     * @return The normalized text
     */
    fun normalize(text: String): String {
        // Convert to lowercase
        var normalized = text.lowercase(Locale.getDefault())

        // Apply NFKC normalization (compatibility decomposition followed by canonical composition)
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKC)

        // Remove diacritics
        normalized = removeDiacritics(normalized)

        // Trim and collapse multiple spaces
        normalized = normalized.trim().replace(Regex("\\s+"), " ")

        return normalized
    }

    /**
     * Removes diacritical marks from text.
     * Converts characters like é, ñ, ü to e, n, u.
     *
     * @param text The text with potential diacritics
     * @return Text without diacritics
     */
    fun removeDiacritics(text: String): String {
        // Decompose characters into base + diacritical marks
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)

        // Remove all diacritical marks (Unicode category Mn = Nonspacing_Mark)
        return normalized.replace(Regex("\\p{Mn}"), "")
    }

    /**
     * Extracts tokens (words) from text for indexing.
     * Removes punctuation and splits on whitespace.
     *
     * @param text The text to tokenize
     * @return List of normalized tokens
     */
    fun extractTokens(text: String): List<String> {
        // Normalize the text first
        val normalized = normalize(text)

        // Remove punctuation and split on whitespace
        return normalized
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }

    /**
     * Prepares a search query for FTS by normalizing and adding wildcards if needed.
     *
     * @param query The raw search query
     * @return The prepared FTS query
     */
    fun prepareSearchQuery(query: String): String {
        val normalized = normalize(query)

        // If query is too short, return as-is
        if (normalized.length < 2) {
            return normalized
        }

        // Split into tokens and add wildcards for prefix matching
        val tokens = extractTokens(normalized)

        return tokens.joinToString(" ") { token ->
            "$token*"
        }
    }

    /**
     * Calculates a simple word count for text.
     *
     * @param text The text to count words in
     * @return The number of words
     */
    fun wordCount(text: String): Int {
        return extractTokens(text).size
    }
}
