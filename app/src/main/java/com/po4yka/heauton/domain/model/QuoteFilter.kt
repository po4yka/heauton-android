package com.po4yka.heauton.domain.model

/**
 * Filter criteria for querying quotes.
 */
data class QuoteFilter(
    val searchQuery: String? = null,
    val author: String? = null,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val mood: String? = null,
    val onlyFavorites: Boolean = false,
    val sortBy: SortOption = SortOption.CREATED_DESC
)

/**
 * Sort options for quotes.
 */
enum class SortOption {
    CREATED_DESC,
    CREATED_ASC,
    AUTHOR_ASC,
    AUTHOR_DESC,
    READ_COUNT_DESC
}
