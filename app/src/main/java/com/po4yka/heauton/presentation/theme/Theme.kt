package com.po4yka.heauton.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Data class for semantic accent colors.
 * These colors are used for specific semantic purposes beyond Material3's standard tokens.
 */
data class SemanticColors(
    // Phase 1 - Semantic colors
    val warning: Color,
    val success: Color,
    val info: Color,

    // Phase 2 - Mood colors
    val moodEnergeticJoy: Color,
    val moodCalmGratitude: Color,
    val moodNeutralReflect: Color,
    val moodAlertAnxious: Color,
    val moodSubduedSad: Color,

    // Phase 3 - Achievement tiers
    val tierBronze: Color,
    val tierSilver: Color,
    val tierGold: Color,

    // Phase 3 - Exercise difficulties
    val difficultyBeginner: Color,
    val difficultyIntermediate: Color,
    val difficultyAdvanced: Color,

    // Phase 3 - Exercise types
    val typeMeditation: Color,
    val typeBreathing: Color,
    val typeVisualization: Color,
    val typeBodyScan: Color,

    // Phase 3 - Achievement categories
    val categoryQuotes: Color,
    val categoryJournaling: Color,
    val categoryMeditation: Color,
    val categoryBreathing: Color,
    val categoryConsistency: Color,
    val categorySocial: Color,
    val categoryGeneral: Color,

    // Phase 3 - Calendar heatmap
    val heatmapLevel0: Color,
    val heatmapLevel1: Color,
    val heatmapLevel2: Color,
    val heatmapLevel3: Color,
    val heatmapLevel4: Color
)

/**
 * CompositionLocal for accessing semantic colors throughout the app.
 */
val LocalSemanticColors = staticCompositionLocalOf {
    SemanticColors(
        warning = SemanticWarning,
        success = SemanticSuccess,
        info = SemanticInfo,
        moodEnergeticJoy = MoodEnergeticJoy,
        moodCalmGratitude = MoodCalmGratitude,
        moodNeutralReflect = MoodNeutralReflect,
        moodAlertAnxious = MoodAlertAnxious,
        moodSubduedSad = MoodSubduedSad,
        tierBronze = TierBronze,
        tierSilver = TierSilver,
        tierGold = TierGold,
        difficultyBeginner = DifficultyBeginner,
        difficultyIntermediate = DifficultyIntermediate,
        difficultyAdvanced = DifficultyAdvanced,
        typeMeditation = TypeMeditation,
        typeBreathing = TypeBreathing,
        typeVisualization = TypeVisualization,
        typeBodyScan = TypeBodyScan,
        categoryQuotes = CategoryQuotes,
        categoryJournaling = CategoryJournaling,
        categoryMeditation = CategoryMeditation,
        categoryBreathing = CategoryBreathing,
        categoryConsistency = CategoryConsistency,
        categorySocial = CategorySocial,
        categoryGeneral = CategoryGeneral,
        heatmapLevel0 = HeatmapLevel0,
        heatmapLevel1 = HeatmapLevel1,
        heatmapLevel2 = HeatmapLevel2,
        heatmapLevel3 = HeatmapLevel3,
        heatmapLevel4 = HeatmapLevel4
    )
}

/**
 * Extension property to access semantic colors from MaterialTheme.
 */
val MaterialTheme.semanticColors: SemanticColors
    @Composable
    get() = LocalSemanticColors.current

/**
 * Light color scheme for Material 3.
 */
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = SemanticError,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF8D7DA),
    onErrorContainer = Color(0xFF721C24),
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant
)

/**
 * Dark color scheme for Material 3.
 */
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkSemanticError,
    onError = Color(0xFF212529),
    errorContainer = Color(0xFF5A1A1A),
    onErrorContainer = Color(0xFFF8D7DA),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

/**
 * Main theme for the Heauton app.
 * Uses Light Steel monochrome color palette.
 * Supports light/dark mode and dynamic colors on Android 12+.
 *
 * @param darkTheme Whether to use dark theme
 * @param dynamicColor Whether to use dynamic color (Android 12+)
 * @param content The composable content to theme
 */
@Composable
fun HeautonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic color is available on Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val semanticColors = if (darkTheme) {
        SemanticColors(
            warning = DarkSemanticWarning,
            success = DarkSemanticSuccess,
            info = DarkSemanticInfo,
            moodEnergeticJoy = DarkMoodEnergeticJoy,
            moodCalmGratitude = DarkMoodCalmGratitude,
            moodNeutralReflect = DarkMoodNeutralReflect,
            moodAlertAnxious = DarkMoodAlertAnxious,
            moodSubduedSad = DarkMoodSubduedSad,
            tierBronze = DarkTierBronze,
            tierSilver = DarkTierSilver,
            tierGold = DarkTierGold,
            difficultyBeginner = DarkDifficultyBeginner,
            difficultyIntermediate = DarkDifficultyIntermediate,
            difficultyAdvanced = DarkDifficultyAdvanced,
            typeMeditation = DarkTypeMeditation,
            typeBreathing = DarkTypeBreathing,
            typeVisualization = DarkTypeVisualization,
            typeBodyScan = DarkTypeBodyScan,
            categoryQuotes = DarkCategoryQuotes,
            categoryJournaling = DarkCategoryJournaling,
            categoryMeditation = DarkCategoryMeditation,
            categoryBreathing = DarkCategoryBreathing,
            categoryConsistency = DarkCategoryConsistency,
            categorySocial = DarkCategorySocial,
            categoryGeneral = DarkCategoryGeneral,
            heatmapLevel0 = DarkHeatmapLevel0,
            heatmapLevel1 = DarkHeatmapLevel1,
            heatmapLevel2 = DarkHeatmapLevel2,
            heatmapLevel3 = DarkHeatmapLevel3,
            heatmapLevel4 = DarkHeatmapLevel4
        )
    } else {
        SemanticColors(
            warning = SemanticWarning,
            success = SemanticSuccess,
            info = SemanticInfo,
            moodEnergeticJoy = MoodEnergeticJoy,
            moodCalmGratitude = MoodCalmGratitude,
            moodNeutralReflect = MoodNeutralReflect,
            moodAlertAnxious = MoodAlertAnxious,
            moodSubduedSad = MoodSubduedSad,
            tierBronze = TierBronze,
            tierSilver = TierSilver,
            tierGold = TierGold,
            difficultyBeginner = DifficultyBeginner,
            difficultyIntermediate = DifficultyIntermediate,
            difficultyAdvanced = DifficultyAdvanced,
            typeMeditation = TypeMeditation,
            typeBreathing = TypeBreathing,
            typeVisualization = TypeVisualization,
            typeBodyScan = TypeBodyScan,
            categoryQuotes = CategoryQuotes,
            categoryJournaling = CategoryJournaling,
            categoryMeditation = CategoryMeditation,
            categoryBreathing = CategoryBreathing,
            categoryConsistency = CategoryConsistency,
            categorySocial = CategorySocial,
            categoryGeneral = CategoryGeneral,
            heatmapLevel0 = HeatmapLevel0,
            heatmapLevel1 = HeatmapLevel1,
            heatmapLevel2 = HeatmapLevel2,
            heatmapLevel3 = HeatmapLevel3,
            heatmapLevel4 = HeatmapLevel4
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalSemanticColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
