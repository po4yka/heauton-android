package com.po4yka.heauton.domain.model

/**
 * Represents a search result with relevance scoring.
 */
data class SearchResult(
    val quote: Quote,
    val relevanceScore: Double,
    val matchedFields: List<MatchedField> = emptyList()
)

/**
 * Indicates which fields matched the search query.
 */
enum class MatchedField {
    TEXT,
    AUTHOR,
    SOURCE,
    TAGS,
    CATEGORIES
}
