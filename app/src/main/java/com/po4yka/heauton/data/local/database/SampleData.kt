package com.po4yka.heauton.data.local.database

import com.po4yka.heauton.data.local.database.entities.QuoteEntity

/**
 * Sample quote data for seeding the database.
 */
object SampleData {

    /**
     * 12 philosophical quotes to seed the database on first launch.
     */
    val sampleQuotes = listOf(
        QuoteEntity(
            author = "Marcus Aurelius",
            text = "You have power over your mind - not outside events. Realize this, and you will find strength.",
            source = "Meditations",
            categories = listOf("Stoicism", "Philosophy"),
            tags = listOf("control", "strength", "mindfulness"),
            mood = "reflective",
            wordCount = 16
        ),
        QuoteEntity(
            author = "Epictetus",
            text = "It's not what happens to you, but how you react to it that matters.",
            source = "Enchiridion",
            categories = listOf("Stoicism", "Philosophy"),
            tags = listOf("control", "response", "wisdom"),
            mood = "motivating",
            wordCount = 13
        ),
        QuoteEntity(
            author = "Seneca",
            text = "We suffer more often in imagination than in reality.",
            source = "Letters from a Stoic",
            categories = listOf("Stoicism", "Philosophy"),
            tags = listOf("anxiety", "imagination", "reality"),
            mood = "reflective",
            wordCount = 9
        ),
        QuoteEntity(
            author = "Buddha",
            text = "Peace comes from within. Do not seek it without.",
            source = "Dhammapada",
            categories = listOf("Buddhism", "Wisdom"),
            tags = listOf("peace", "inner", "seeking"),
            mood = "peaceful",
            wordCount = 9
        ),
        QuoteEntity(
            author = "Lao Tzu",
            text = "The journey of a thousand miles begins with one step.",
            source = "Tao Te Ching",
            categories = listOf("Taoism", "Wisdom"),
            tags = listOf("journey", "beginning", "action"),
            mood = "motivating",
            wordCount = 10
        ),
        QuoteEntity(
            author = "Rumi",
            text = "The wound is the place where the Light enters you.",
            source = null,
            categories = listOf("Sufism", "Poetry"),
            tags = listOf("healing", "light", "transformation"),
            mood = "reflective",
            wordCount = 10
        ),
        QuoteEntity(
            author = "Viktor Frankl",
            text = "When we are no longer able to change a situation, we are challenged to change ourselves.",
            source = "Man's Search for Meaning",
            categories = listOf("Psychology", "Philosophy"),
            tags = listOf("change", "adaptation", "growth"),
            mood = "motivating",
            wordCount = 16
        ),
        QuoteEntity(
            author = "Alan Watts",
            text = "You are under no obligation to be the same person you were five minutes ago.",
            source = null,
            categories = listOf("Philosophy", "Zen"),
            tags = listOf("change", "identity", "freedom"),
            mood = "liberating",
            wordCount = 15
        ),
        QuoteEntity(
            author = "Thich Nhat Hanh",
            text = "Breathing in, I calm my body. Breathing out, I smile.",
            source = "Being Peace",
            categories = listOf("Buddhism", "Mindfulness"),
            tags = listOf("breathing", "calm", "mindfulness"),
            mood = "peaceful",
            wordCount = 10
        ),
        QuoteEntity(
            author = "Carl Jung",
            text = "Until you make the unconscious conscious, it will direct your life and you will call it fate.",
            source = null,
            categories = listOf("Psychology", "Philosophy"),
            tags = listOf("consciousness", "awareness", "fate"),
            mood = "reflective",
            wordCount = 16
        ),
        QuoteEntity(
            author = "Heraclitus",
            text = "No man ever steps in the same river twice, for it's not the same river and he's not the same man.",
            source = null,
            categories = listOf("Philosophy", "Ancient Greek"),
            tags = listOf("change", "flow", "impermanence"),
            mood = "reflective",
            wordCount = 19
        ),
        QuoteEntity(
            author = "Socrates",
            text = "The unexamined life is not worth living.",
            source = "Apology",
            categories = listOf("Philosophy", "Ancient Greek"),
            tags = listOf("self-examination", "wisdom", "meaning"),
            mood = "reflective",
            wordCount = 7
        )
    )
}
