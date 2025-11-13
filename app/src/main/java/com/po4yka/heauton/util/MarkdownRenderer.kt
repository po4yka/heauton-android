package com.po4yka.heauton.util

import android.content.Context
import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import dagger.hilt.android.qualifiers.ApplicationContext
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper for Markwon library to render Markdown content.
 *
 * ## Features:
 * - Full Markdown support (CommonMark spec)
 * - Material 3 themed rendering
 * - Code syntax highlighting
 * - Link support
 * - List rendering
 * - Blockquote styling
 *
 * ## Usage in Compose:
 * ```kotlin
 * val markdownRenderer = rememberMarkdownRenderer()
 * AndroidView(
 *     factory = { context ->
 *         TextView(context).apply {
 *             markdownRenderer.setMarkdown(this, "# Hello **World**")
 *         }
 *     }
 * )
 * ```
 */
@Singleton
class MarkdownRenderer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var _markwon: Markwon? = null

    /**
     * Get or create Markwon instance with Material 3 theme.
     */
    private fun getMarkwon(
        primaryColor: Int,
        onSurfaceColor: Int,
        codeBackgroundColor: Int
    ): Markwon {
        return _markwon ?: createMarkwon(primaryColor, onSurfaceColor, codeBackgroundColor).also {
            _markwon = it
        }
    }

    /**
     * Create a new Markwon instance with custom theme.
     */
    private fun createMarkwon(
        primaryColor: Int,
        onSurfaceColor: Int,
        codeBackgroundColor: Int
    ): Markwon {
        return Markwon.builder(context)
            .usePlugin(object : io.noties.markwon.AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        // Links
                        .linkColor(primaryColor)
                        // Code blocks
                        .codeBackgroundColor(codeBackgroundColor)
                        .codeTextColor(onSurfaceColor)
                        .codeTypeface(Typeface.MONOSPACE)
                        // Blockquotes
                        .blockQuoteColor(primaryColor)
                        // Headings
                        .headingBreakHeight(0)
                        .headingTextSizeMultipliers(
                            floatArrayOf(2f, 1.5f, 1.25f, 1.1f, 1f, 0.9f)
                        )
                        // Lists
                        .listItemColor(primaryColor)
                }
            })
            .build()
    }

    /**
     * Render Markdown to a TextView.
     *
     * @param textView Target TextView
     * @param markdown Markdown content
     * @param primaryColor Primary theme color
     * @param onSurfaceColor On-surface color
     * @param codeBackgroundColor Background color for code blocks
     */
    fun setMarkdown(
        textView: android.widget.TextView,
        markdown: String,
        primaryColor: Int,
        onSurfaceColor: Int,
        codeBackgroundColor: Int
    ) {
        val markwon = getMarkwon(primaryColor, onSurfaceColor, codeBackgroundColor)
        markwon.setMarkdown(textView, markdown)
    }

    /**
     * Convert Markdown to Spanned for TextView.
     */
    fun toMarkdown(
        markdown: String,
        primaryColor: Int,
        onSurfaceColor: Int,
        codeBackgroundColor: Int
    ): android.text.Spanned {
        val markwon = getMarkwon(primaryColor, onSurfaceColor, codeBackgroundColor)
        return markwon.toMarkdown(markdown)
    }
}

/**
 * Markdown formatting helpers for the editor toolbar.
 */
object MarkdownFormatter {

    /**
     * Insert Markdown syntax at cursor position.
     *
     * @param text Current text
     * @param selection Current cursor position or selection
     * @param prefix Markdown prefix to insert
     * @param suffix Markdown suffix to insert (optional)
     * @return Updated text and new cursor position
     */
    data class FormattedText(
        val text: String,
        val cursorPosition: Int
    )

    /**
     * Apply bold formatting (**text**).
     */
    fun bold(text: String, selectionStart: Int, selectionEnd: Int): FormattedText {
        return wrapSelection(text, selectionStart, selectionEnd, "**", "**")
    }

    /**
     * Apply italic formatting (*text*).
     */
    fun italic(text: String, selectionStart: Int, selectionEnd: Int): FormattedText {
        return wrapSelection(text, selectionStart, selectionEnd, "_", "_")
    }

    /**
     * Apply inline code formatting (`code`).
     */
    fun inlineCode(text: String, selectionStart: Int, selectionEnd: Int): FormattedText {
        return wrapSelection(text, selectionStart, selectionEnd, "`", "`")
    }

