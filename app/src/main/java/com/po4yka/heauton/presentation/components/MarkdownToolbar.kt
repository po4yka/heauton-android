package com.po4yka.heauton.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.po4yka.heauton.presentation.screens.journal.JournalEditorContract

/**
 * Markdown formatting toolbar for the journal editor.
 *
 * ## Features:
 * - Common Markdown formatting buttons
 * - Bold, Italic, Code formatting
 * - Headings (H1, H2, H3)
 * - Lists (bullet, numbered)
 * - Blockquote, Code block
 * - Horizontal scrolling for all buttons
 *
 * @param onFormatClick Callback when a formatting button is clicked
 * @param modifier Modifier for the toolbar
 */
@Composable
fun MarkdownToolbar(
    onFormatClick: (JournalEditorContract.MarkdownFormatting) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bold
            ToolbarButton(
                icon = {
                    Text(
                        text = "B",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                onClick = { onFormatClick(JournalEditorContract.MarkdownFormatting.BOLD) },
                contentDescription = "Bold"
            )

            // Italic
            ToolbarButton(
                icon = {
                    Text(
                        text = "I",
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    )
                },
                onClick = { onFormatClick(JournalEditorContract.MarkdownFormatting.ITALIC) },
                contentDescription = "Italic"
            )

            // Code
            ToolbarButton(
                icon = { Icon(Icons.Default.Code, contentDescription = null) },
                onClick = { onFormatClick(JournalEditorContract.MarkdownFormatting.CODE) },
                contentDescription = "Inline code"
            )

            VerticalHorizontalDivider(modifier = Modifier.height(24.dp))

            // Heading 1
            ToolbarButton(
                icon = {
                    Text(
                        text = "H1",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                onClick = { onFormatClick(JournalEditorContract.MarkdownFormatting.HEADING_1) },
                contentDescription = "Heading 1"
            )

            // Heading 2
            ToolbarButton(
                icon = {
                    Text(
                        text = "H2",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                onClick = { onFormatClick(JournalEditorContract.MarkdownFormatting.HEADING_2) },
                contentDescription = "Heading 2"
            )

            // Heading 3
            ToolbarButton(
                icon = {
                    Text(
                        text = "H3",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                onClick = { onFormatClick(JournalEditorContract.MarkdownFormatting.HEADING_3) },
                contentDescription = "Heading 3"
            )

            VerticalHorizontalDivider(modifier = Modifier.height(24.dp))

            // Bullet list
            ToolbarButton(
                icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = null) },
                onClick = { onFormatClick(JournalEditorContract.MarkdownFormatting.BULLET_LIST) },
                contentDescription = "Bullet list"
            )

            // Numbered list
            ToolbarButton(
                icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) },
                onClick = { onFormatClick(JournalEditorContract.MarkdownFormatting.NUMBERED_LIST) },
                contentDescription = "Numbered list"
            )

            VerticalHorizontalDivider(modifier = Modifier.height(24.dp))

            // Blockquote
            ToolbarButton(
                icon = { Icon(Icons.Default.FormatQuote, contentDescription = null) },
                onClick = { onFormatClick(JournalEditorContract.MarkdownFormatting.BLOCKQUOTE) },
                contentDescription = "Blockquote"
            )

            // Code block
            ToolbarButton(
                icon = { Icon(Icons.Default.DataObject, contentDescription = null) },
                onClick = { onFormatClick(JournalEditorContract.MarkdownFormatting.CODE_BLOCK) },
                contentDescription = "Code block"
            )
        }
    }
}

/**
 * Individual toolbar button.
 */
@Composable
private fun ToolbarButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp)
    ) {
        icon()
    }
}

@Composable
private fun VerticalHorizontalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .padding(vertical = 4.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxHeight(),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
