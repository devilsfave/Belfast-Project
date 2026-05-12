package com.example.medgem.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText

/**
 * Interface for messages that can be displayed in the shared MessageList.
 */
interface DisplayableMessage {
    val id: Long
    val content: String
    val isUser: Boolean
    val imagePaths: List<String>
    val thought: String? // Added thought field
    val initiallyExpanded: Boolean
}

/**
 * Shared MessageList component to render chat history.
 */
@Composable
fun MessageList(
    messages: List<DisplayableMessage>,
    listState: LazyListState,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
    showTypingIndicator: Boolean = true,
    onImageClick: ((String) -> Unit)? = null,
    bubbleContent: (@Composable (DisplayableMessage) -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .semantics { contentDescription = "Chat messages" },
        state = listState,
        contentPadding = PaddingValues(bottom = 16.dp),
        reverseLayout = true
    ) {
        if (isGenerating && showTypingIndicator) {
            item { TypingIndicator() }
        }
        items(messages, key = { it.id }) { message ->
            if (bubbleContent == null) {
                GenericChatBubble(message, onImageClick = onImageClick)
            } else {
                bubbleContent(message)
            }
        }
    }
}

/**
 * A generic chat bubble component.
 */
@Composable
fun GenericChatBubble(
    message: DisplayableMessage,
    onImageClick: ((String) -> Unit)? = null,
    content: @Composable () -> Unit = {
        RichText { Markdown(content = message.content) }
    }
) {
    val isUser = message.isUser

    // Layout: Avatar + Bubble row for AI, Bubble for User
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // AI Avatar
        if (!isUser) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .size(32.dp)
                    .padding(top = 4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "MedGem Bot",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // Thinking Process (if any) displayed *before* the answer (or collapsed)
            if (!message.thought.isNullOrEmpty()) {
                ThinkingBlock(
                    thought = message.thought!!,
                    messageId = message.id,
                    initiallyExpanded = message.initiallyExpanded
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Message Bubble
            Surface(
                shape = if (isUser)
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 4.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp
                    )
                else
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp
                    ),
                color = if (isUser)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 340.dp)
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Column {
                        // Display Images if present
                        val paths = message.imagePaths
                        if (paths.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                paths.forEachIndexed { index, path ->
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(path)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Image ${index + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(if (paths.size == 1) 200.dp else 100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onImageClick?.invoke(path) }
                                    )
                                }
                            }
                        }

                        if (isUser) {
                            Text(
                                text = message.content,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            content()
                        }
                    }
                }
            }
        }
    }
}

/**
 * A shared Thinking Block component.
 */
@Composable
fun ThinkingBlock(
    thought: String,
    messageId: Long,
    initiallyExpanded: Boolean,
    title: String = "Thinking Process"
) {
    var isExpanded by rememberSaveable(messageId) { mutableStateOf(initiallyExpanded) }

    val borderColor =
        if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent
    val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column {
            // Header (Always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Content (Expandable)
            if (isExpanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                ) {
                    RichText {
                        Markdown(content = thought)
                    }
                }
            }
        }
    }
}
