package com.example.medgem.ui.components

/**
 * A unified conversation list screen that replaces the three separate
 * ConversationListScreen, ChatConversationListScreen, and RagConversationListScreen.
 *
 * @param title The screen title (e.g. "Chat Conversations", "RAG Conversations")
 * @param emptyIcon Icon shown in the empty-state placeholder
 * @param emptyText Text shown when no conversations exist
 * @param emptySubText Sub-text for the empty state
 * @param conversations Flow of conversation entities to display
 * @param visionEnabled null for screens without vision restriction (RAG), true/false for Chat
 * @param onNewRagConversation Optional callback for creating a new RAG conversation
 * @param onBack Back navigation callback
 * @param onConversationSelected Callback with (conversationId, isReadOnly)
 * @param onNewConversation Callback for creating a new conversation
 * @param chatRepository Repository for delete operations
 */
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.medgem.data.ChatRepository
import com.example.medgem.data.ConversationEntity
import com.example.medgem.data.ConversationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedConversationListScreen(
    title: String,
    emptyIcon: ImageVector,
    emptyText: String,
    emptySubText: String,
    conversations: Flow<List<ConversationEntity>>,
    visionEnabled: Boolean? = null,
    showBackButton: Boolean = true,
    onNewRagConversation: (() -> Unit)? = null,
    onBack: () -> Unit,
    onConversationSelected: (ConversationEntity, Boolean) -> Unit,
    onNewConversation: () -> Unit,
    chatRepository: ChatRepository
) {
    val coroutineScope = rememberCoroutineScope()
    val conversationList by conversations.collectAsState(initial = emptyList())
    val sheetState = rememberModalBottomSheetState()

    var showDeleteDialog by remember { mutableStateOf<ConversationEntity?>(null) }
    var showRenameDialog by remember { mutableStateOf<ConversationEntity?>(null) }
    var showVisionDisabledDialog by remember { mutableStateOf<ConversationEntity?>(null) }
    var showNewConversationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MedGemTopBar(
                title = title,
                onBack = if (showBackButton) onBack else null
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (onNewRagConversation != null) {
                        showNewConversationDialog = true
                    } else {
                        onNewConversation()
                    }
                },
                modifier = Modifier.semantics {
                    contentDescription = "New conversation"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New conversation"
                )
            }
        }
    ) { padding ->
        if (conversationList.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = emptyIcon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = emptySubText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = conversationList, key = { it.id }) { conversation ->
                    val hasImages = conversation.previewImagePath != null
                    val isRestricted = visionEnabled != null && hasImages && !visionEnabled

                    ConversationCard(
                        conversation = conversation,
                        defaultIcon = emptyIcon,
                        onClick = {
                            if (isRestricted) {
                                showVisionDisabledDialog = conversation
                            } else {
                                onConversationSelected(conversation, false)
                            }
                        },
                        onDelete = { showDeleteDialog = conversation },
                        onRename = { showRenameDialog = conversation },
                        isRestricted = isRestricted
                    )
                }
            }
        }
    }

    // Rename Dialog
    showRenameDialog?.let { conversation ->
        var newTitle by remember(conversation.id) { mutableStateOf(conversation.title) }
        val isTitleValid = newTitle.isNotBlank() && newTitle.length <= 100
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Conversation") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { if (it.length <= 100) newTitle = it },
                    label = { Text("Conversation Title") },
                    supportingText = {
                        Text("${newTitle.length}/100")
                    },
                    isError = newTitle.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newTitle.trim()
                        coroutineScope.launch {
                            try {
                                chatRepository.updateConversationTitle(conversation.id, trimmed)
                            } catch (e: Exception) {
                                Log.e("UnifiedConversationList", "Failed to rename conversation", e)
                            }
                        }
                        showRenameDialog = null
                    },
                    enabled = isTitleValid
                ) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) { Text("Cancel") }
            }
        )
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { conversation ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Conversation") },
            text = { Text("Are you sure you want to delete \"${conversation.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            chatRepository.deleteConversation(conversation.id)
                        }
                        showDeleteDialog = null
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }

    // Vision disabled dialog (only relevant when visionEnabled is not null)
    if (visionEnabled != null) {
        showVisionDisabledDialog?.let { conversation ->
            AlertDialog(
                onDismissRequest = { showVisionDisabledDialog = null },
                title = { Text("Vision Encoder Disabled") },
                text = {
                    Text(
                        "This conversation contains images and cannot be resumed while the vision encoder is disabled. " +
                                "You can view the conversation in read-only mode."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showVisionDisabledDialog = null
                            onConversationSelected(conversation, true)
                        }
                    ) { Text("View Only") }
                },
                dismissButton = {
                    TextButton(onClick = { showVisionDisabledDialog = null }) { Text("Cancel") }
                }
            )
        }
    }

    if (showNewConversationDialog) {
        ModalBottomSheet(
            onDismissRequest = { showNewConversationDialog = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = "New Conversation",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Standard Chat Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showNewConversationDialog = false
                            onNewConversation()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Standard Chat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Chat with the LLM directly",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // RAG Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showNewConversationDialog = false
                            onNewRagConversation?.invoke()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "RAG Search",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Search and chat with your documents",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationCard(
    conversation: ConversationEntity,
    defaultIcon: ImageVector,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    isRestricted: Boolean = false
) {
    val dateFormatter = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val conversationType = conversation.getType()
    val typeLabel = if (conversationType == ConversationType.RAG) "RAG" else "Chat"
    val typeBackground =
        if (conversationType == ConversationType.RAG) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }
    val typeForeground =
        if (conversationType == ConversationType.RAG) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Conversation: ${conversation.title}" },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preview image or default icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (conversation.previewImagePath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(conversation.previewImagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Conversation preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = defaultIcon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and timestamp
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(typeBackground)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = typeForeground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isRestricted) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "Vision disabled",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Vision disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        Text(
                            text = dateFormatter.format(Date(conversation.updatedAt)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Edit button
            IconButton(
                onClick = onRename,
                modifier = Modifier.semantics { contentDescription = "Rename conversation" }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Rename",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.semantics { contentDescription = "Delete conversation" }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
