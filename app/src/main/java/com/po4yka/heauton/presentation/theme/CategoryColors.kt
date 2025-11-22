package com.po4yka.heauton.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.po4yka.heauton.data.local.database.entities.AchievementCategory
import com.po4yka.heauton.data.local.database.entities.Difficulty
import com.po4yka.heauton.data.local.database.entities.ExerciseType
import com.po4yka.heauton.domain.model.Achievement

/**
 * Extension function to get the color for an achievement's tier.
 *
 * @return Color representing the tier (Bronze, Silver, or Gold)
 */
@Composable
fun Achievement.getTierColor(): Color {
    val semanticColors = MaterialTheme.semanticColors
    return when (tier) {
        1 -> semanticColors.tierBronze
        2 -> semanticColors.tierSilver
        3 -> semanticColors.tierGold
        else -> semanticColors.tierBronze
    }
}

/**
 * Extension function to get the color for a difficulty level.
 *
 * @return Color representing the difficulty (Beginner, Intermediate, or Advanced)
 */
@Composable
fun Difficulty.toColor(): Color {
    val semanticColors = MaterialTheme.semanticColors
    return when (this) {
        Difficulty.BEGINNER -> semanticColors.difficultyBeginner
        Difficulty.INTERMEDIATE -> semanticColors.difficultyIntermediate
        Difficulty.ADVANCED -> semanticColors.difficultyAdvanced
    }
}

/**
 * Extension function to get the color for an exercise type.
 *
 * @return Color representing the exercise type
 */
@Composable
fun ExerciseType.toColor(): Color {
    val semanticColors = MaterialTheme.semanticColors
    return when (this) {
        ExerciseType.MEDITATION -> semanticColors.typeMeditation
        ExerciseType.BREATHING -> semanticColors.typeBreathing
        ExerciseType.VISUALIZATION -> semanticColors.typeVisualization
        ExerciseType.BODY_SCAN -> semanticColors.typeBodyScan
    }
}

/**
 * Extension function to get the color for an achievement category.
 *
 * @return Color representing the category
 */
@Composable
fun AchievementCategory.toColor(): Color {
    val semanticColors = MaterialTheme.semanticColors
    return when (this) {
        AchievementCategory.QUOTES -> semanticColors.categoryQuotes
        AchievementCategory.JOURNALING -> semanticColors.categoryJournaling
        AchievementCategory.MEDITATION -> semanticColors.categoryMeditation
        AchievementCategory.BREATHING -> semanticColors.categoryBreathing
        AchievementCategory.CONSISTENCY -> semanticColors.categoryConsistency
        AchievementCategory.SOCIAL -> semanticColors.categorySocial
        AchievementCategory.GENERAL -> semanticColors.categoryGeneral
    }
}

/**
 * Get color for calendar heatmap based on activity level.
 *
 * @param level Activity intensity level (0-4)
 * @return Color representing the activity level
 */
@Composable
fun getHeatmapColor(level: Int): Color {
    val semanticColors = MaterialTheme.semanticColors
    return when (level) {
        0 -> semanticColors.heatmapLevel0
        1 -> semanticColors.heatmapLevel1
        2 -> semanticColors.heatmapLevel2
        3 -> semanticColors.heatmapLevel3
        else -> semanticColors.heatmapLevel4
    }
}
