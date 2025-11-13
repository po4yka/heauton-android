package com.po4yka.heauton.presentation.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.po4yka.heauton.R
import com.po4yka.heauton.presentation.MainActivity

/**
 * Content for the Quote Widget.
 *
 * Adapts layout based on widget size:
 * - Small (< 200dp width): Quote text only, truncated
 * - Medium (200-300dp width): Quote + Author
 * - Large (> 300dp width): Quote + Author + Source + Icon
 */
@Composable
fun QuoteWidgetContent(
    quote: WidgetQuote?
) {
    val context = LocalContext.current
    val size = LocalSize.current

    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .cornerRadius(16.dp)
                .padding(16.dp)
                .clickable(actionStartActivity(createOpenAppIntent(context, quote?.id)))
        ) {
            if (quote != null) {
                when {
                    size.width < 200.dp -> SmallWidgetLayout(quote)
                    size.width < 300.dp -> MediumWidgetLayout(quote)
                    else -> LargeWidgetLayout(quote)
                }
            } else {
                EmptyWidgetLayout()
            }
        }
    }
}

/**
 * Small widget layout: Quote text only (truncated).
 */
@Composable
private fun SmallWidgetLayout(quote: WidgetQuote) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = quote.text.take(100) + if (quote.text.length > 100) "..." else "",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = GlanceTheme.colors.onBackground,
                textAlign = TextAlign.Center
            ),
            maxLines = 4
        )
    }
}

/**
 * Medium widget layout: Quote + Author.
 */
@Composable
private fun MediumWidgetLayout(quote: WidgetQuote) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.Top
    ) {
        // Quote text
        Text(
            text = "\"${quote.text.take(150)}${if (quote.text.length > 150) "..." else ""}\"",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = GlanceTheme.colors.onBackground,
                textAlign = TextAlign.Start
            ),
            maxLines = 5
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Author
        Text(
            text = "— ${quote.author}",
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ColorProvider(
                    androidx.compose.ui.graphics.Color(0xFF6B7280)
                ),
                textAlign = TextAlign.End
            )
        )
    }
}

/**
 * Large widget layout: Quote + Author + Source + Icon.
 */
@Composable
private fun LargeWidgetLayout(quote: WidgetQuote) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.Top
    ) {
        // Header with icon
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_launcher_foreground),
                contentDescription = "Heauton",
                modifier = GlanceModifier.size(32.dp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "Daily Quote",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        // Quote text
        Text(
            text = "\"${quote.text}\"",
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = GlanceTheme.colors.onBackground,
                textAlign = TextAlign.Start
            ),
            maxLines = 8
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Author and source
        Column(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "— ${quote.author}",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(
                        androidx.compose.ui.graphics.Color(0xFF4B5563)
                    ),
                    textAlign = TextAlign.End
                )
            )

            if (!quote.source.isNullOrBlank()) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = quote.source,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = ColorProvider(
                            androidx.compose.ui.graphics.Color(0xFF6B7280)
                        ),
                        textAlign = TextAlign.End
                    )
                )
            }
        }
    }
}

/**
 * Empty widget layout: Shown when no quote is available.
 */
@Composable
private fun EmptyWidgetLayout() {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_launcher_foreground),
            contentDescription = "Heauton",
            modifier = GlanceModifier.size(48.dp)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "Tap to open Heauton",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = ColorProvider(
                    androidx.compose.ui.graphics.Color(0xFF6B7280)
                ),
                textAlign = TextAlign.Center
            )
        )
    }
}

/**
 * Creates an intent to open the app (with optional quote ID).
 */
private fun createOpenAppIntent(context: Context, quoteId: String?): Intent {
    return Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        if (quoteId != null) {
            putExtra("quote_id", quoteId)
            putExtra("source", "widget")
        }
    }
}
