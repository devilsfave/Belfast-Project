package com.belfasttrust.jpclinical.domain

// ─────────────────────────────────────────────────────────────────────────────
// DOMAIN INTERFACE ABSTRACTIONS — shared/commonMain
// KMP contracts that Android implements now; iOS will implement in a future module.
// Zero Android imports. Zero platform-specific code.
// ─────────────────────────────────────────────────────────────────────────────

import com.belfasttrust.jpclinical.schema.MasterSchema
import kotlinx.coroutines.flow.Flow

// ── AuditEventType ────────────────────────────────────────────────────────────

/** All event types that must be written to the AuditLog. */
enum class AuditEventType {
    EXTRACTION_START,
    EXTRACTION_COMPLETE,
    VALIDATION_RUN,
    VALIDATION_RESULT,
    CLARIFICATION_ASKED,
    CLARIFICATION_ANSWERED,
    FORM_GENERATED,
    NURSE_APPROVED,
    NURSE_OVERRIDE,       // Nurse typed acknowledgement note for a SOFT_FLAG
    SYNC_TRIGGERED,
    SYNC_COMPLETE,
    SYNC_FAILED
}

// ── InferenceEngine ────────────────────────────────────────────────────────────

/**
 * Contract for on-device LLM inference.
 * Android implementation: wraps MedGEM's LlmInferenceService (ExecuTorch).
 * Test/emulator implementation: returns pre-baked JSON from synthetic notes.
 * iOS future implementation: wraps ExecuTorch C++ iOS bridge.
 *
 * DCB0129: This interface is the boundary between the clinical pipeline and
 * the inference engine. Any change here requires safety documentation.
 */
interface InferenceEngine {
    /**
     * Run inference on [prompt] and stream the response token by token.
     * Callers collect the Flow and build the full response.
     */
    fun generateStream(prompt: String): Flow<String>

    /** Single-shot generation — blocks until complete. Use only in tests. */
    suspend fun generate(prompt: String): String

    /** True if the model is loaded and ready for inference. */
    val isReady: Boolean
}

// ── SpeechRecogniser ──────────────────────────────────────────────────────────

/**
 * Contract for offline speech-to-text.
 * Android implementation: wraps MedGEM's MedAsrService (sherpa-onnx).
 * iOS future: wraps sherpa-onnx iOS CocoaPods build.
 */
interface SpeechRecogniser {
    fun startRecording()
    fun stopRecording()
    val transcriptFlow: Flow<String>
    val isRecording: Boolean
}

// ── SessionRepository ─────────────────────────────────────────────────────────

/**
 * Contract for persisting and retrieving ClinicalSessions.
 * Android implementation: ObjectBox ClinicalSessionEntity.
 * iOS future: ObjectBox Swift SDK.
 */
interface SessionRepository {
    suspend fun saveSchema(sessionId: String, schema: MasterSchema)
    suspend fun loadSchema(sessionId: String): MasterSchema?
    suspend fun finaliseSession(sessionId: String)
    fun getAllSessions(): Flow<List<String>> // returns sessionIds
}

// ── AuditLogger ──────────────────────────────────────────────────────────────

/**
 * Contract for writing to the immutable audit log.
 * Android implementation: ObjectBox AuditEntryEntity.
 * The audit log is NEVER deleted.
 */
interface AuditLogger {
    suspend fun log(
        sessionId: String,
        eventType: AuditEventType,
        nurseId: String,
        details: String  // JSON string — must NOT contain raw clinical notes
    )
}
