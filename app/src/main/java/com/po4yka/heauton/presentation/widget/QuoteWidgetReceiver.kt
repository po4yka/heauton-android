package com.po4yka.heauton.presentation.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver for the Quote Widget.
 *
 * Handles widget lifecycle events:
 * - Widget added to home screen
 * - Widget updated
 * - Widget removed
 */
class QuoteWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = QuoteWidget()

    companion object {
        const val TAG = "QuoteWidgetReceiver"
    }
}
