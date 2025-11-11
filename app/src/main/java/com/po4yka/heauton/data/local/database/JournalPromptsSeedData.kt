package com.po4yka.heauton.data.local.database

import com.po4yka.heauton.data.local.database.entities.JournalPromptEntity
import com.po4yka.heauton.data.local.database.entities.PromptCategory
import com.po4yka.heauton.data.local.database.entities.PromptDifficulty
import java.util.UUID

/**
 * Seed data for journal prompts.
 * Contains 60+ prompts across 8 categories with varying difficulty levels.
 */
object JournalPromptsSeedData {

    fun getPrompts(): List<JournalPromptEntity> = listOf(
        // ========== Self-Reflection (10 prompts) ==========
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What are three things you learned about yourself today?",
            category = PromptCategory.SELF_REFLECTION,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("daily", "learning")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "If you could change one decision you made this week, what would it be and why?",
            category = PromptCategory.SELF_REFLECTION,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("decisions", "regret")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What does authenticity mean to you? Are you living authentically?",
            category = PromptCategory.SELF_REFLECTION,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("identity", "authenticity")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Describe a moment when you felt most like yourself.",
            category = PromptCategory.SELF_REFLECTION,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("identity", "memory")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What patterns do you notice in your behavior when you're stressed?",
            category = PromptCategory.SELF_REFLECTION,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("stress", "patterns")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "How have your values changed over the past five years?",
            category = PromptCategory.SELF_REFLECTION,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("values", "growth")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What would you tell your younger self if you could send a message?",
            category = PromptCategory.SELF_REFLECTION,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("past", "advice")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What aspects of your personality do you hide from others? Why?",
            category = PromptCategory.SELF_REFLECTION,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("authenticity", "vulnerability")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "When do you feel most confident? What contributes to that feeling?",
            category = PromptCategory.SELF_REFLECTION,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("confidence", "self-esteem")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What limiting beliefs hold you back, and where did they come from?",
            category = PromptCategory.SELF_REFLECTION,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("beliefs", "psychology")
        ),