    /**
     * Insert heading (# Heading).
     */
    fun heading(text: String, cursorPosition: Int, level: Int = 1): FormattedText {
        val prefix = "#".repeat(level) + " "
        return insertAtLineStart(text, cursorPosition, prefix)
    }

    /**
     * Insert unordered list item (- Item).
     */
    fun bulletList(text: String, cursorPosition: Int): FormattedText {
        return insertAtLineStart(text, cursorPosition, "- ")
    }

    /**
     * Insert ordered list item (1. Item).
     */
    fun numberedList(text: String, cursorPosition: Int): FormattedText {
        return insertAtLineStart(text, cursorPosition, "1. ")
    }

    /**
     * Insert blockquote (> Quote).
     */
    fun blockquote(text: String, cursorPosition: Int): FormattedText {
        return insertAtLineStart(text, cursorPosition, "> ")
    }

    /**
     * Insert code block (```code```).
     */
    fun codeBlock(text: String, selectionStart: Int, selectionEnd: Int): FormattedText {
        return wrapSelection(text, selectionStart, selectionEnd, "```\n", "\n```")
    }

    /**
     * Insert horizontal rule (---).
     */
    fun horizontalRule(text: String, cursorPosition: Int): FormattedText {
        val before = text.substring(0, cursorPosition)
        val after = text.substring(cursorPosition)
        val newText = "$before\n---\n$after"
        return FormattedText(newText, cursorPosition + 5)
    }

    /**
     * Helper: Wrap selected text with prefix and suffix.
     */
    private fun wrapSelection(
        text: String,
        selectionStart: Int,
        selectionEnd: Int,
        prefix: String,
        suffix: String
    ): FormattedText {
        val selectedText = if (selectionStart != selectionEnd) {
            text.substring(selectionStart, selectionEnd)
        } else {
            "text"
        }

        val before = text.substring(0, selectionStart)
        val after = text.substring(selectionEnd)

        val newText = "$before$prefix$selectedText$suffix$after"
        val newCursorPos = selectionStart + prefix.length + selectedText.length

        return FormattedText(newText, newCursorPos)
    }

    /**
     * Helper: Insert text at the beginning of the current line.
     */
    private fun insertAtLineStart(text: String, cursorPosition: Int, prefix: String): FormattedText {
        // Find the start of the current line
        var lineStart = cursorPosition
        while (lineStart > 0 && text[lineStart - 1] != '\n') {
            lineStart--
        }

        val before = text.substring(0, lineStart)
        val after = text.substring(lineStart)

        val newText = "$before$prefix$after"
        val newCursorPos = cursorPosition + prefix.length

        return FormattedText(newText, newCursorPos)
    }

    /**
     * Get word count excluding Markdown syntax.
     */
    fun getWordCount(markdown: String): Int {
        // Remove Markdown syntax for accurate word count
        val plainText = markdown
            .replace(Regex("[*_`~#\\[\\]()>]"), "")
            .replace(Regex("!\\[.*?\\]\\(.*?\\)"), "") // Remove images
            .replace(Regex("\\[.*?\\]\\(.*?\\)"), "") // Remove links
            .trim()

        if (plainText.isEmpty()) return 0

        return plainText.split(Regex("\\s+")).size
    }

    /**
     * Get character count excluding Markdown syntax.
     */
    fun getCharCount(markdown: String): Int {
        return getWordCount(markdown) // Reuse to get plain text
            .let { markdown.replace(Regex("[*_`~#\\[\\]()>]"), "").length }
    }
}

/**
 * Composable function to remember a MarkdownRenderer with Material 3 theme.
 */
@Composable
fun rememberMarkdownRenderer(): MarkdownRendererState {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary.toArgb()
    val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant.toArgb()

    return MarkdownRendererState(
        context = context,
        primaryColor = primary,
        onSurfaceColor = onSurface,
        codeBackgroundColor = surfaceVariant
    )
}

/**
 * State holder for MarkdownRenderer in Compose.
 */
data class MarkdownRendererState(
    val context: Context,
    val primaryColor: Int,
    val onSurfaceColor: Int,
    val codeBackgroundColor: Int
) {
    private val renderer = MarkdownRenderer(context)

    fun setMarkdown(textView: android.widget.TextView, markdown: String) {
        renderer.setMarkdown(
            textView,
            markdown,
            primaryColor,
            onSurfaceColor,
            codeBackgroundColor
        )
    }

    fun toMarkdown(markdown: String): android.text.Spanned {
        return renderer.toMarkdown(
            markdown,
            primaryColor,
            onSurfaceColor,
            codeBackgroundColor
        )
    }
}
