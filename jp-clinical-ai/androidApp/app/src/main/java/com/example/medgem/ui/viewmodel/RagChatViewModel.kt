package com.example.medgem.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medgem.LlmInferenceService
import com.example.medgem.LlmInferenceService.LlmGenerationEvent
import com.example.medgem.MedAsrService
import com.example.medgem.RAG_FEWSHOT_PROMPT
import com.example.medgem.RAG_SYSTEM_PROMPT
import com.example.medgem.RagChatMessage
import com.example.medgem.RagContextManager
import com.example.medgem.Route
import com.example.medgem.data.ChatImageConverter
import com.example.medgem.data.ChatRepository
import com.example.medgem.data.ConversationType
import com.example.medgem.data.RagSourceReference
import com.example.medgem.data.jsonToReferences
import com.example.medgem.data.referencesToJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class RagChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val llmInferenceService: LlmInferenceService,
    private val ragContextManager: RagContextManager,
    private val medAsrService: MedAsrService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val messages = mutableStateListOf<RagChatMessage>()
    val inputTextState = MutableStateFlow("")
    val selectedImagesState = MutableStateFlow<List<String>>(emptyList())
    val currentConversationId = MutableStateFlow<Long?>(null)

    val patientId: Long? =
        savedStateHandle.get<Long>(Route.RagChat.ArgPatientId)?.takeIf { it != 0L }

    private val _isGeneratingState = MutableStateFlow(false)
    val isGeneratingState: StateFlow<Boolean> = _isGeneratingState.asStateFlow()
    private val _isVoiceRecordingState = MutableStateFlow(false)
    val isVoiceRecordingState: StateFlow<Boolean> = _isVoiceRecordingState.asStateFlow()
    val currentSearchQueryState = MutableStateFlow<String?>(null)
    val modelStatusState = MutableStateFlow("Initializing...")
    val isStoppedState = AtomicBoolean(false)
    private val _isRestoringState = MutableStateFlow(false)
    val isRestoringState: StateFlow<Boolean> = _isRestoringState.asStateFlow()
    val isInternalUpdate = AtomicBoolean(false)
    val showClearDialogState = MutableStateFlow(false)
    val imageLoadErrorState = MutableStateFlow<String?>(null)
    private val _hasStartedConversationState = MutableStateFlow(false)
    val hasStartedConversationState: StateFlow<Boolean> = _hasStartedConversationState.asStateFlow()
    private val _previousRunFinishedWithEOSState = MutableStateFlow(true)
    val previousRunFinishedWithEOSState: StateFlow<Boolean> =
        _previousRunFinishedWithEOSState.asStateFlow()

    private val messageIdGenerator = AtomicLong(System.nanoTime())
    // inferenceDispatcher removed

    override fun onCleared() {
        super.onCleared()
        if (_isVoiceRecordingState.value) {
            medAsrService.stopAndRelease()
            _isVoiceRecordingState.value = false
        }
    }

    fun syncConversationId(conversationId: Long?) {
        // Prevent clearing state if we are in an active "New Chat" session (conversationId arg is null)
        // but have already established an internal ID and messages.
        // This happens when returning from PDF view or other screens before the route is updated.
        if (conversationId == null && currentConversationId.value != null && messages.isNotEmpty()) {
            return
        }

        if (currentConversationId.value != conversationId) {
            currentConversationId.value = conversationId
            messages.clear()
            inputTextState.value = ""
            selectedImagesState.value = emptyList()
            _isGeneratingState.value = false
            currentSearchQueryState.value = null
            modelStatusState.value = "Initializing..."
            isStoppedState.set(false)
            _isRestoringState.value = false
            isInternalUpdate.set(false)
            showClearDialogState.value = false
            imageLoadErrorState.value = null
            _hasStartedConversationState.value = false
            _previousRunFinishedWithEOSState.value = true
        }
    }

    fun loadConversation(
        conversationId: Long?,
        lastLoadedConversationId: Long?,
        onConversationLoaded: (Long?) -> Unit
    ) {
        if (conversationId != null && isInternalUpdate.getAndSet(false)) {
            return
        }

        if (conversationId == null) {
            // Guard: Don't reset if we already have an active conversation with messages.
            // This happens when returning from PDF viewer before the route URL is updated.
            if (currentConversationId.value != null && messages.isNotEmpty()) {
                return
            }
            resetForNewConversation(onConversationLoaded)
            return
        }

        currentConversationId.value = conversationId
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val savedMessages = repository.getMessages(conversationId)
                val loadedMessages =
                    savedMessages.map { entity ->
                        val json = entity.imagePathsJson
                        val paths = if (!json.isNullOrBlank()) {
                            ChatImageConverter.jsonArrayToPaths(json)
                        } else {
                            emptyList()
                        }

                        RagChatMessage(
                            id = entity.id,
                            content = entity.content,
                            isUser = entity.isUser,
                            imagePaths = paths,
                            references = jsonToReferences(entity.referencesJson)
                        )
                    }

                withContext(Dispatchers.Main) {
                    messages.clear()
                    messages.addAll(loadedMessages.reversed())
                    if (loadedMessages.isNotEmpty()) {
                        _hasStartedConversationState.value = true
                        _previousRunFinishedWithEOSState.value = true
                    }
                }

                if (loadedMessages.isNotEmpty()) {
                    _isRestoringState.value = true

                    if (lastLoadedConversationId == conversationId) {
                        withContext(Dispatchers.Main) {
                            modelStatusState.value = "Model Ready (Cached context)"
                            _isRestoringState.value = false
                            onConversationLoaded(conversationId)
                        }
                        return@launch
                    }

                    try {
                        withContext(Dispatchers.Main) {
                            modelStatusState.value = "Restoring conversation history..."
                        }

                        llmInferenceService.reset()
                        var isFirstTurn = true

                        for (msg in loadedMessages) {
                            val numBos = if (isFirstTurn) 1 else 0

                            if (msg.isUser) {
                                if (isFirstTurn) {
                                    val prefix =
                                        RAG_FEWSHOT_PROMPT
                                            .replace("{system_prompt}", RAG_SYSTEM_PROMPT.trim())
                                            .substringBefore("<start_of_turn>user\n{user_input}")

                                    llmInferenceService.prefillPrompt(prefix, numBos)
                                }

                                val startTurn = "<start_of_turn>user\n"
                                llmInferenceService.prefillPrompt(startTurn, 0)

                                if (msg.imagePaths.isNotEmpty()) {
                                    llmInferenceService.prefillImages(msg.imagePaths)
                                }

                                val content = msg.content + "<end_of_turn>\n"
                                llmInferenceService.prefillPrompt(content, 0)
                                isFirstTurn = false
                            } else {
                                val modelTurn =
                                    "<start_of_turn>model\n" + msg.content + "<end_of_turn>\n"
                                llmInferenceService.prefillPrompt(modelTurn, 0)
                            }
                        }

                        withContext(Dispatchers.Main) {
                            modelStatusState.value = "Model Loaded. Ready to chat."
                            _isRestoringState.value = false
                            onConversationLoaded(conversationId)
                        }
                    } catch (e: Exception) {
                        Log.e("RagChatViewModel", "Failed to restore context", e)
                        withContext(Dispatchers.Main) {
                            modelStatusState.value = "Error restoring context"
                            _isRestoringState.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RagChatViewModel", "Error restoring messages", e)
            }
        }
    }

    fun sendMessage(
        context: Context,
        temperature: Float,
        topP: Float,
        maxSearchSteps: Int, // e.g. 5
        maxContextTokens: Int,
        maxRetrievedDocs: Int,
        minRetrievalScore: Float,
        maxTokens: Int,
        onConversationLoaded: (Long?) -> Unit
    ) {
        if (_isGeneratingState.value) return
        if (inputTextState.value.isBlank() && selectedImagesState.value.isEmpty()) return

        Log.d(
            "RagChatViewModel",
            "Starting generation with: temperature=$temperature, topP=$topP, maxSearchSteps=$maxSearchSteps"
        )

        val userText = inputTextState.value
        val userImages = selectedImagesState.value
        val userMessageId = messageIdGenerator.incrementAndGet()

        messages.add(
            0,
            RagChatMessage(
                id = userMessageId,
                content = userText,
                isUser = true
            )
        )
        inputTextState.value = ""
        selectedImagesState.value = emptyList()
        _isGeneratingState.value = true
        isStoppedState.set(false)
        currentSearchQueryState.value = null

        viewModelScope.launch {
            val finalResponseId = messageIdGenerator.incrementAndGet()
            try {
                val convId =
                    currentConversationId.value
                        ?: run {
                            val newId = repository.createConversation(
                                type = ConversationType.RAG,
                                patientId = patientId ?: 0L
                            )
                            isInternalUpdate.set(true)
                            currentConversationId.value = newId
                            withContext(Dispatchers.Main) {
                                onConversationLoaded(newId)
                            }
                            newId
                        }

                // Save user message (Pass paths directly)
                val messageId = repository.saveMessage(
                    conversationId = convId,
                    content = userText,
                    isUser = true,
                    imagePaths = userImages
                )

                // Retrieve paths from saved message
                val savedMessageEntity = repository.getMessages(convId).find { it.id == messageId }
                val savedPaths =
                    savedMessageEntity?.imagePathsJson?.let { ChatImageConverter.jsonArrayToPaths(it) }
                        ?: emptyList()

                withContext(Dispatchers.Main) {
                    val index = messages.indexOfFirst { it.id == userMessageId }
                    if (index != -1) {
                        messages[index] = messages[index].copy(imagePaths = savedPaths)
                    }

                    messages.add(
                        0,
                        RagChatMessage(
                            id = finalResponseId,
                            content = "",
                            isUser = false,
                            initiallyExpanded = true,
                            isGenerating = true
                        )
                    )
                }

                var currentStep = 0
                val allReferences = mutableListOf<RagSourceReference>()
                var currentPrompt = "$userText<end_of_turn>\n<start_of_turn>model\n"

                // Determine preImagePrompt (User Start)
                var currentNumBos = 0
                val preImagePrompt: String

                val wasPreviousRunClean = _previousRunFinishedWithEOSState.value
                _previousRunFinishedWithEOSState.value = false

                if (!_hasStartedConversationState.value) {
                    _hasStartedConversationState.value = true
                    val fewshotPrefix =
                        RAG_FEWSHOT_PROMPT
                            .replace("{system_prompt}", RAG_SYSTEM_PROMPT.trim())
                            .substringBefore("<start_of_turn>user\n{user_input}")

                    preImagePrompt = fewshotPrefix + "<start_of_turn>user\n"
                    currentNumBos = 1
                } else {
                    val prefix = if (wasPreviousRunClean) "\n" else "<end_of_turn>\n"
                    preImagePrompt = prefix + "<start_of_turn>user\n"
                    currentNumBos = 0
                }

                // RAG Loop
                while (currentStep <= maxSearchSteps) {
                    if (isStoppedState.get()) break

                    val fullResponseBuilder = StringBuilder()
                    val searchRegex = Pattern.compile(
                        "\\*\\*Search Query:\\*\\*\\s*\"([^\"]*)\"",
                        Pattern.DOTALL or Pattern.CASE_INSENSITIVE
                    )
                    var searchDetected = false
                    var searchQuery = ""


                    val iterImages = if (currentStep == 0) savedPaths else emptyList()
                    val iterPrePrompt = if (currentStep == 0) preImagePrompt else ""
                    val iterNumBos = if (currentStep == 0) currentNumBos else 0

                    try {
                        llmInferenceService.generateResponseFlow(
                            prompt = currentPrompt,
                            preImagePrompt = iterPrePrompt,
                            images = iterImages,
                            maxTokens = maxTokens,
                            numBos = iterNumBos,
                            temperature = temperature,
                            topP = topP
                        ).collect { event ->
                            if (isStoppedState.get()) throw java.util.concurrent.CancellationException(
                                "Stopped"
                            )

                            when (event) {
                                is LlmGenerationEvent.Content -> {
                                    val result = event.text
                                    fullResponseBuilder.append(result)
                                    withContext(Dispatchers.Main) {
                                        val idx = messages.indexOfFirst { it.id == finalResponseId }
                                        if (idx != -1) {
                                            messages[idx] =
                                                messages[idx].copy(content = messages[idx].content + result)
                                        }
                                    }

                                    val matcher =
                                        searchRegex.matcher(fullResponseBuilder.toString())
                                    if (matcher.find()) {
                                        searchQuery = matcher.group(1)!!.trim()
                                        searchDetected = true
                                        throw java.util.concurrent.CancellationException("SearchDetected")
                                    }
                                }

                                is LlmGenerationEvent.Stats -> {
                                    Log.d(
                                        "RagChatViewModel",
                                        "Stats: generated=${event.generatedTokens}, prompt=${event.promptTokens}"
                                    )
                                }

                                is LlmGenerationEvent.Done -> {
                                    _previousRunFinishedWithEOSState.value = true
                                }
                            }
                        }
                    } catch (e: java.util.concurrent.CancellationException) {
                        if (e.message == "SearchDetected") {
                            // Handle Search
                            llmInferenceService.stop() // Ensure generation stops

                            Log.d("RagChatViewModel", "Search detected: $searchQuery")
                            withContext(Dispatchers.Main) {
                                currentSearchQueryState.value = searchQuery
                            }

                            val searchResults = ragContextManager.performSearch(
                                searchQuery,
                                maxRetrievedDocs,
                                minRetrievalScore,
                                maxContextTokens
                            )
                            allReferences.addAll(ragContextManager.extractReferences(searchResults))

                            withContext(Dispatchers.Main) {
                                currentSearchQueryState.value = null
                                val idx = messages.indexOfFirst { it.id == finalResponseId }
                                if (idx != -1) {
                                    messages[idx] =
                                        messages[idx].copy(references = allReferences.toList())
                                }
                            }

                            val toolResponse =
                                ragContextManager.buildToolResponse(searchResults)
                            currentPrompt =
                                "<end_of_turn>\n<start_of_turn>user\n$toolResponse<end_of_turn>\n<start_of_turn>model\n"
                            currentStep++
                            continue // Loop again with new prompt
                        } else {
                            Log.d("RagChatViewModel", "Generation stopped")
                            break // Stopped
                        }
                    } catch (e: Exception) {
                        Log.e("RagChatViewModel", "Error in generation", e)
                        break
                    }

                    // If we finished collection normally (Done), break logic
                    if (!searchDetected) {
                        Log.d("RagChatViewModel", "Generation finished (Done)")
                        break
                    }
                }

                val index = messages.indexOfFirst { it.id == finalResponseId }
                if (index != -1) {
                    val content = messages[index].content
                    if (content.isNotBlank()) {
                        val referencesJson = referencesToJson(messages[index].references)
                        repository.saveMessage(
                            conversationId = convId,
                            content = content,
                            isUser = false,
                            referencesJson = referencesJson
                        )
                    }
                }
            } catch (t: Throwable) {
                if (t !is java.util.concurrent.CancellationException) {
                    Log.e("RagChatViewModel", "Error in RAG generation: ${t.message}", t)
                    withContext(Dispatchers.Main) {
                        val errorMsg = "Error: ${t.message ?: "Unknown error"}"
                        val index = messages.indexOfFirst { it.id == finalResponseId }
                        if (index != -1) {
                            messages[index] = messages[index].copy(content = errorMsg)
                        } else {
                            messages.add(0, RagChatMessage(content = errorMsg, isUser = false))
                        }
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isGeneratingState.value = false
                    currentSearchQueryState.value = null
                    val index = messages.indexOfFirst { it.id == finalResponseId }
                    if (index != -1) {
                        messages[index] = messages[index].copy(isGenerating = false)
                    }
                }
            }
        }
    }

    fun stopGeneration() {
        isStoppedState.set(true)
        viewModelScope.launch {
            llmInferenceService.stop()
        }
        _isGeneratingState.value = false
    }

    fun startVoiceInput() {
        if (_isVoiceRecordingState.value) return
        _isVoiceRecordingState.value = true
        viewModelScope.launch {
            val started = medAsrService.startRecording()
            if (!started) {
                withContext(Dispatchers.Main) {
                    _isVoiceRecordingState.value = false
                }
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

    fun clearConversation(onConversationLoaded: (Long?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                llmInferenceService.reset()
            } catch (e: Exception) {
                Log.e("RagChatViewModel", "Error resetting context", e)
            }

            withContext(Dispatchers.Main) {
                messages.clear()
                _hasStartedConversationState.value = false
                currentConversationId.value = null
                showClearDialogState.value = false
                onConversationLoaded(null)
            }
        }
    }

    private fun resetForNewConversation(
        onConversationLoaded: (Long?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                llmInferenceService.reset()
                withContext(Dispatchers.Main) {
                    _hasStartedConversationState.value = false
                    messages.clear()
                    onConversationLoaded(null)
                }
            } catch (e: Exception) {
                Log.e("RagChatViewModel", "Error resetting context", e)
            }
        }
    }
}