        // ========== Gratitude (10 prompts) ==========
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "List five small things that brought you joy today.",
            category = PromptCategory.GRATITUDE,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("daily", "joy")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Who is someone that made a positive impact on your life recently? How can you thank them?",
            category = PromptCategory.GRATITUDE,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("relationships", "appreciation")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What challenge are you grateful for, and what did it teach you?",
            category = PromptCategory.GRATITUDE,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("challenges", "growth")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Describe a place that brings you peace. Why are you grateful for it?",
            category = PromptCategory.GRATITUDE,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("place", "peace")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What ability or skill do you take for granted that you're grateful to have?",
            category = PromptCategory.GRATITUDE,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("skills", "abilities")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Reflect on a failure that ultimately led to something positive.",
            category = PromptCategory.GRATITUDE,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("failure", "silver lining")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What part of your daily routine are you most thankful for?",
            category = PromptCategory.GRATITUDE,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("routine", "daily")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Write a gratitude letter to yourself acknowledging your efforts and progress.",
            category = PromptCategory.GRATITUDE,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("self-compassion", "progress")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What difficult lesson are you grateful to have learned?",
            category = PromptCategory.GRATITUDE,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("lessons", "wisdom")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What sensory experiences (sounds, smells, textures) bring you comfort?",
            category = PromptCategory.GRATITUDE,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("senses", "comfort")
        ),

        // ========== Goals (10 prompts) ==========
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What is one small goal you can accomplish this week?",
            category = PromptCategory.GOALS,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("planning", "short-term")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Where do you see yourself in five years? What steps can you take now to get there?",
            category = PromptCategory.GOALS,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("future", "planning")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "If you could master one skill this year, what would it be and why?",
            category = PromptCategory.GOALS,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("skills", "learning")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What goal have you been avoiding? What's holding you back?",
            category = PromptCategory.GOALS,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("avoidance", "fear")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Describe your ideal day five years from now in vivid detail.",
            category = PromptCategory.GOALS,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("visualization", "future")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What does success mean to you? Has this definition changed over time?",
            category = PromptCategory.GOALS,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("success", "values")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What habit would you like to develop? What's your first step?",
            category = PromptCategory.GOALS,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("habits", "development")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Review last month's goals. What progress did you make? What obstacles did you face?",
            category = PromptCategory.GOALS,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("review", "progress")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What would you attempt if you knew you couldn't fail?",
            category = PromptCategory.GOALS,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("dreams", "fear")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What legacy do you want to leave? How does this shape your current priorities?",
            category = PromptCategory.GOALS,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("legacy", "purpose")
        ),

        // ========== Emotions (10 prompts) ==========
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What emotion dominated your day? Explore why you felt this way.",
            category = PromptCategory.EMOTIONS,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("daily", "awareness")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Describe a recent moment when you felt overwhelmed. What triggered it?",
            category = PromptCategory.EMOTIONS,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("stress", "triggers")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What emotion are you most uncomfortable feeling? Why do you think that is?",
            category = PromptCategory.EMOTIONS,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("discomfort", "psychology")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Write about a time you felt truly at peace. What created that feeling?",
            category = PromptCategory.EMOTIONS,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("peace", "memory")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "How do you typically respond to anger? Is this response serving you?",
            category = PromptCategory.EMOTIONS,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("anger", "coping")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "When was the last time you cried? What was behind those tears?",
            category = PromptCategory.EMOTIONS,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("sadness", "release")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What makes you feel most alive and energized?",
            category = PromptCategory.EMOTIONS,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("joy", "energy")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Explore a complex emotion you're experiencing that you can't quite name.",
            category = PromptCategory.EMOTIONS,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("complexity", "introspection")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What soothes you when you're feeling anxious or worried?",
            category = PromptCategory.EMOTIONS,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("anxiety", "coping")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "How do your emotions influence your decision-making? Is this helpful or harmful?",
            category = PromptCategory.EMOTIONS,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("decisions", "awareness")
        ),

        // ========== Creativity (8 prompts) ==========
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "If your life was a book, what would this chapter be titled?",
            category = PromptCategory.CREATIVITY,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("metaphor", "story")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Write a dialogue between your present self and your future self ten years from now.",
            category = PromptCategory.CREATIVITY,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("future", "dialogue")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Describe your perfect day using only sensory details (no events, just experiences).",
            category = PromptCategory.CREATIVITY,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("senses", "imagination")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "If you could have dinner with anyone, living or dead, who would it be? What would you talk about?",
            category = PromptCategory.CREATIVITY,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("imagination", "conversation")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Create a metaphor for your current emotional state.",
            category = PromptCategory.CREATIVITY,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("metaphor", "emotions")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Write a letter to a place that holds special meaning for you.",
            category = PromptCategory.CREATIVITY,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("place", "memory")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "If your mood had a color, texture, and sound right now, what would they be?",
            category = PromptCategory.CREATIVITY,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("synesthesia", "mood")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Imagine your ideal future. Now work backward—what path led you there?",
            category = PromptCategory.CREATIVITY,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("future", "planning")
        ),

        // ========== Daily (6 prompts) ==========
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What was the highlight of your day?",
            category = PromptCategory.DAILY,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("reflection", "positive")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What challenged you today, and how did you respond?",
            category = PromptCategory.DAILY,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("challenge", "coping")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Describe an interaction you had today. What did you learn from it?",
            category = PromptCategory.DAILY,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("interaction", "learning")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What are you looking forward to tomorrow?",
            category = PromptCategory.DAILY,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("anticipation", "planning")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "How did you take care of yourself today?",
            category = PromptCategory.DAILY,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("self-care", "wellness")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What thought kept coming back to you today? Why do you think that is?",
            category = PromptCategory.DAILY,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("thoughts", "awareness")
        ),

        // ========== Relationships (4 prompts) ==========
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Who in your life makes you feel most understood?",
            category = PromptCategory.RELATIONSHIPS,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("connection", "understanding")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "Describe a relationship that has changed significantly. How has this affected you?",
            category = PromptCategory.RELATIONSHIPS,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("change", "impact")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What patterns do you notice in your relationships? Are they healthy?",
            category = PromptCategory.RELATIONSHIPS,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("patterns", "health")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "How can you be a better friend/partner/family member this week?",
            category = PromptCategory.RELATIONSHIPS,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("improvement", "action")
        ),

        // ========== Growth (4 prompts) ==========
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What new perspective have you gained recently?",
            category = PromptCategory.GROWTH,
            difficulty = PromptDifficulty.BEGINNER,
            tags = listOf("learning", "perspective")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What area of your life needs the most attention right now?",
            category = PromptCategory.GROWTH,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("priorities", "awareness")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "How have you grown in the past year? What evidence do you have of this growth?",
            category = PromptCategory.GROWTH,
            difficulty = PromptDifficulty.ADVANCED,
            tags = listOf("progress", "reflection")
        ),
        JournalPromptEntity(
            id = UUID.randomUUID().toString(),
            text = "What feedback have you received recently? How can you use it constructively?",
            category = PromptCategory.GROWTH,
            difficulty = PromptDifficulty.INTERMEDIATE,
            tags = listOf("feedback", "improvement")
        )
    )

    /**
     * Get prompts by category for easier filtering.
     */
    fun getPromptsByCategory(category: String): List<JournalPromptEntity> {
        return getPrompts().filter { it.category == category }
    }

    /**
     * Get total count of prompts.
     */
    fun getPromptCount(): Int = getPrompts().size
}
