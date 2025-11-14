package com.po4yka.heauton.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.presentation.theme.HeautonTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for generating quote card images.
 *
 * Supports multiple card styles:
 * - MINIMAL: Plain text on solid background
 * - GRADIENT: Quote with gradient background
 * - ATTRIBUTED: Decorative card with attribution
 */
@Singleton
class QuoteCardGenerator @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    /**
     * Card styles for quote image generation.
     */
    enum class CardStyle {
        MINIMAL,
        GRADIENT,
        ATTRIBUTED
    }

    /**
     * Generates a bitmap image of a quote card.
     *
     * @param quote The quote to render
     * @param style The card style to use
     * @param width Width of the card in pixels
     * @param height Height of the card in pixels
     * @return Bitmap of the rendered quote card
     */
    suspend fun generateQuoteCard(
        quote: Quote,
        style: CardStyle = CardStyle.GRADIENT,
        width: Int = 1080,
        height: Int = 1920
    ): Bitmap = withContext(Dispatchers.Main) {
        val composeView = ComposeView(context).apply {
            setContent {
                HeautonTheme(darkTheme = false) {
                    Box(
                        modifier = Modifier
                            .size(width.dp, height.dp)
                            .background(Color.White)
                    ) {
                        when (style) {
                            CardStyle.MINIMAL -> MinimalCard(quote)
                            CardStyle.GRADIENT -> GradientCard(quote)
                            CardStyle.ATTRIBUTED -> AttributedCard(quote)
                        }
                    }
                }
            }
        }

        // Measure and layout the view
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        composeView.measure(widthSpec, heightSpec)
        composeView.layout(0, 0, width, height)

        // Create bitmap and draw
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        composeView.draw(canvas)

        bitmap
    }

    @Composable
    private fun MinimalCard(quote: Quote) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "\"${quote.text}\"",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )
            )

            if (!quote.source.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = quote.source,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                    )
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Heauton",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFD1D5DB),
                    letterSpacing = 2.sp
                )
            )
        }
    }

    @Composable
    private fun GradientCard(quote: Quote) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6),
                            Color(0xFFEC4899)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\"${quote.text}\"",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 48.sp
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                )

                if (!quote.source.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = quote.source,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            // Branding at bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
            ) {
                Text(
                    text = "Heauton",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 2.sp
                    )
                )
            }
        }
    }

    @Composable
    private fun AttributedCard(quote: Quote) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(48.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Decorative quote marks
                        Text(
                            text = "❝",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B5CF6).copy(alpha = 0.2f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = quote.text,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF1F2937),
                                textAlign = TextAlign.Center,
                                lineHeight = 42.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(2.dp)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.3f))
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = quote.author,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center
                            )
                        )

                        if (!quote.source.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = quote.source,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Italic,
                                    color = Color(0xFF9CA3AF),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }

            // Branding at bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
            ) {
                Text(
                    text = "HEAUTON",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD1D5DB),
                        letterSpacing = 3.sp
                    )
                )
            }
        }
    }
}
