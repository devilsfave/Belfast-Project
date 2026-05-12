package com.example.medgem.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medgem.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Global LLM Settings
    val temperature = userPreferencesRepository.temperature
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.8f)
    val topP = userPreferencesRepository.topP
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.9f)
    val systemPrompt = userPreferencesRepository.systemPrompt
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserPreferencesRepository.DEFAULT_SYSTEM_PROMPT
        )
    val thinkingEnabled = userPreferencesRepository.thinkingEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val visionEnabled = userPreferencesRepository.visionEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val prefillChunkSize = userPreferencesRepository.prefillChunkSize
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1024)
    val maxSequenceLength = userPreferencesRepository.maxSequenceLength
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4096)

    // RAG Settings
    val ragTemperature = userPreferencesRepository.ragTemperature
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.8f)
    val ragTopP = userPreferencesRepository.ragTopP
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.9f)
    val maxSearchSteps = userPreferencesRepository.maxSearchSteps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    val maxContextTokens = userPreferencesRepository.maxContextTokens
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2048)
    val maxRetrievedDocs = userPreferencesRepository.maxRetrievedDocs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    val minRetrievalScore = userPreferencesRepository.minRetrievalScore
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)

    // SOAP Settings
    val soapTemperature = userPreferencesRepository.soapTemperature
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.2f)
    val soapTopP = userPreferencesRepository.soapTopP
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.9f)
    val soapSystemPrompt = userPreferencesRepository.soapSystemPrompt
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserPreferencesRepository.DEFAULT_SOAP_SYSTEM_PROMPT
        )
    val soapThinkingEnabled = userPreferencesRepository.soapThinkingEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Context Chat Settings
    val contextChatTemperature = userPreferencesRepository.contextChatTemperature
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.2f)
    val contextChatTopP = userPreferencesRepository.contextChatTopP
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.9f)
    val contextChatThinkingEnabled = userPreferencesRepository.contextChatThinkingEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val contextChatSystemPrompt = userPreferencesRepository.contextChatSystemPrompt
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserPreferencesRepository.DEFAULT_CONTEXT_CHAT_SYSTEM_PROMPT
        )

    fun setTemperature(value: Float) {
        viewModelScope.launch { userPreferencesRepository.setTemperature(value) }
    }

    fun setTopP(value: Float) {
        viewModelScope.launch { userPreferencesRepository.setTopP(value) }
    }

    fun setSystemPrompt(value: String) {
        viewModelScope.launch { userPreferencesRepository.setSystemPrompt(value) }
    }

    fun setThinkingEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setThinkingEnabled(enabled) }
    }

    fun setVisionEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setVisionEnabled(enabled) }
    }

    fun setPrefillChunkSize(value: Int) {
        viewModelScope.launch { userPreferencesRepository.setPrefillChunkSize(value) }
    }

    fun setMaxSequenceLength(value: Int) {
        viewModelScope.launch { userPreferencesRepository.setMaxSequenceLength(value) }
    }

    fun setRagTemperature(value: Float) {
        viewModelScope.launch { userPreferencesRepository.setRagTemperature(value) }
    }

    fun setRagTopP(value: Float) {
        viewModelScope.launch { userPreferencesRepository.setRagTopP(value) }
    }

    fun setMaxSearchSteps(value: Int) {
        viewModelScope.launch { userPreferencesRepository.setMaxSearchSteps(value) }
    }

    fun setMaxContextTokens(value: Int) {
        viewModelScope.launch { userPreferencesRepository.setMaxContextTokens(value) }
    }

    fun setMaxRetrievedDocs(value: Int) {
        viewModelScope.launch { userPreferencesRepository.setMaxRetrievedDocs(value) }
    }

    fun setMinRetrievalScore(value: Float) {
        viewModelScope.launch { userPreferencesRepository.setMinRetrievalScore(value) }
    }

    fun setSoapTemperature(value: Float) {
        viewModelScope.launch { userPreferencesRepository.setSoapTemperature(value) }
    }

    fun setSoapTopP(value: Float) {
        viewModelScope.launch { userPreferencesRepository.setSoapTopP(value) }
    }

    fun setSoapSystemPrompt(value: String) {
        viewModelScope.launch { userPreferencesRepository.setSoapSystemPrompt(value) }
    }

    fun setSoapThinkingEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setSoapThinkingEnabled(enabled) }
    }

    // Context Chat Setters
    fun setContextChatTemperature(value: Float) {
        viewModelScope.launch { userPreferencesRepository.setContextChatTemperature(value) }
    }

    fun setContextChatTopP(value: Float) {
        viewModelScope.launch { userPreferencesRepository.setContextChatTopP(value) }
    }

    fun setContextChatThinkingEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setContextChatThinkingEnabled(enabled) }
    }

    fun setContextChatSystemPrompt(value: String) {
        viewModelScope.launch { userPreferencesRepository.setContextChatSystemPrompt(value) }
    }

    fun resetToDefaults() {
        viewModelScope.launch { userPreferencesRepository.resetToDefaults() }
    }
}
