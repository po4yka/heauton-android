package com.po4yka.heauton.domain.usecase.quote

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.po4yka.heauton.domain.model.Quote
import com.po4yka.heauton.domain.repository.QuotesRepository
import com.po4yka.heauton.util.QuoteCardGenerator
import com.po4yka.heauton.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Use case for sharing quotes.
 *
 * Supports multiple sharing methods:
 * - Share as image (generated card)
 * - Share as text
 * - Copy to clipboard
 */
class ShareQuoteUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val quotesRepository: QuotesRepository,
    private val quoteCardGenerator: QuoteCardGenerator
) {
    /**
     * Share types supported.
     */
    enum class ShareType {
        IMAGE,
        TEXT,
        CLIPBOARD
    }

    /**
     * Shares a quote using the specified method.
     *
     * @param quoteId ID of the quote to share
     * @param shareType Type of sharing
     * @param cardStyle Style for image sharing (if IMAGE type)
     * @return Result with Intent to launch share sheet (null for CLIPBOARD)
     */
    suspend operator fun invoke(
        quoteId: String,
        shareType: ShareType,
        cardStyle: QuoteCardGenerator.CardStyle = QuoteCardGenerator.CardStyle.GRADIENT
    ): Result<Intent?> {
        return try {
            // Get quote
            val quote = quotesRepository.getQuoteById(quoteId)
            if (quote == null) {
                return Result.Error("Quote not found")
            }

            when (shareType) {
                ShareType.IMAGE -> shareAsImage(quote, cardStyle)
                ShareType.TEXT -> shareAsText(quote)
                ShareType.CLIPBOARD -> copyToClipboard(quote)
            }
        } catch (e: Exception) {
            Result.Error("Failed to share quote: ${e.message}")
        }
    }

    /**
     * Shares quote as an image.
     */
    private suspend fun shareAsImage(
        quote: Quote,
        cardStyle: QuoteCardGenerator.CardStyle
    ): Result<Intent?> {
        return try {
            // Generate quote card
            val bitmap = quoteCardGenerator.generateQuoteCard(quote, cardStyle)

            // Save to cache
            val imageFile = saveBitmapToCache(bitmap, "quote_${quote.id}.png")

            // Get URI via FileProvider
            val imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            // Create share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, formatQuoteText(quote))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share Quote")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            Result.Success(chooserIntent)
        } catch (e: Exception) {
            Result.Error("Failed to share as image: ${e.message}")
        }
    }

    /**
     * Shares quote as text.
     */
    private fun shareAsText(quote: Quote): Result<Intent?> {
        return try {
            val shareText = formatQuoteText(quote)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share Quote")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            Result.Success(chooserIntent)
        } catch (e: Exception) {
            Result.Error("Failed to share as text: ${e.message}")
        }
    }

    /**
     * Copies quote to clipboard.
     */
    private fun copyToClipboard(quote: Quote): Result<Intent?> {
        return try {
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Quote", formatQuoteText(quote))
            clipboardManager.setPrimaryClip(clip)

            Result.Success(null) // No intent needed for clipboard
        } catch (e: Exception) {
            Result.Error("Failed to copy to clipboard: ${e.message}")
        }
    }

    /**
     * Formats quote text for sharing.
     */
    private fun formatQuoteText(quote: Quote): String {
        return buildString {
            append("\"${quote.text}\"\n\n")
            append("— ${quote.author}")
            if (!quote.source.isNullOrBlank()) {
                append(", ${quote.source}")
            }
            append("\n\nShared via Heauton")
        }
    }

    /**
     * Saves bitmap to cache directory.
     */
    private suspend fun saveBitmapToCache(bitmap: Bitmap, filename: String): File {
        return withContext(Dispatchers.IO) {
            val cacheDir = File(context.cacheDir, "shared_images")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val imageFile = File(cacheDir, filename)
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            imageFile
        }
    }
}
