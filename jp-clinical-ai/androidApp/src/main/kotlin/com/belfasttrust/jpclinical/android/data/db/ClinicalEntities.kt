package com.belfasttrust.jpclinical.android.data.db

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

// ─────────────────────────────────────────────────────────────────────────────
// NEW OBJECTBOX ENTITIES for JP Clinical AI
// Rule: Do NOT modify existing MedGEM entities (PatientEntity, VisitEntity,
//       ConversationEntity, MessageEntity, KnowledgeEntity).
// These live in androidApp — they use ObjectBox annotations (Android-only).
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Represents one documentation session (one shift visit → one form set).
 * Checkpointed after each extraction cluster and after each form approval.
 */
@Entity
data class ClinicalSessionEntity(
    @Id var id: Long = 0,
    @Index var sessionId: String = "",
    var nurseId: String = "",
    var assessmentDate: String = "",          // ISO 8601
    var patientHcNumber: String = "",
    var rawNotesPath: String? = null,         // encrypted file path on-device
    /** Serialised MasterSchema JSON (encrypted at write time) */
    var masterSchemaJson: String = "",
    var extractionCluster: Int = 0,           // 0 = not started, 1-3 = in progress
    var schemaFinalised: Boolean = false,
    var allFormsApproved: Boolean = false,
    var createdAt: Long = 0L,
    var updatedAt: Long = 0L
)

/**
 * Full audit trail — never deleted from device.
 * Legal defence under NMC / UK GDPR / Belfast Trust IG policy.
 * Every pipeline action writes one AuditEntry.
 */
@Entity
data class AuditEntryEntity(
    @Id var id: Long = 0,
    @Index var sessionId: String = "",
    var timestamp: Long = 0L,               // epoch millis
    var eventType: String = "",             // AuditEventType.name
    /**
     * JSON string with event-specific data.
     * Must never contain raw clinical notes directly —
     * only field names, validation rule IDs, form names, sync outcomes.
     */
    var details: String = "",
    var nurseId: String = ""
)

/**
 * Stores encrypted FHIR R4 bundles awaiting manual Epic sync.
 * Bundles are AES-256 encrypted with keys bound to Android Keystore.
 * Nothing is transmitted automatically.
 */
@Entity
data class FhirBundleEntity(
    @Id var id: Long = 0,
    @Index var sessionId: String = "",
    var formType: String = "",              // e.g. "SAFETY_PLAN", "PISANI"
    /** AES-256 encrypted FHIR R4 bundle bytes, base64-encoded for ObjectBox storage */
    var encryptedBundleBase64: String = "",
    var syncStatus: String = "PENDING",     // PENDING | SENT | FAILED
    var syncAttemptedAt: Long? = null,
    var syncOutcomeMessage: String? = null,
    var createdAt: Long = 0L
)

/**
 * One item in the Clarification Queue — a field the nurse must answer.
 * Populated after each extraction cluster if confidence < 0.80 or hard-block fires.
 */
@Entity
data class ClarificationItemEntity(
    @Id var id: Long = 0,
    @Index var sessionId: String = "",
    /** Dot-path to the schema field, e.g. "mentalStateExamination.suicidality.tlnwl" */
    var schemaFieldPath: String = "",
    /** Human-readable question presented to the nurse */
    var question: String = "",
    /** Options if constrained (e.g. "euthymic|depressed|elated|labile"), else null = free text */
    var optionsJson: String? = null,
    var confidenceScore: Float = 0f,
    var isJudgmentRequired: Boolean = false,
    var isHardBlockFailure: Boolean = false,
    var nurseAnswer: String? = null,
    var answeredAt: Long? = null,
    var createdAt: Long = 0L
)
