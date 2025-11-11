package com.po4yka.heauton.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.po4yka.heauton.domain.model.ProgressSnapshot
import java.text.SimpleDateFormat
import java.util.*

/**
 * Calendar heatmap component showing activity intensity over time.
 *
 * Similar to GitHub's contribution graph.
 *
 * @param snapshots List of progress snapshots to display
 * @param modifier Modifier for the component
 * @param onDayClick Callback when a day is clicked
 */
@Composable
fun CalendarHeatmap(
    snapshots: List<ProgressSnapshot>,
    modifier: Modifier = Modifier,
    onDayClick: ((Long) -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    // Create map of date -> snapshot for quick lookup
    val snapshotMap = snapshots.associateBy { it.date }

    // Calculate date range
    val calendar = Calendar.getInstance()
    val endDate = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_YEAR, -89) // 90 days (roughly 3 months)
    val startDate = calendar.timeInMillis

    // Generate all dates in range
    val dates = generateDateRange(startDate, endDate)

    // Organize into weeks
    val weeks = organizeIntoWeeks(dates)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title
        Text(
            text = "Activity",
            style = MaterialTheme.typography.titleMedium
        )

        // Month labels
        MonthLabels(
            startDate = startDate,
            endDate = endDate,
            modifier = Modifier.fillMaxWidth()
        )

        // Heatmap grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Day labels
            DayLabels(modifier = Modifier.width(24.dp))

            // Week columns
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                weeks.forEach { week ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        week.forEach { date ->
                            val snapshot = snapshotMap[date]
                            val intensity = snapshot?.getActivityIntensity() ?: 0

                            HeatmapCell(
                                intensity = intensity,
                                primaryColor = primaryColor,
                                surfaceColor = surfaceVariant,
                                onClick = { onDayClick?.invoke(date) },
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Legend
        HeatmapLegend(
            primaryColor = primaryColor,
            surfaceColor = surfaceVariant,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun MonthLabels(
    startDate: Long,
    endDate: Long,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance()
    val monthFormatter = SimpleDateFormat("MMM", Locale.getDefault())

    val months = mutableListOf<String>()
    calendar.timeInMillis = startDate

    while (calendar.timeInMillis <= endDate) {
        if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
            months.add(monthFormatter.format(calendar.time))
        }
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    Row(
        modifier = modifier.padding(start = 28.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        months.forEach { month ->
            Text(
                text = month,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DayLabels(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HeatmapCell(
    intensity: Int,
    primaryColor: Color,
    surfaceColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (intensity) {
        0 -> surfaceColor
        1 -> primaryColor.copy(alpha = 0.2f)
        2 -> primaryColor.copy(alpha = 0.4f)
        3 -> primaryColor.copy(alpha = 0.6f)
        4 -> primaryColor.copy(alpha = 0.8f)
        else -> primaryColor
    }

    Box(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = color,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}

@Composable
private fun HeatmapLegend(
    primaryColor: Color,
    surfaceColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Less",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        listOf(0, 1, 2, 3, 4, 5).forEach { intensity ->
            val color = when (intensity) {
                0 -> surfaceColor
                1 -> primaryColor.copy(alpha = 0.2f)
                2 -> primaryColor.copy(alpha = 0.4f)
                3 -> primaryColor.copy(alpha = 0.6f)
                4 -> primaryColor.copy(alpha = 0.8f)
                else -> primaryColor
            }

            Box(modifier = Modifier.size(12.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )
                }
            }
        }

        Text(
            text = "More",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Generate list of dates between start and end (inclusive).
 */
private fun generateDateRange(startDate: Long, endDate: Long): List<Long> {
    val dates = mutableListOf<Long>()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = startDate

    // Normalize to midnight
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    while (calendar.timeInMillis <= endDate) {
        dates.add(calendar.timeInMillis)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    return dates
}

/**
 * Organize dates into weeks (7 days per week).
 */
private fun organizeIntoWeeks(dates: List<Long>): List<List<Long>> {
    val weeks = mutableListOf<List<Long>>()
    val calendar = Calendar.getInstance()

    // Find first Sunday (or start of week)
    calendar.timeInMillis = dates.firstOrNull() ?: return emptyList()
    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
        calendar.add(Calendar.DAY_OF_YEAR, -1)
    }

    val startOfWeek = calendar.timeInMillis
    calendar.timeInMillis = dates.lastOrNull() ?: return emptyList()

    // Generate weeks
    calendar.timeInMillis = startOfWeek
    val endDate = dates.last()

    while (calendar.timeInMillis <= endDate) {
        val week = mutableListOf<Long>()
        for (i in 0 until 7) {
            val date = calendar.timeInMillis
            week.add(date)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        weeks.add(week)
    }

    return weeks
}
