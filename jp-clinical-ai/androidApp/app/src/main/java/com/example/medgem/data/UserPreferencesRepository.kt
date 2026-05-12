package com.example.medgem.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository private constructor(private val context: Context) {

    private object PreferencesKeys {
        val TEMPERATURE = floatPreferencesKey("temperature")
        val TOP_P = floatPreferencesKey("top_p")
        val RAG_TEMPERATURE = floatPreferencesKey("rag_temperature")
        val RAG_TOP_P = floatPreferencesKey("rag_top_p")
        val MAX_SEARCH_STEPS = intPreferencesKey("max_search_steps")
        val MAX_CONTEXT_TOKENS = intPreferencesKey("max_context_tokens")
        val MAX_RETRIEVED_DOCS = intPreferencesKey("max_retrieved_docs")
        val MIN_RETRIEVAL_SCORE = floatPreferencesKey("min_retrieval_score")
        val SYSTEM_PROMPT =
            androidx.datastore.preferences.core.stringPreferencesKey("system_prompt")
        val THINKING_ENABLED =
            androidx.datastore.preferences.core.booleanPreferencesKey("thinking_enabled")
        val VISION_ENABLED =
            androidx.datastore.preferences.core.booleanPreferencesKey("vision_enabled")
        val PREFILL_CHUNK_SIZE = intPreferencesKey("prefill_chunk_size")
        val MAX_SEQUENCE_LENGTH = intPreferencesKey("max_sequence_length")
        val USER_NAME = androidx.datastore.preferences.core.stringPreferencesKey("user_name")
        val IS_ONBOARDING_COMPLETED =
            androidx.datastore.preferences.core.booleanPreferencesKey("is_onboarding_completed")

        // SOAP Settings
        val SOAP_TEMPERATURE = floatPreferencesKey("soap_temperature")
        val SOAP_TOP_P = floatPreferencesKey("soap_top_p")
        val SOAP_SYSTEM_PROMPT =
            androidx.datastore.preferences.core.stringPreferencesKey("soap_system_prompt")
        val SOAP_THINKING_ENABLED =
            androidx.datastore.preferences.core.booleanPreferencesKey("soap_thinking_enabled")

        // Context Chat Settings
        val CONTEXT_CHAT_TEMPERATURE = floatPreferencesKey("context_chat_temperature")
        val CONTEXT_CHAT_TOP_P = floatPreferencesKey("context_chat_top_p")
        val CONTEXT_CHAT_THINKING_ENABLED =
            androidx.datastore.preferences.core.booleanPreferencesKey("context_chat_thinking_enabled")
        val CONTEXT_CHAT_SYSTEM_PROMPT =
            androidx.datastore.preferences.core.stringPreferencesKey("context_chat_system_prompt")
    }

    private fun <T> getPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences -> preferences[key] ?: defaultValue }
    }

    val temperature: Flow<Float> =
        getPreference(
            PreferencesKeys.TEMPERATURE,
            com.example.medgem.ModelConfig.Llm.DEFAULT_TEMPERATURE
        )

    val topP: Flow<Float> =
        getPreference(PreferencesKeys.TOP_P, com.example.medgem.ModelConfig.Llm.DEFAULT_TOP_P)

    val ragTemperature: Flow<Float> =
        getPreference(
            PreferencesKeys.RAG_TEMPERATURE,
            com.example.medgem.ModelConfig.Llm.DEFAULT_TEMPERATURE
        )

    val ragTopP: Flow<Float> =
        getPreference(
            PreferencesKeys.RAG_TOP_P,
            com.example.medgem.ModelConfig.Llm.DEFAULT_TOP_P
        )

    val maxSearchSteps: Flow<Int> = getPreference(PreferencesKeys.MAX_SEARCH_STEPS, 1)

    val maxContextTokens: Flow<Int> = getPreference(PreferencesKeys.MAX_CONTEXT_TOKENS, 2048)

    val maxRetrievedDocs: Flow<Int> = getPreference(PreferencesKeys.MAX_RETRIEVED_DOCS, 1)

    val minRetrievalScore: Flow<Float> = getPreference(PreferencesKeys.MIN_RETRIEVAL_SCORE, 0.5f)

    val systemPrompt: Flow<String> =
        getPreference(PreferencesKeys.SYSTEM_PROMPT, DEFAULT_SYSTEM_PROMPT)

    val thinkingEnabled: Flow<Boolean> = getPreference(PreferencesKeys.THINKING_ENABLED, true)

    val visionEnabled: Flow<Boolean> = getPreference(PreferencesKeys.VISION_ENABLED, true)

    val prefillChunkSize: Flow<Int> = getPreference(PreferencesKeys.PREFILL_CHUNK_SIZE, 1024)

    val maxSequenceLength: Flow<Int> = getPreference(PreferencesKeys.MAX_SEQUENCE_LENGTH, 2048)

    val userName: Flow<String> = getPreference(PreferencesKeys.USER_NAME, "User")

    val isOnboardingCompleted: Flow<Boolean> =
        getPreference(PreferencesKeys.IS_ONBOARDING_COMPLETED, false)

    // SOAP Settings Flows
    val soapTemperature: Flow<Float> =
        getPreference(PreferencesKeys.SOAP_TEMPERATURE, 0.2f)

    val soapTopP: Flow<Float> =
        getPreference(PreferencesKeys.SOAP_TOP_P, 0.9f)

    val soapSystemPrompt: Flow<String> =
        getPreference(
            PreferencesKeys.SOAP_SYSTEM_PROMPT,
            DEFAULT_SOAP_SYSTEM_PROMPT
        )

    val soapThinkingEnabled: Flow<Boolean> =
        getPreference(PreferencesKeys.SOAP_THINKING_ENABLED, false)

    // Context Chat Settings Flows
    val contextChatTemperature: Flow<Float> =
        getPreference(PreferencesKeys.CONTEXT_CHAT_TEMPERATURE, 0.2f)

    val contextChatTopP: Flow<Float> =
        getPreference(PreferencesKeys.CONTEXT_CHAT_TOP_P, 0.9f)

    val contextChatThinkingEnabled: Flow<Boolean> =
        getPreference(PreferencesKeys.CONTEXT_CHAT_THINKING_ENABLED, false)

    val contextChatSystemPrompt: Flow<String> =
        getPreference(
            PreferencesKeys.CONTEXT_CHAT_SYSTEM_PROMPT,
            DEFAULT_CONTEXT_CHAT_SYSTEM_PROMPT
        )

    suspend fun setTemperature(temperature: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEMPERATURE] = temperature
        }
    }

    suspend fun setTopP(topP: Float) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.TOP_P] = topP }
    }

    suspend fun setRagTemperature(temperature: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RAG_TEMPERATURE] = temperature
        }
    }

    suspend fun setRagTopP(topP: Float) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.RAG_TOP_P] = topP }
    }

    suspend fun setMaxSearchSteps(steps: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_SEARCH_STEPS] = steps
        }
    }

    suspend fun setMaxContextTokens(tokens: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_CONTEXT_TOKENS] = tokens
        }
    }

    suspend fun setMaxRetrievedDocs(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_RETRIEVED_DOCS] = count
        }
    }

    suspend fun setMinRetrievalScore(score: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MIN_RETRIEVAL_SCORE] = score
        }
    }

    suspend fun setSystemPrompt(prompt: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYSTEM_PROMPT] = prompt
        }
    }

    suspend fun setThinkingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THINKING_ENABLED] = enabled
        }
    }

    suspend fun setVisionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VISION_ENABLED] = enabled
        }
    }

    suspend fun setPrefillChunkSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFILL_CHUNK_SIZE] = size
        }
    }

    suspend fun setMaxSequenceLength(length: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_SEQUENCE_LENGTH] = length
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences -> preferences[PreferencesKeys.USER_NAME] = name }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] = completed
        }
    }

    // SOAP Setters
    suspend fun setSoapTemperature(temperature: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOAP_TEMPERATURE] = temperature
        }
    }

    suspend fun setSoapTopP(topP: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOAP_TOP_P] = topP
        }
    }

    suspend fun setSoapSystemPrompt(prompt: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOAP_SYSTEM_PROMPT] = prompt
        }
    }

    suspend fun setSoapThinkingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOAP_THINKING_ENABLED] = enabled
        }
    }

    // Context Chat Setters
    suspend fun setContextChatTemperature(temperature: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONTEXT_CHAT_TEMPERATURE] = temperature
        }
    }

    suspend fun setContextChatTopP(topP: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONTEXT_CHAT_TOP_P] = topP
        }
    }

    suspend fun setContextChatThinkingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONTEXT_CHAT_THINKING_ENABLED] = enabled
        }
    }

    suspend fun setContextChatSystemPrompt(prompt: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONTEXT_CHAT_SYSTEM_PROMPT] = prompt
        }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            val userName = preferences[PreferencesKeys.USER_NAME]
            val onboardingCompleted = preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED]
            preferences.clear()
            if (userName != null) {
                preferences[PreferencesKeys.USER_NAME] = userName
            }
            if (onboardingCompleted != null) {
                preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] = onboardingCompleted
            }
        }
    }

    companion object {
        @Volatile
        @SuppressLint("StaticFieldLeak") // Using applicationContext, which is safe and doesn't leak
        private var INSTANCE: UserPreferencesRepository? = null

        const val DEFAULT_SYSTEM_PROMPT = "You are a helpful medical assistant."
        const val DEFAULT_SOAP_SYSTEM_PROMPT =
            "You are an expert medical scribe. Convert the following doctor-patient transcript and any attached medical images into a structured SOAP note (Subjective, Objective, Assessment, Plan). Be concise and accurate. Do not invent details not present in the transcript or images."
        const val DEFAULT_CONTEXT_CHAT_SYSTEM_PROMPT =
            "You are a helpful medical assistant analyzing patient data and medical images. Answer questions based on the provided context and images."

        fun getInstance(context: Context): UserPreferencesRepository {
            return INSTANCE
                ?: synchronized(this) {
                    INSTANCE
                        ?: UserPreferencesRepository(context.applicationContext).also {
                            INSTANCE = it
                        }
                }
        }
    }
}
