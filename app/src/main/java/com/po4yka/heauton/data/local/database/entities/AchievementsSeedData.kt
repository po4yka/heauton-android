package com.po4yka.heauton.data.local.database.entities

/**
 * Seed data for achievements.
 *
 * Provides 25+ curated achievements across all categories.
 */
object AchievementsSeedData {

    fun getAchievements(): List<AchievementEntity> = listOf(
        // GENERAL Achievements
        AchievementEntity(
            id = "ach_first_launch",
            title = "Welcome Aboard",
            description = "Launch Heauton for the first time",
            icon = "celebration",
            category = AchievementCategory.GENERAL,
            requirement = 1,
            progress = 0,
            tier = 1,
            points = 5
        ),
        AchievementEntity(
            id = "ach_explorer",
            title = "Explorer",
            description = "Visit all main sections of the app",
            icon = "explore",
            category = AchievementCategory.GENERAL,
            requirement = 5,
            progress = 0,
            tier = 1,
            points = 10
        ),

        // QUOTES Achievements
        AchievementEntity(
            id = "ach_quote_collector_bronze",
            title = "Quote Collector",
            description = "Favorite 10 quotes",
            icon = "bookmark",
            category = AchievementCategory.QUOTES,
            requirement = 10,
            progress = 0,
            tier = 1,
            points = 10
        ),
        AchievementEntity(
            id = "ach_quote_collector_silver",
            title = "Quote Enthusiast",
            description = "Favorite 50 quotes",
            icon = "bookmarks",
            category = AchievementCategory.QUOTES,
            requirement = 50,
            progress = 0,
            tier = 2,
            points = 25
        ),
        AchievementEntity(
            id = "ach_quote_collector_gold",
            title = "Quote Master",
            description = "Favorite 100 quotes",
            icon = "library_books",
            category = AchievementCategory.QUOTES,
            requirement = 100,
            progress = 0,
            tier = 3,
            points = 50
        ),
        AchievementEntity(
            id = "ach_daily_wisdom",
            title = "Daily Wisdom",
            description = "View quotes for 7 consecutive days",
            icon = "lightbulb",
            category = AchievementCategory.QUOTES,
            requirement = 7,
            progress = 0,
            tier = 1,
            points = 15
        ),

        // JOURNALING Achievements
        AchievementEntity(
            id = "ach_first_entry",
            title = "First Steps",
            description = "Write your first journal entry",
            icon = "edit_note",
            category = AchievementCategory.JOURNALING,
            requirement = 1,
            progress = 0,
            tier = 1,
            points = 10
        ),
        AchievementEntity(
            id = "ach_journal_bronze",
            title = "Diarist",
            description = "Write 10 journal entries",
            icon = "menu_book",
            category = AchievementCategory.JOURNALING,
            requirement = 10,
            progress = 0,
            tier = 1,
            points = 15
        ),
        AchievementEntity(
            id = "ach_journal_silver",
            title = "Chronicler",
            description = "Write 50 journal entries",
            icon = "auto_stories",
            category = AchievementCategory.JOURNALING,
            requirement = 50,
            progress = 0,
            tier = 2,
            points = 30
        ),
        AchievementEntity(
            id = "ach_journal_gold",
            title = "Master Chronicler",
            description = "Write 100 journal entries",
            icon = "library_add",
            category = AchievementCategory.JOURNALING,
            requirement = 100,
            progress = 0,
            tier = 3,
            points = 60
        ),
        AchievementEntity(
            id = "ach_wordsmith",
            title = "Wordsmith",
            description = "Write a journal entry with 500+ words",
            icon = "article",
            category = AchievementCategory.JOURNALING,
            requirement = 500,
            progress = 0,
            tier = 2,
            points = 20
        ),
        AchievementEntity(
            id = "ach_novelist",
            title = "Novelist",
            description = "Write a total of 10,000 words in journal entries",
            icon = "description",
            category = AchievementCategory.JOURNALING,
            requirement = 10000,
            progress = 0,
            tier = 3,
            points = 50
        ),

        // BREATHING Achievements
        AchievementEntity(
            id = "ach_first_breath",
            title = "First Breath",
            description = "Complete your first breathing exercise",
            icon = "air",
            category = AchievementCategory.BREATHING,
            requirement = 1,
            progress = 0,
            tier = 1,
            points = 10
        ),
        AchievementEntity(
            id = "ach_breathwork_bronze",
            title = "Breath Practitioner",
            description = "Complete 10 breathing sessions",
            icon = "wb_twilight",
            category = AchievementCategory.BREATHING,
            requirement = 10,
            progress = 0,
            tier = 1,
            points = 15
        ),
        AchievementEntity(
            id = "ach_breathwork_silver",
            title = "Breath Expert",
            description = "Complete 50 breathing sessions",
            icon = "nights_stay",
            category = AchievementCategory.BREATHING,
            requirement = 50,
            progress = 0,
            tier = 2,
            points = 30
        ),
        AchievementEntity(
            id = "ach_breathwork_gold",
            title = "Breath Master",
            description = "Complete 100 breathing sessions",
            icon = "spa",
            category = AchievementCategory.BREATHING,
            requirement = 100,
            progress = 0,
            tier = 3,
            points = 60
        ),

        // MEDITATION Achievements
        AchievementEntity(
            id = "ach_first_meditation",
            title = "Inner Peace Seeker",
            description = "Complete your first meditation",
            icon = "self_improvement",
            category = AchievementCategory.MEDITATION,
            requirement = 1,
            progress = 0,
            tier = 1,
            points = 10
        ),
        AchievementEntity(
            id = "ach_meditation_bronze",
            title = "Mindful Beginner",
            description = "Complete 10 meditation sessions",
            icon = "psychology",
            category = AchievementCategory.MEDITATION,
            requirement = 10,
            progress = 0,
            tier = 1,
            points = 15
        ),
        AchievementEntity(
            id = "ach_meditation_silver",
            title = "Mindful Practitioner",
            description = "Complete 50 meditation sessions",
            icon = "emoji_objects",
            category = AchievementCategory.MEDITATION,
            requirement = 50,
            progress = 0,
            tier = 2,
            points = 30
        ),
        AchievementEntity(
            id = "ach_meditation_gold",
            title = "Zen Master",
            description = "Complete 100 meditation sessions",
            icon = "verified",
            category = AchievementCategory.MEDITATION,
            requirement = 100,
            progress = 0,
            tier = 3,
            points = 60
        ),

        // CONSISTENCY Achievements
        AchievementEntity(
            id = "ach_streak_3",
            title = "Getting Started",
            description = "Maintain a 3-day streak",
            icon = "local_fire_department",
            category = AchievementCategory.CONSISTENCY,
            requirement = 3,
            progress = 0,
            tier = 1,
            points = 10
        ),
        AchievementEntity(
            id = "ach_streak_7",
            title = "Week Warrior",
            description = "Maintain a 7-day streak",
            icon = "whatshot",
            category = AchievementCategory.CONSISTENCY,
            requirement = 7,
            progress = 0,
            tier = 1,
            points = 20
        ),
        AchievementEntity(
            id = "ach_streak_30",
            title = "Dedicated",
            description = "Maintain a 30-day streak",
            icon = "military_tech",
            category = AchievementCategory.CONSISTENCY,
            requirement = 30,
            progress = 0,
            tier = 2,
            points = 50
        ),
        AchievementEntity(
            id = "ach_streak_100",
            title = "Unstoppable",
            description = "Maintain a 100-day streak",
            icon = "emoji_events",
            category = AchievementCategory.CONSISTENCY,
            requirement = 100,
            progress = 0,
            tier = 3,
            points = 100
        ),
        AchievementEntity(
            id = "ach_early_bird",
            title = "Early Bird",
            description = "Complete 10 activities before 9 AM",
            icon = "wb_sunny",
            category = AchievementCategory.CONSISTENCY,
            requirement = 10,
            progress = 0,
            tier = 2,
            points = 20,
            isHidden = true
        ),
        AchievementEntity(
            id = "ach_night_owl",
            title = "Night Owl",
            description = "Complete 10 activities after 10 PM",
            icon = "bedtime",
            category = AchievementCategory.CONSISTENCY,
            requirement = 10,
            progress = 0,
            tier = 2,
            points = 20,
            isHidden = true
        )
    )
}
