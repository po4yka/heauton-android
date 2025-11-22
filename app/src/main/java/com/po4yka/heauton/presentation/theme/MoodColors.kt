package com.po4yka.heauton.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.po4yka.heauton.data.local.database.entities.JournalMood

/**
 * Extension function to get the color for a JournalMood.
 * Maps each mood to one of the 5 mood category colors.
 */
@Composable
fun JournalMood.toColor(): Color {
    val semanticColors = MaterialTheme.semanticColors
    return when (this) {
        JournalMood.JOYFUL -> semanticColors.moodEnergeticJoy
        JournalMood.MOTIVATED -> semanticColors.moodEnergeticJoy
        JournalMood.GRATEFUL -> semanticColors.moodCalmGratitude
        JournalMood.PEACEFUL -> semanticColors.moodCalmGratitude
        JournalMood.NEUTRAL -> semanticColors.moodNeutralReflect
        JournalMood.REFLECTIVE -> semanticColors.moodNeutralReflect
        JournalMood.ANXIOUS -> semanticColors.moodAlertAnxious
        JournalMood.SAD -> semanticColors.moodSubduedSad
        JournalMood.FRUSTRATED -> semanticColors.moodSubduedSad
    }
}

/**
 * Helper function to get mood color from string mood name.
 * Used for components that work with string mood names instead of JournalMood enum.
 *
 * @param moodName String mood name (e.g., "Happy", "Peaceful", etc.)
 * @return Color corresponding to the mood, defaults to neutral if not found
 */
@Composable
fun getMoodColorFromString(moodName: String): Color {
    val semanticColors = MaterialTheme.semanticColors
    return when (moodName) {
        "Happy", "Joyful", "Motivated" -> semanticColors.moodEnergeticJoy
        "Peaceful", "Grateful" -> semanticColors.moodCalmGratitude
        "Neutral", "Reflective" -> semanticColors.moodNeutralReflect
        "Anxious" -> semanticColors.moodAlertAnxious
        "Sad", "Frustrated" -> semanticColors.moodSubduedSad
        else -> semanticColors.moodNeutralReflect
    }
}
