package com.example.medgem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.medgem.data.UserPreferencesRepository
import com.example.medgem.ui.components.ChatInputBar
import com.example.medgem.ui.components.ChatTopBar
import com.example.medgem.ui.components.ClearConversationDialog
import com.example.medgem.ui.components.FullScreenImageDialog
import com.example.medgem.ui.components.ImageErrorBanner
import com.example.medgem.ui.components.ImagePreviewRow
import com.example.medgem.ui.components.MessageList
import com.example.medgem.ui.components.ThinkingBlock
import com.example.medgem.ui.components.rememberImagePickers
import com.example.medgem.ui.viewmodel.RagChatViewModel
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import org.pytorch.executorch.extension.llm.LlmModule
import java.util.regex.Pattern

private const val TAG = "RagChatScreen"

// RAG Constants
private const val MAX_SEARCH_STEPS = 3
private const val MAX_CONTEXT_TOKENS = 2048

@Composable
fun RagChatScreen(
    onBack: () -> Unit,
    onPdfClick: (String, Int) -> Unit = { _, _ -> },
    conversationId: Long? = null,
    llmModule: LlmModule? = null,
    isModelLoaded: Boolean = false,
    modelStatusMessage: String = "Initializing...",
    userPreferencesRepository: UserPreferencesRepository? = null,
    lastLoadedConversationId: Long? = null,
    onConversationLoaded: (Long?) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: RagChatViewModel = hiltViewModel()

    // Settings
    val temperature =
        userPreferencesRepository?.ragTemperature?.collectAsState(initial = 0.8f)?.value ?: 0.8f
    val topP = userPreferencesRepository?.ragTopP?.collectAsState(initial = 0.9f)?.value ?: 0.9f
    val maxSearchStepsSetting =
        userPreferencesRepository?.maxSearchSteps?.collectAsState(initial = MAX_SEARCH_STEPS)
            ?.value
            ?: MAX_SEARCH_STEPS
    val maxContextTokensSetting =
        userPreferencesRepository?.maxContextTokens?.collectAsState(
            initial = MAX_CONTEXT_TOKENS
        )
            ?.value
            ?: MAX_CONTEXT_TOKENS
    val maxRetrievedDocsSetting =
        userPreferencesRepository?.maxRetrievedDocs?.collectAsState(initial = 3)?.value ?: 3
    val minRetrievalScoreSetting =
        userPreferencesRepository?.minRetrievalScore?.collectAsState(initial = 0.5f)?.value
            ?: 0.5f
    val visionEnabled =
        userPreferencesRepository?.visionEnabled?.collectAsState(initial = true)?.value ?: true
    val maxSequenceLength =
        userPreferencesRepository?.maxSequenceLength?.collectAsState(initial = 4096)?.value
            ?: 4096

    val messages = viewModel.messages
    val inputText by viewModel.inputTextState.collectAsState()
    val selectedImages by viewModel.selectedImagesState.collectAsState()

    LaunchedEffect(conversationId) { viewModel.syncConversationId(conversationId) }

    val isGenerating by viewModel.isGeneratingState.collectAsState()
    val currentSearchQuery by viewModel.currentSearchQueryState.collectAsState()
    val modelStatus by viewModel.modelStatusState.collectAsState()
    val isRestoring by viewModel.isRestoringState.collectAsState()
    val showClearDialog by viewModel.showClearDialogState.collectAsState()
    val imageLoadError by viewModel.imageLoadErrorState.collectAsState()
    val isVoiceRecording by viewModel.isVoiceRecordingState.collectAsState()

    LaunchedEffect(modelStatusMessage) { viewModel.modelStatusState.value = modelStatusMessage }

    // Image picker/camera via shared helper
    val imagePickers =
        rememberImagePickers(viewModel.selectedImagesState, viewModel.imageLoadErrorState)

    // Permission launcher for audio recording
    val permissionLauncher =
        androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.startVoiceInput()
            } else {
                // Optionally handle permission denial (e.g., show a snackbar)
            }
        }

    val listState = rememberLazyListState()

    // Full screen image state
    var selectedImage by remember { mutableStateOf<String?>(null) }

    val canSend by remember {
        derivedStateOf {
            isModelLoaded &&
                    llmModule != null &&
                    (inputText.isNotBlank() || selectedImages.isNotEmpty()) &&
                    !isGenerating &&
                    !isRestoring
        }
    }

    LaunchedEffect(messages.size, isGenerating, currentSearchQuery) {
        if (messages.isNotEmpty() || isGenerating) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(conversationId, llmModule) {
        viewModel.loadConversation(conversationId, lastLoadedConversationId, onConversationLoaded)
    }

    val onSend = {
        viewModel.sendMessage(
            context = context,
            temperature = temperature,
            topP = topP,
            maxSearchSteps = maxSearchStepsSetting,
            maxContextTokens = maxContextTokensSetting,
            maxRetrievedDocs = maxRetrievedDocsSetting,
            minRetrievalScore = minRetrievalScoreSetting,
            maxTokens = maxSequenceLength,
            onConversationLoaded = onConversationLoaded
        )
    }

    // Clear confirmation dialog
    if (showClearDialog) {
        ClearConversationDialog(
            onConfirm = { viewModel.clearConversation(onConversationLoaded) },
            onDismiss = { viewModel.showClearDialogState.value = false }
        )
    }

    // Full screen image dialog
    if (selectedImage != null) {
        FullScreenImageDialog(imagePath = selectedImage!!, onDismiss = { selectedImage = null })
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                title = "Search",
                onBack = onBack,
                onNewChat = { viewModel.showClearDialogState.value = true },
                newChatEnabled = messages.isNotEmpty() && !isGenerating
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Status / Search Indicator
            if (currentSearchQuery != null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = "Searching knowledge base for: \"${currentSearchQuery}\"...",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (!isModelLoaded) {
                Text(text = modelStatus, modifier = Modifier.padding(16.dp))
            }

            // Chat List
            MessageList(
                messages = messages,
                listState = listState,
                isGenerating = isGenerating && currentSearchQuery == null,
                modifier = Modifier.weight(1f),
                onImageClick = { selectedImage = it },
                bubbleContent = { message ->
                    RagChatBubble(
                        message = message as RagChatMessage,
                        onPdfClick = onPdfClick,
                        onImageClick = { selectedImage = it }
                    )
                }
            )

            // Input Area
            Column(modifier = Modifier.fillMaxWidth()) {
                ImageErrorBanner(
                    errorMessage = imageLoadError,
                    onDismiss = { viewModel.imageLoadErrorState.value = null }
                )

                ImagePreviewRow(
                    images = selectedImages,
                    onRemoveImage = { index ->
                        val updatedList = selectedImages.toMutableList()
                        val removedPath = updatedList.removeAt(index)
                        try {
                            java.io.File(removedPath).delete()
                        } catch (e: Exception) {
                            android.util.Log.e("RagChatScreen", "Error deleting temp image", e)
                        }
                        viewModel.selectedImagesState.value = updatedList
                    }
                )

                ChatInputBar(
                    inputText = inputText,
                    onInputChange = { viewModel.inputTextState.value = it },
                    onSend = onSend,
                    onStop = { viewModel.stopGeneration() },
                    onPickImage = imagePickers.pickImages,
                    onTakePhoto = imagePickers.takePhoto,
                    onStartVoiceInput = {
                        val permissionCheckResult =
                            androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.RECORD_AUDIO
                            )
                        if (permissionCheckResult ==
                            android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            viewModel.startVoiceInput()
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onStopVoiceInput = { viewModel.stopVoiceInput() },
                    isVoiceRecording = isVoiceRecording,
                    isGenerating = isGenerating,
                    isModelLoaded = isModelLoaded,
                    isRestoring = isRestoring,
                    visionEnabled = visionEnabled,
                    canSend = canSend,
                    hasImages = selectedImages.isNotEmpty(),
                    placeholder = "Ask a medical question..."
                )
            }
        }
    }
}

// Helper for Search
sealed class ContentBlock {
    data class Text(val content: String) : ContentBlock()
    data class Thought(val content: String, val isComplete: Boolean) : ContentBlock()
    data class ToolCall(val query: String) : ContentBlock()
}

// Helper to parse content with multiple thinking blocks and tool calls
private fun parseRagContent(content: String): List<ContentBlock> {
    val blocks = mutableListOf<ContentBlock>()
    val startTag = "<unused94>"
    val endTag = "<unused95>"

    var currentIndex = 0
    while (currentIndex < content.length) {
        val startIndex = content.indexOf(startTag, currentIndex)

        if (startIndex == -1) {
            val remaining = content.substring(currentIndex)
            if (remaining.isNotBlank()) {
                blocks.add(ContentBlock.Text(remaining))
            }
            break
        }

        if (startIndex > currentIndex) {
            val textBefore = content.substring(currentIndex, startIndex)
            if (textBefore.isNotBlank()) {
                blocks.add(ContentBlock.Text(textBefore))
            }
        }

        val afterStartTagIndex = startIndex + startTag.length
        val endIndex = content.indexOf(endTag, afterStartTagIndex)

        if (endIndex != -1) {
            val thoughtContent = content.substring(afterStartTagIndex, endIndex).trim()
            if (thoughtContent.isNotBlank()) {
                blocks.add(ContentBlock.Thought(thoughtContent, isComplete = true))
            }
            currentIndex = endIndex + endTag.length
        } else {
            val thoughtContent = content.substring(afterStartTagIndex).trim()
            if (thoughtContent.isNotBlank()) {
                blocks.add(ContentBlock.Thought(thoughtContent, isComplete = false))
            }
            break
        }
    }

    // Pass 2: Extract Tool Calls
    val searchRegex =
        Pattern.compile(
            "\\*\\*Search Query:\\*\\*\\s*\"([^\"]*)\"",
            Pattern.DOTALL or Pattern.CASE_INSENSITIVE
        )

    val refinedBlocks = mutableListOf<ContentBlock>()
    for (block in blocks) {
        when (block) {
            is ContentBlock.Text -> {
                val matcher = searchRegex.matcher(block.content)
                var lastTextEnd = 0
                var foundAny = false

                while (matcher.find()) {
                    foundAny = true
                    val start = matcher.start()
                    val end = matcher.end()
                    val query = matcher.group(1) ?: ""

                    if (start > lastTextEnd) {
                        val textBefore = block.content.substring(lastTextEnd, start).trim()
                        if (textBefore.isNotEmpty()) {
                            refinedBlocks.add(ContentBlock.Text(textBefore))
                        }
                    }

                    refinedBlocks.add(ContentBlock.ToolCall(query))
                    lastTextEnd = end
                }

                if (foundAny && lastTextEnd < block.content.length) {
                    val textAfter = block.content.substring(lastTextEnd).trim()
                    if (textAfter.isNotEmpty()) {
                        refinedBlocks.add(ContentBlock.Text(textAfter))
                    }
                }

                if (!foundAny) {
                    refinedBlocks.add(block)
                }
            }

            is ContentBlock.Thought -> {
                val matcher = searchRegex.matcher(block.content)
                var lastEnd = 0
                var foundAny = false

                while (matcher.find()) {
                    foundAny = true
                    val start = matcher.start()
                    val end = matcher.end()
                    val query = matcher.group(1) ?: ""

                    if (start > lastEnd) {
                        val thoughtBefore = block.content.substring(lastEnd, start).trim()
                        if (thoughtBefore.isNotEmpty()) {
                            refinedBlocks.add(ContentBlock.Thought(thoughtBefore, block.isComplete))
                        }
                    }

                    refinedBlocks.add(ContentBlock.ToolCall(query))
                    lastEnd = end
                }

                if (foundAny && lastEnd < block.content.length) {
                    val thoughtAfter = block.content.substring(lastEnd).trim()
                    if (thoughtAfter.isNotEmpty()) {
                        refinedBlocks.add(ContentBlock.Thought(thoughtAfter, block.isComplete))
                    }
                }

                if (!foundAny) {
                    refinedBlocks.add(block)
                }
            }

            else -> refinedBlocks.add(block)
        }
    }

    return refinedBlocks
}

@Composable
fun RagChatBubble(
    message: RagChatMessage,
    onPdfClick: (String, Int) -> Unit = { _, _ -> },
    onImageClick: ((String) -> Unit)? = null
) {
    val bubbleDescription = if (message.isUser) "Your message" else "Assistant message"
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .semantics {
                    contentDescription = bubbleDescription
                },
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier =
                Modifier
                    .widthIn(max = 300.dp)
                    .background(
                        color =
                            if (message.isUser)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
        ) {
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
                            val imageDescription =
                                if (message.isUser) {
                                    "Image ${index + 1} attached to your message"
                                } else {
                                    "Image ${index + 1} in assistant response"
                                }
                            AsyncImage(
                                model =
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(path)
                                        .crossfade(true)
                                        .build(),
                                contentDescription = imageDescription,
                                contentScale = ContentScale.Crop,
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(if (paths.size == 1) 200.dp else 100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onImageClick?.invoke(path) }
                            )
                        }
                    }
                }

                if (message.isUser) {
                    if (message.content.isNotBlank()) {
                        Text(text = message.content, color = MaterialTheme.colorScheme.onPrimary)
                    }
                } else {
                    val blocks = parseRagContent(message.content)
                    Column {
                        blocks.forEachIndexed { index, block ->
                            when (block) {
                                is ContentBlock.Thought -> {
                                    val blockKey = message.id + index.toLong()
                                    ThinkingBlock(
                                        thought = block.content,
                                        messageId = blockKey,
                                        initiallyExpanded =
                                            block.isComplete || message.initiallyExpanded
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                is ContentBlock.Text -> {
                                    RichText { Markdown(content = block.content) }
                                }

                                is ContentBlock.ToolCall -> {
                                    Row(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .background(
                                                    MaterialTheme.colorScheme
                                                        .surfaceVariant.copy(
                                                            alpha = 0.5f
                                                        ),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme
                                                        .outlineVariant,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Tool Call",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Searching Dictionary",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = block.query,
                                                style = MaterialTheme.typography.bodySmall,
                                                color =
                                                    MaterialTheme.colorScheme
                                                        .onSurfaceVariant
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        // Display Sources/References section ONLY after generation is complete
                        if (message.references.isNotEmpty() && !message.isGenerating) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "Sources",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            message.references.forEachIndexed { index, ref ->
                                val hasValidPdf = ref.pdfFile != null
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .then(
                                                if (ref.pdfFile != null) {
                                                    Modifier.clickable {
                                                        onPdfClick(
                                                            ref.pdfFile,
                                                            ref.page
                                                        )
                                                    }
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint =
                                            if (hasValidPdf)
                                                MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "[${index + 1}] ${ref.title}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color =
                                            if (hasValidPdf)
                                                MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
