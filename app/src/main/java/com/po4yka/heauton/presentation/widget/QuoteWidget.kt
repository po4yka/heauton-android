package com.po4yka.heauton.presentation.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.domain.repository.ScheduleRepository
import com.po4yka.heauton.util.Result
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Glance App Widget for displaying daily quotes.
 *
 * Supports multiple sizes:
 * - Small: Quote text (truncated)
 * - Medium: Quote + author
 * - Large: Quote + author + source + actions
 */
class QuoteWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Get repositories via Hilt
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            QuoteWidgetEntryPoint::class.java
        )
        val quotesRepository = entryPoint.quotesRepository()
        val scheduleRepository = entryPoint.scheduleRepository()

        // Get quote to display
        val quote = getQuoteForWidget(quotesRepository, scheduleRepository)

        provideContent {
            QuoteWidgetContent(quote = quote)
        }
    }

    private suspend fun getQuoteForWidget(
        quotesRepository: QuotesRepository,
        scheduleRepository: ScheduleRepository
    ): WidgetQuote? {
        return try {
            // Try to get quote from default schedule
            val defaultSchedule = scheduleRepository.getDefaultSchedule()
            if (defaultSchedule is Result.Success<*> && defaultSchedule.data != null) {
                @Suppress("UNCHECKED_CAST")
                val schedule = defaultSchedule.data as com.po4yka.heauton.domain.model.QuoteSchedule
                if (schedule.lastDeliveredQuoteId != null) {
                    // Show last delivered quote
                    val quoteResult = quotesRepository.getQuoteById(schedule.lastDeliveredQuoteId)
                    when (quoteResult) {
                        is Result.Success<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            val quote = quoteResult.data as? com.po4yka.heauton.domain.model.Quote
                            quote?.let {
                                WidgetQuote(
                                    id = it.id,
                                    text = it.text,
                                    author = it.author,
                                    source = it.source
                                )
                            }
                        }
                        is Result.Error -> null
                        else -> null
                    }
                } else {
                    // Get a random quote
                    val quoteResult = quotesRepository.getRandomQuote()
                    when (quoteResult) {
                        is Result.Success<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            val quote = quoteResult.data as? com.po4yka.heauton.domain.model.Quote
                            quote?.let {
                                WidgetQuote(
                                    id = it.id,
                                    text = it.text,
                                    author = it.author,
                                    source = it.source
                                )
                            }
                        }
                        is Result.Error -> null
                        else -> null
                    }
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Hilt EntryPoint for accessing dependencies in widget.
     */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface QuoteWidgetEntryPoint {
        fun quotesRepository(): QuotesRepository
        fun scheduleRepository(): ScheduleRepository
    }
}

/**
 * Data class representing a quote for widget display.
 */
data class WidgetQuote(
    val id: String,
    val text: String,
    val author: String,
    val source: String?
)
