package com.example.medgem.ui.viewmodel

// Atomic imports
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medgem.ChatMessage
import com.example.medgem.LlmInferenceService
import com.example.medgem.LlmInferenceService.LlmGenerationEvent
import com.example.medgem.MedAsrService
import com.example.medgem.Route
import com.example.medgem.data.ChatImageConverter
import com.example.medgem.data.ChatRepository
import com.example.medgem.data.ContextItem
import com.example.medgem.data.ConversationType
import com.example.medgem.data.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@HiltViewModel
class ChatViewModel
@Inject
constructor(
    private val repository: ChatRepository,
    private val patientRepository: PatientRepository,
    private val llmInferenceService: LlmInferenceService,
    private val medAsrService: MedAsrService,
    private val savedStateHandle: SavedStateHandle,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    val messages = mutableStateListOf<ChatMessage>()
    val inputTextState = MutableStateFlow("")
    val selectedImagesState = MutableStateFlow<List<String>>(emptyList())
    val currentConversationId = MutableStateFlow<Long?>(null)

    val patientId: Long? = savedStateHandle.get<Long>(Route.Chat.ArgPatientId)?.takeIf { it != 0L }
    val visitIds: List<Long>? =
        savedStateHandle.get<String>(Route.Chat.ArgVisitIds)?.split(",")?.mapNotNull {
            it.trim().toLongOrNull()
        }

    private val _isGeneratingState = MutableStateFlow(false)
    val isGeneratingState: StateFlow<Boolean> = _isGeneratingState.asStateFlow()
    private val _isRestoringState = MutableStateFlow(false)
    val isRestoringState: StateFlow<Boolean> = _isRestoringState.asStateFlow()
    private val _isVoiceRecordingState = MutableStateFlow(false)
    val isVoiceRecordingState: StateFlow<Boolean> = _isVoiceRecordingState.asStateFlow()
    val modelStatusState = MutableStateFlow("Initializing...")
    val isStoppedState = AtomicBoolean(false)
    val showClearDialogState = MutableStateFlow(false)
    val imageLoadErrorState = MutableStateFlow<String?>(null)
    private val _hasStartedConversationState = MutableStateFlow(false)
    val hasStartedConversationState: StateFlow<Boolean> = _hasStartedConversationState.asStateFlow()
    private val _isContextChatState = MutableStateFlow(false)
    val isContextChatState: StateFlow<Boolean> = _isContextChatState.asStateFlow()
    private val _previousRunFinishedWithEOSState = MutableStateFlow(true)
    val previousRunFinishedWithEOSState: StateFlow<Boolean> =
        _previousRunFinishedWithEOSState.asStateFlow()
    private val _systemPromptTurnOpen = MutableStateFlow(false)

    private val messageIdGenerator = AtomicLong(System.nanoTime())
    // inferenceDispatcher removed as Service manages its own threading

    override fun onCleared() {
        super.onCleared()
        if (_isVoiceRecordingState.value) {
            medAsrService.stopAndRelease()
            _isVoiceRecordingState.value = false
        }
    }

    fun syncConversationId(conversationId: Long?) {
        if (currentConversationId.value != conversationId) {
            currentConversationId.value = conversationId
            messages.clear()
            inputTextState.value = ""
            selectedImagesState.value = emptyList()
            _isGeneratingState.value = false
            _isRestoringState.value = false
            isStoppedState.set(false)
            showClearDialogState.value = false
            imageLoadErrorState.value = null
            _hasStartedConversationState.value = false
            _isContextChatState.value = false
            _previousRunFinishedWithEOSState.value = true
            _systemPromptTurnOpen.value = false
        }
    }

    fun loadConversation(
        conversationId: Long?,
        thinkingEnabled: Boolean = false,
        contextChatSystemPrompt: String = "",
        contextChatThinkingEnabled: Boolean = false
    ) {
        if (conversationId == null) {
            resetForNewConversation()
            // Only inject context when visit data is explicitly selected ("Chat with Context")
            if (patientId != null && !visitIds.isNullOrEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val patient = patientRepository.getPatient(patientId)
                        if (patient != null) {
                            val contextItems = mutableListOf<ContextItem>()
                            val visitDates = mutableListOf<String>()

                            // 1. Patient Info
                            val patientInfo = StringBuilder()
                            patientInfo.append(
                                "You are assisting with a patient named ${patient.name} (${patient.age}y, ${patient.gender}).\n"
                            )

                            if (patient.allergies.isNotEmpty()) {
                                patientInfo.append("Allergies: ${patient.allergies.joinToString(", ")}\n")
                            }
                            if (patient.chronicConditions.isNotEmpty()) {
                                patientInfo.append(
                                    "Chronic Conditions: ${
                                        patient.chronicConditions.joinToString(
                                            ", "
                                        )
                                    }\n"
                                )
                            }
                            if (patient.currentMedications.isNotEmpty()) {
                                patientInfo.append(
                                    "Current Medications: ${
                                        patient.currentMedications.joinToString(
                                            ", "
                                        )
                                    }\n"
                                )
                            }
                            contextItems.add(ContextItem.Text(patientInfo.toString()))

                            // 2. Visit Info
                            if (!visitIds.isNullOrEmpty()) {
                                contextItems.add(ContextItem.Text("Attached Visit Data:\n"))
                                visitIds.forEach { vid ->
                                    val visit = patientRepository.getVisit(vid)
                                    if (visit != null) {
                                        val dateStr =
                                            SimpleDateFormat(
                                                "MMM dd, yyyy",
                                                Locale.getDefault()
                                            )
                                                .format(Date(visit.date))
                                        visitDates.add(dateStr)

                                        val visitText = StringBuilder()
                                        visitText.append("--- Visit Date: $dateStr ---\n")
                                        if (visit.generatedNote.isNotBlank()) {
                                            visitText.append(
                                                "Summary: ${visit.generatedNote}\n"
                                            )
                                        }
                                        if (visit.rawTranscript.isNotBlank()) {
                                            visitText.append(
                                                "Transcript: ${visit.rawTranscript}\n"
                                            )
                                        }
                                        contextItems.add(ContextItem.Text(visitText.toString()))

                                        // Handle Visit Images
                                        if (visit.imagePaths.isNotEmpty()) {
                                            contextItems.add(ContextItem.Text("\nImages from this visit:\n"))
                                            visit.imagePaths.forEachIndexed { index, originalPath ->
                                                val copiedPath =
                                                    copyImageToChatStorage(originalPath)
                                                if (copiedPath != null) {
                                                    contextItems.add(ContextItem.Text("Image ${index + 1}: "))
                                                    contextItems.add(ContextItem.Image(copiedPath))
                                                    contextItems.add(ContextItem.Text("\n"))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            withContext(Dispatchers.Main) {
                                modelStatusState.value = "Injecting patient context..."
                            }

                            // 3. System Instruction & Wrapper
                            val effectiveContextItems = mutableListOf<ContextItem>()

                            // Use Context Chat specific settings
                            // Prepend system prompt (with thinking instruction if enabled)
                            var systemInstruction = contextChatSystemPrompt
                            if (contextChatThinkingEnabled) {
                                systemInstruction =
                                    "SYSTEM INSTRUCTION: think silently if needed. $systemInstruction"
                            }

                            if (systemInstruction.isNotBlank()) {
                                effectiveContextItems.add(ContextItem.Text("$systemInstruction\n"))
                            }

                            effectiveContextItems.addAll(contextItems)

                            val chatTitle =
                                if (visitDates.size == 1) {
                                    "${patient.name} - ${visitDates.first()}"
                                } else {
                                    val allButLast =
                                        visitDates.dropLast(1).joinToString(", ")
                                    "${patient.name} - $allButLast & ${visitDates.last()}"
                                }

                            // Create conversation and persist context
                            val convId =
                                repository.createConversation(
                                    title = chatTitle,
                                    type = ConversationType.CHAT,
                                    patientId = patientId,
                                    contextItems = effectiveContextItems
                                )
                            currentConversationId.value = convId

                            // Wait for model readiness before prefilling
                            val module = llmInferenceService.getModule()
                            if (module != null) {
                                // Start of turn
                                llmInferenceService.prefillPrompt("<start_of_turn>user\n", 1)
                                // Inject all items
                                llmInferenceService.prefillContext(effectiveContextItems)
                                // End of turn
                                llmInferenceService.prefillPrompt("\n\n", 0)
                            }

                            withContext(Dispatchers.Main) {
                                if (module != null) {
                                    modelStatusState.value = "Model Loaded. Context Injected."
                                } else {
                                    modelStatusState.value = "Model not ready. Context saved."
                                }
                                _hasStartedConversationState.value = true
                                _isContextChatState.value = true
                                _previousRunFinishedWithEOSState.value = true
                                _systemPromptTurnOpen.value = true
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "Error injecting patient context", e)
                        withContext(Dispatchers.Main) { modelStatusState.value = "Model Loaded." }
                    }
                }
            }
            return
        }

        currentConversationId.value = conversationId
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val savedMessages = repository.getMessages(conversationId)
                val conversation = repository.getConversation(conversationId)
                val contextJson = conversation?.contextJson
                val contextItems =
                    if (contextJson != null) ContextItem.fromJson(contextJson) else emptyList()

                val loadedMessages =
                    savedMessages.map { entity ->
                        val json = entity.imagePathsJson
                        val paths =
                            if (!json.isNullOrBlank()) {
                                ChatImageConverter.jsonArrayToPaths(json)
                            } else {
                                emptyList()
                            }

                        ChatMessage(
                            id = entity.id,
                            content = entity.content,
                            isUser = entity.isUser,
                            imagePaths = paths
                        )
                    }

                withContext(Dispatchers.Main) {
                    messages.clear()
                    messages.addAll(loadedMessages.reversed())
                    if (loadedMessages.isNotEmpty() || contextItems.isNotEmpty()) {
                        _hasStartedConversationState.value = true
                        if (contextItems.isNotEmpty()) {
                            _isContextChatState.value = true
                        }
                        _previousRunFinishedWithEOSState.value = true
                    }
                }

                if (loadedMessages.isNotEmpty() || contextItems.isNotEmpty()) {
                    _isRestoringState.value = true
                    viewModelScope.launch(
                        Dispatchers.IO
                    ) { // Use IO, service handles its own scope if needed, but here we call suspend
                        // functions
                        try {
                            withContext(Dispatchers.Main) {
                                modelStatusState.value = "Restoring conversation history..."
                            }

                            llmInferenceService.reset()

                            var numBos = 1
                            var sysPromptOpen = false

                            // 1. Inject Context Items (includes thinking prefix if it was saved in context)
                            // If context items exist, they form the initial system/context prompt.
                            if (contextItems.isNotEmpty()) {
                                llmInferenceService.prefillPrompt("<start_of_turn>user\n", numBos)
                                numBos = 0 // Consumed BOS
                                llmInferenceService.prefillContext(contextItems)
                                // We assume context items end with the setup, so we close the user turn or leave it open?
                                // In creation logic, we did: prefillPrompt("\n\n", 0) and left sysPromptOpen=true logically.
                                // Let's try to match that behavior.
                                llmInferenceService.prefillPrompt("\n\n", 0)
                                sysPromptOpen = true
                            }

                            for (msg in loadedMessages) {
                                if (msg.isUser) {
                                    if (sysPromptOpen) {
                                        // First user msg continues the system prompt turn
                                        if (msg.imagePaths.isNotEmpty()) {
                                            llmInferenceService.prefillImages(msg.imagePaths)
                                        }
                                        val finalContent = msg.content + "<end_of_turn>\n"
                                        llmInferenceService.prefillPrompt(finalContent, 0)
                                        sysPromptOpen = false
                                    } else {
                                        val startTurn = "<start_of_turn>user\n"
                                        llmInferenceService.prefillPrompt(startTurn, numBos)

                                        if (msg.imagePaths.isNotEmpty()) {
                                            llmInferenceService.prefillImages(msg.imagePaths)
                                        }
                                        val finalContent = msg.content + "<end_of_turn>\n"
                                        llmInferenceService.prefillPrompt(finalContent, 0)
                                    }
                                } else {
                                    if (sysPromptOpen) {
                                        // Close open system prompt turn before model response
                                        llmInferenceService.prefillPrompt("<end_of_turn>\n", 0)
                                        sysPromptOpen = false
                                    }
                                    val modelTurn =
                                        "<start_of_turn>model\n" +
                                                msg.content +
                                                "<end_of_turn>\n"
                                    llmInferenceService.prefillPrompt(modelTurn, numBos)
                                }
                                numBos = 0 // BOS only for very first prompt
                            }

                            // If system prompt open with no messages, keep it open for sendMessage
                            if (sysPromptOpen) {
                                withContext(Dispatchers.Main) { _systemPromptTurnOpen.value = true }
                            }

                            withContext(Dispatchers.Main) {
                                modelStatusState.value = "Model Loaded. Ready to chat."
                                _isRestoringState.value = false
                            }
                        } catch (e: Exception) {
                            Log.e("ChatViewModel", "Failed to restore context", e)
                            withContext(Dispatchers.Main) {
                                modelStatusState.value = "Error restoring context"
                                _isRestoringState.value = false
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error loading messages", e)
            }
        }
    }

    fun sendMessage(
        temperature: Float,
        topP: Float,
        systemPrompt: String,
        thinkingEnabled: Boolean,
        maxTokens: Int
    ) {
        if (_isGeneratingState.value) return
        if (inputTextState.value.isBlank() && selectedImagesState.value.isEmpty()) return

        Log.d(
            "ChatViewModel",
            "Starting generation with: temperature=$temperature, topP=$topP, thinkingEnabled=$thinkingEnabled, maxTokens=$maxTokens"
        )

        val userText = inputTextState.value
        val userImages = selectedImagesState.value
        val userMessageId = messageIdGenerator.incrementAndGet()
        val responseId = messageIdGenerator.incrementAndGet()

        messages.add(0, ChatMessage(id = userMessageId, content = userText, isUser = true))
        inputTextState.value = ""
        selectedImagesState.value = emptyList()
        _isGeneratingState.value = true
        isStoppedState.set(false)

        // Compute effective system prompt (with thinking prefix) for both storage and prefill
        val effectiveSystemPrompt =
            if (thinkingEnabled && systemPrompt.isNotBlank()) {
                "SYSTEM INSTRUCTION: think silently if needed. $systemPrompt"
            } else {
                systemPrompt
            }

        // 1. Save messages and images
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val convId =
                    currentConversationId.value
                        ?: run {
                            val contextItems = if (effectiveSystemPrompt.isNotBlank()) {
                                listOf(ContextItem.Text(effectiveSystemPrompt))
                            } else {
                                emptyList()
                            }
                            val newId =
                                repository.createConversation(
                                    type = ConversationType.CHAT,
                                    patientId = patientId ?: 0L,
                                    contextItems = contextItems
                                )
                            currentConversationId.value = newId
                            newId
                        }

                val messageId =
                    repository.saveMessage(
                        conversationId = convId,
                        content = userText,
                        isUser = true,
                        imagePaths = userImages
                    )

                // Reload message to get paths
                // We need paths for LLM inference
                val savedMessageEntity = repository.getMessages(convId).find { it.id == messageId }
                val savedPaths =
                    savedMessageEntity?.imagePathsJson?.let {
                        ChatImageConverter.jsonArrayToPaths(it)
                    }
                        ?: emptyList()

                withContext(Dispatchers.Main) {
                    // Update UI with paths
                    val index = messages.indexOfFirst { it.id == userMessageId }
                    if (index != -1) {
                        messages[index] = messages[index].copy(imagePaths = savedPaths)
                    }

                    messages.add(
                        0,
                        ChatMessage(
                            id = responseId,
                            content = "",
                            isUser = false,
                            initiallyExpanded = true
                        )
                    )
                }

                val wasPreviousRunClean = _previousRunFinishedWithEOSState.value
                _previousRunFinishedWithEOSState.value = false

                // 2. Determine Prompts
                val currentNumBos = if (!hasStartedConversationState.value) 1 else 0
                val preImagePrompt =
                    if (_systemPromptTurnOpen.value) {
                        // System prompt turn is already open, continue it
                        _systemPromptTurnOpen.value = false
                        ""
                    } else if (!_hasStartedConversationState.value) {
                        _hasStartedConversationState.value = true

                        if (effectiveSystemPrompt.isNotBlank()) {
                            "<start_of_turn>user\n$effectiveSystemPrompt\n\n"
                        } else {
                            "<start_of_turn>user\n"
                        }
                    } else {
                        val prefix = if (wasPreviousRunClean) "\n" else "<end_of_turn>\n"
                        prefix + "<start_of_turn>user\n"
                    }

                val prompt = "$userText<end_of_turn>\n<start_of_turn>model\n"

                // 3. Generate Response
                llmInferenceService.generateResponseFlow(
                    prompt = prompt,
                    preImagePrompt = preImagePrompt,
                    images = savedPaths,
                    maxTokens = maxTokens,
                    numBos = currentNumBos,
                    temperature = temperature,
                    topP = topP
                )
                    .collect { event ->
                        if (isStoppedState.get()) return@collect

                        when (event) {
                            is LlmGenerationEvent.Content -> {
                                val result = event.text
                                withContext(Dispatchers.Main) {
                                    val index = messages.indexOfFirst { it.id == responseId }
                                    if (index != -1) {
                                        val currentMsg = messages[index]
                                        messages[index] =
                                            currentMsg.copy(
                                                content = currentMsg.content + result
                                            )
                                    }
                                }
                            }

                            is LlmGenerationEvent.Stats -> {
                                Log.d(
                                    "ChatViewModel",
                                    "Stats: generated=${event.generatedTokens}, prompt=${event.promptTokens}"
                                )
                            }

                            is LlmGenerationEvent.Done -> {
                                _previousRunFinishedWithEOSState.value = true
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error generating response", e)
                withContext(Dispatchers.Main) {
                    val index = messages.indexOfFirst { it.id == responseId }
                    val errorMsg = "Error: ${e.message}"
                    if (index != -1) {
                        messages[index] = messages[index].copy(content = errorMsg)
                    } else {
                        messages.add(0, ChatMessage(content = errorMsg, isUser = false))
                    }
                }
            } finally {
                withContext(Dispatchers.Main) { _isGeneratingState.value = false }
                // Save assistant response
                val convId = currentConversationId.value
                if (convId != null) {
                    val index = messages.indexOfFirst { it.id == responseId }
                    if (index != -1) {
                        val assistantContent = messages[index].content
                        if (assistantContent.isNotBlank()) {
                            try {
                                repository.saveMessage(convId, assistantContent, false)
                            } catch (e: Exception) {
                                Log.e("ChatViewModel", "Error saving assistant response", e)
                            }
                        }
                    }
                }
            }
        }
    }

    fun stopGeneration() {
        isStoppedState.set(true)
        viewModelScope.launch { llmInferenceService.stop() }
        _isGeneratingState.value = false
    }

    fun startVoiceInput() {
        if (_isVoiceRecordingState.value) return
        _isVoiceRecordingState.value = true
        viewModelScope.launch {
            val started = medAsrService.startRecording()
            if (!started) {
                withContext(Dispatchers.Main) { _isVoiceRecordingState.value = false }
            }
        }
    }

    fun stopVoiceInput() {
        if (!_isVoiceRecordingState.value) return
        viewModelScope.launch {
            val result = medAsrService.stopRecording()
            withContext(Dispatchers.Main) {
                if (!result.isNullOrBlank()) {
                    inputTextState.value = result
                }
                _isVoiceRecordingState.value = false
            }
        }
    }

    fun clearConversation() {
        viewModelScope.launch {
            try {
                llmInferenceService.reset()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error resetting context", e)
            }
        }

        messages.clear()
        currentConversationId.value = null
        _hasStartedConversationState.value = false
        _previousRunFinishedWithEOSState.value = true
        _systemPromptTurnOpen.value = false
        showClearDialogState.value = false
        Log.d("ChatViewModel", "Conversation cleared and LLM context reset")
    }

    private fun resetForNewConversation() {
        viewModelScope.launch {
            try {
                llmInferenceService.reset()
                Log.d("ChatViewModel", "Context reset for new conversation")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error resetting context", e)
            }
        }
    }

    private fun copyImageToChatStorage(originalPath: String): String? {
        try {
            val sourceFile = File(originalPath)
            if (!sourceFile.exists()) return null

            val chatImagesDir = File(context.filesDir, "chat_context_images")
            if (!chatImagesDir.exists()) chatImagesDir.mkdirs()

            // Create unique filename to avoid collisions
            val extension = sourceFile.extension.ifEmpty { "jpg" }
            val newFilename = "CTX_${UUID.randomUUID()}.$extension"
            val targetFile = File(chatImagesDir, newFilename)

            sourceFile.copyTo(targetFile, overwrite = true)
            return targetFile.absolutePath
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error copying image to chat storage", e)
            return null
        }
    }
}
