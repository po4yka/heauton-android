package com.po4yka.heauton.data.local.search

import com.po4yka.heauton.data.local.database.entities.QuoteEntity
import kotlin.math.ln

/**
 * Implements BM25-like ranking algorithm for search results.
 * Ranks search results by relevance to the query.
 */
object SearchRanker {

    // BM25 parameters
    private const val K1 = 1.2 // Term frequency saturation parameter
    private const val B = 0.75  // Length normalization parameter

    /**
     * Data class for search results with scoring.
     */
    data class ScoredQuote(
        val quote: QuoteEntity,
        val score: Double
    )

    /**
     * Ranks a list of quotes based on query relevance.
     *
     * @param quotes List of quotes to rank
     * @param query The search query
     * @param avgDocLength Average document length in the corpus
     * @return List of quotes sorted by relevance score (highest first)
     */
    fun rankResults(
        quotes: List<QuoteEntity>,
        query: String,
        avgDocLength: Double = 50.0 // Default average quote length
    ): List<QuoteEntity> {
        if (quotes.isEmpty() || query.isBlank()) {
            return quotes
        }

        val queryTokens = TextNormalizer.extractTokens(query)
        val totalDocs = quotes.size

        // Calculate IDF for each query term
        val idfScores = calculateIDF(quotes, queryTokens)

        // Score each document
        val scored = quotes.map { quote ->
            val score = calculateBM25Score(
                quote = quote,
                queryTokens = queryTokens,
                idfScores = idfScores,
                avgDocLength = avgDocLength,
                totalDocs = totalDocs
            )
            ScoredQuote(quote, score)
        }

        // Sort by score descending
        return scored
            .sortedByDescending { it.score }
            .map { it.quote }
    }

    /**
     * Calculates IDF (Inverse Document Frequency) scores for query terms.
     */
    private fun calculateIDF(
        documents: List<QuoteEntity>,
        queryTokens: List<String>
    ): Map<String, Double> {
        val totalDocs = documents.size.toDouble()
        val idfScores = mutableMapOf<String, Double>()

        for (token in queryTokens) {
            val docsContainingTerm = documents.count { doc ->
                val docText = TextNormalizer.normalize("${doc.text} ${doc.author}")
                docText.contains(token, ignoreCase = true)
            }

            // IDF = ln((N - n + 0.5) / (n + 0.5) + 1)
            // Where N = total docs, n = docs containing term
            val idf = ln((totalDocs - docsContainingTerm + 0.5) / (docsContainingTerm + 0.5) + 1)
            idfScores[token] = idf
        }

        return idfScores
    }

    /**
     * Calculates BM25 score for a single document.
     */
    private fun calculateBM25Score(
        quote: QuoteEntity,
        queryTokens: List<String>,
        idfScores: Map<String, Double>,
        avgDocLength: Double,
        totalDocs: Int
    ): Double {
        val docText = TextNormalizer.normalize("${quote.text} ${quote.author}")
        val docTokens = TextNormalizer.extractTokens(docText)
        val docLength = docTokens.size.toDouble()

        var score = 0.0

        for (queryToken in queryTokens) {
            // Term frequency in document
            val tf = docTokens.count { it == queryToken }.toDouble()

            if (tf == 0.0) continue

            val idf = idfScores[queryToken] ?: 0.0

            // BM25 formula
            val numerator = tf * (K1 + 1)
            val denominator = tf + K1 * (1 - B + B * (docLength / avgDocLength))

            score += idf * (numerator / denominator)
        }

        // Boost score if it's a favorite
        if (quote.isFavorite) {
            score *= 1.2
        }

        // Boost score based on read count (popular quotes)
        if (quote.readCount > 0) {
            score *= (1.0 + (quote.readCount * 0.01))
        }

        return score
    }

    /**
     * Simple relevance scorer based on exact matches and position.
     * Lighter alternative to full BM25.
     */
    fun simpleScore(quote: QuoteEntity, query: String): Double {
        val normalizedQuery = TextNormalizer.normalize(query)
        val normalizedText = TextNormalizer.normalize(quote.text)
        val normalizedAuthor = TextNormalizer.normalize(quote.author)

        var score = 0.0

        // Exact text match (highest priority)
        if (normalizedText.contains(normalizedQuery)) {
            score += 100.0

            // Bonus for match at beginning
            if (normalizedText.startsWith(normalizedQuery)) {
                score += 50.0
            }
        }

        // Author match (medium priority)
        if (normalizedAuthor.contains(normalizedQuery)) {
            score += 75.0
        }

        // Token matching (lower priority)
        val queryTokens = TextNormalizer.extractTokens(normalizedQuery)
        val textTokens = TextNormalizer.extractTokens(normalizedText)

        val matchingTokens = queryTokens.count { queryToken ->
            textTokens.any { it.startsWith(queryToken) }
        }

        score += matchingTokens * 10.0

        // Favorite bonus
        if (quote.isFavorite) {
            score *= 1.2
        }

        return score
    }
}
