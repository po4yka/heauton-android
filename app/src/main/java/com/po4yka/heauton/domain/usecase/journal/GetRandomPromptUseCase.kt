package com.po4yka.heauton.domain.usecase.journal

import com.po4yka.heauton.domain.model.JournalPrompt
import com.po4yka.heauton.domain.repository.JournalRepository
import javax.inject.Inject

/**
 * Use case to get a random journal prompt.
 *
 * ## Features:
 * - Returns random prompt from all prompts
 * - Can filter by category
 * - Increments usage count when used
 */
class GetRandomPromptUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    /**
     * Get a random prompt, optionally filtered by category.
     *
     * @param category Optional category filter
     * @param incrementUsage Whether to increment usage count (default: true)
     * @return Random prompt, or null if none available
     */
    suspend operator fun invoke(
        category: String? = null,
        incrementUsage: Boolean = true
    ): JournalPrompt? {
        val prompt = if (category != null) {
            repository.getRandomPromptByCategory(category)
        } else {
            repository.getRandomPrompt()
        }

        // Increment usage count if requested
        if (incrementUsage && prompt != null) {
            repository.incrementPromptUsage(prompt.id)
        }

        return prompt
    }
}
