package com.example.medgem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.medgem.data.UserPreferencesRepository
import com.example.medgem.ui.components.ChatInputBar
import com.example.medgem.ui.components.ChatTopBar
import com.example.medgem.ui.components.ClearConversationDialog
import com.example.medgem.ui.components.FullScreenImageDialog
import com.example.medgem.ui.components.GenericChatBubble
import com.example.medgem.ui.components.ImageErrorBanner
import com.example.medgem.ui.components.ImagePreviewRow
import com.example.medgem.ui.components.MessageList
import com.example.medgem.ui.components.ThinkingBlock
import com.example.medgem.ui.components.rememberImagePickers
import com.example.medgem.ui.viewmodel.ChatViewModel
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import org.pytorch.executorch.extension.llm.LlmModule

private val BUBBLE_MAX_WIDTH = 300.dp

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    conversationId: Long? = null,
    llmModule: LlmModule? = null,
    isModelLoaded: Boolean = false,
    modelStatusMessage: String = "Initializing...",
    userPreferencesRepository: UserPreferencesRepository? = null,
    isReadOnly: Boolean = false
) {
    val context = LocalContext.current
    val viewModel: ChatViewModel = hiltViewModel()

    // Settings
    val temperature =
        userPreferencesRepository?.temperature?.collectAsState(initial = 0.8f)?.value ?: 0.0f
    val topP = userPreferencesRepository?.topP?.collectAsState(initial = 0.9f)?.value ?: 0.9f
    val systemPrompt =
        userPreferencesRepository?.systemPrompt?.collectAsState(
            initial = "You are a helpful medical assistant."
        )
            ?.value
            ?: ""
    val thinkingEnabled =
        userPreferencesRepository?.thinkingEnabled?.collectAsState(initial = true)?.value
            ?: true
    val visionEnabled =
        userPreferencesRepository?.visionEnabled?.collectAsState(initial = true)?.value ?: true
    val maxSequenceLength =
        userPreferencesRepository?.maxSequenceLength?.collectAsState(initial = 4096)?.value
            ?: 4096

    // Context Chat Settings
    val contextChatTemperature =
        userPreferencesRepository?.contextChatTemperature?.collectAsState(initial = 0.2f)?.value
            ?: 0.2f
    val contextChatTopP =
        userPreferencesRepository?.contextChatTopP?.collectAsState(initial = 0.9f)?.value ?: 0.9f
    val contextChatThinkingEnabled =
        userPreferencesRepository?.contextChatThinkingEnabled?.collectAsState(initial = false)?.value
            ?: false
    val contextChatSystemPrompt =
        userPreferencesRepository?.contextChatSystemPrompt?.collectAsState(
            initial = "You are a helpful medical assistant analyzing patient data. Answer questions based on the provided context."
        )
            ?.value
            ?: ""

    val messages = viewModel.messages
    val inputText by viewModel.inputTextState.collectAsState()
    val selectedImages by viewModel.selectedImagesState.collectAsState()

    LaunchedEffect(conversationId) { viewModel.syncConversationId(conversationId) }

    val isGenerating by viewModel.isGeneratingState.collectAsState()
    val isRestoring by viewModel.isRestoringState.collectAsState()
    val modelStatus by viewModel.modelStatusState.collectAsState()
    val showClearDialog by viewModel.showClearDialogState.collectAsState()
    val imageLoadError by viewModel.imageLoadErrorState.collectAsState()
    val isVoiceRecording by viewModel.isVoiceRecordingState.collectAsState()
    val isContextChat by viewModel.isContextChatState.collectAsState()

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

    // Scroll state
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

    val canClear by remember {
        derivedStateOf { messages.isNotEmpty() && !isGenerating && !isRestoring }
    }

    val lastMessageContent by remember { derivedStateOf { messages.firstOrNull()?.content ?: "" } }

    LaunchedEffect(messages.size, lastMessageContent, isGenerating) {
        if (messages.isNotEmpty() || isGenerating) {
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(conversationId, llmModule) {
        viewModel.loadConversation(
            conversationId,
            thinkingEnabled,
            contextChatSystemPrompt,
            contextChatThinkingEnabled
        )
    }

    val onSend = {
        viewModel.sendMessage(
            temperature = if (isContextChat) contextChatTemperature else temperature,
            topP = if (isContextChat) contextChatTopP else topP,
            systemPrompt = if (isContextChat) "" else systemPrompt, // Context chat system prompt is handled in loadConversation/prefill
            thinkingEnabled = if (isContextChat) contextChatThinkingEnabled else thinkingEnabled,
            maxTokens = maxSequenceLength
        )
    }

    // Clear confirmation dialog
    if (showClearDialog) {
        ClearConversationDialog(
            onConfirm = { viewModel.clearConversation() },
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
                title = "Chat",
                onBack = onBack,
                onNewChat = { viewModel.showClearDialogState.value = true },
                newChatEnabled = canClear
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Status
            if (!isModelLoaded) {
                Text(
                    text = modelStatus,
                    modifier = Modifier.padding(16.dp),
                    color =
                        if (modelStatus.startsWith("Error")) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                )
            }

            // Chat History
            MessageList(
                messages = messages,
                listState = listState,
                isGenerating = isGenerating,
                modifier = Modifier.weight(1f),
                onImageClick = { selectedImage = it },
                bubbleContent = { message ->
                    GenericChatBubble(message, onImageClick = { selectedImage = it }) {
                        val (thought, actualContent) = parseThinkingContent(message.content)
                        Column {
                            if (thought.isNotBlank()) {
                                ThinkingBlock(
                                    thought = thought,
                                    messageId = message.id,
                                    initiallyExpanded = message.initiallyExpanded
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (actualContent.isNotEmpty()) {
                                RichText { Markdown(content = actualContent) }
                            } else if (thought.isEmpty() && message.content.isNotEmpty()) {
                                RichText { Markdown(content = message.content) }
                            }
                        }
                    }
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
                        val updatedList = viewModel.selectedImagesState.value.toMutableList()
                        val removedPath = updatedList.removeAt(index)
                        try {
                            java.io.File(removedPath).delete()
                        } catch (e: Exception) {
                            android.util.Log.e("ChatScreen", "Error deleting temp image", e)
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
                    isReadOnly = isReadOnly
                )
            }
        }
    }
}

fun parseThinkingContent(content: String): Pair<String, String> {
    val startTag = "<unused94>"
    val endTag = "<unused95>"

    val startIndex = content.indexOf(startTag)

    if (startIndex == -1) {
        return Pair("", content)
    }

    val preThought = content.substring(0, startIndex)
    val afterStartTag = content.substring(startIndex + startTag.length)

    val endIndex = afterStartTag.indexOf(endTag)

    if (endIndex != -1) {
        val thought = afterStartTag.substring(0, endIndex).trim()
        val postThought = afterStartTag.substring(endIndex + endTag.length).trim()
        return Pair(thought, preThought + postThought)
    } else {
        val thought = afterStartTag.trim()
        return Pair(thought, preThought)
    }
}
