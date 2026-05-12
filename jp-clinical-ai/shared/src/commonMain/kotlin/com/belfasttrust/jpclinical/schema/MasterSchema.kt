package com.belfasttrust.jpclinical.schema

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// JP CLINICAL DOCUMENTATION AI — MASTER SCHEMA
// Version: 1.0
// Source of truth: AGENT_BRIEF.md Section 9
//
// RULES:
//  - This file lives in shared/commonMain. ZERO Android imports allowed.
//  - All classes are @Serializable for kotlinx.serialization (KMP-safe).
//  - Every field marked nullable may be absent from the nurse's notes.
//  - fieldConfidenceScores: key = dot-path to field, value = 0.0f..1.0f
//  - Fields with confidence < 0.80f are tagged JUDGMENT_REQUIRED by the
//    ExtractionPipeline and routed to the ClarificationQueue.
//  - MedGemma NEVER guesses. If it cannot extract a value with ≥0.80f
//    confidence, it sets the field to null and logs it as JUDGMENT_REQUIRED.
// ─────────────────────────────────────────────────────────────────────────────

/** Extraction clusters — each cluster checkpoints to the database before the next starts. */
enum class ExtractionCluster {
    CLUSTER_1, // Patient demographics, confidentiality, collateral, history of presenting complaint
    CLUSTER_2, // Mental State Examination, medication section
    CLUSTER_3  // Risk assessment (PISANI), substance misuse, social circumstances, safety plan
}

/**
 * Root master schema. Populated by MedGemma across three clusters.
 * Every form is generated from this object — notes are never re-read after schema is built.
 */
@Serializable
data class MasterSchema(
    val sessionId: String,
    val nurseId: String,
    val assessmentDate: String,          // ISO 8601
    val assessmentTime: String,          // HH:mm
    val visitType: VisitType,

    val patient: Patient,
    val referral: Referral,
    val confidentiality: Confidentiality,
    val collateral: Collateral,
    val historyOfPresentingComplaint: HistoryOfPresentingComplaint,
    val mentalStateExamination: MentalStateExamination,
    val history: History,
    val abuse: Abuse,
    val offendingHistory: OffendingHistory,
    val socialCircumstances: SocialCircumstances,
    val medications: Medications,
    val substanceMisuse: SubstanceMisuse,
    val childProtection: ChildProtection,
    val pisaniRiskAssessment: PisaniRiskAssessment,
    val safetyPlan: SafetyPlan,
    val epicContactNote: EpicContactNote,

    /** dot-path → confidence score 0.0..1.0  */
    val fieldConfidenceScores: Map<String, Float> = emptyMap(),

    /** Which cluster last wrote to this schema object */
    val extractionCluster: Int = 0,

    /** True only after PolicyValidator final pass succeeds and nurse has approved all forms */
    val schemaFinalised: Boolean = false,

    val schemaVersion: String = "1.0"
)

@Serializable
enum class VisitType {
    NEW_ASSESSMENT, REVIEW, CRISIS
}

// ── Patient ──────────────────────────────────────────────────────────────────

@Serializable
data class Patient(
    val fullName: String,
    val hcNumber: String,                 // H&C number
    val dateOfBirth: String,             // ISO 8601
    val address: String,
    val gpName: String,
    val gpAddress: String,
    val gpPhone: String,
    val nextOfKinName: String? = null,
    val nextOfKinRelationship: String? = null,
    val nextOfKinPhone: String? = null,
    val maritalStatus: String? = null,
    val ethnicity: String? = null,
    val gender: String? = null,
    val placeOfAssessment: String
)

// ── Referral ─────────────────────────────────────────────────────────────────

@Serializable
data class Referral(
    val referralAgent: String,
    val referralSource: String,
    val referralDate: String,            // ISO 8601
    val assessorName: String,
    val assessorDesignation: String
)

// ── Confidentiality ──────────────────────────────────────────────────────────

@Serializable
data class Confidentiality(
    val confidentialityExplained: Boolean,
    val capacityToConsent: Boolean? = null,
    val consentToSeekInformation: Boolean? = null,
    val consentToShareInformation: Boolean? = null,
    val informationShareableWith: List<String> = emptyList(),
    val familyCarerConsent: Boolean? = null,
    val consentToPhoneContact: Boolean? = null,
    val thirdPartyInformationRestrictions: String? = null
)

// ── Collateral ───────────────────────────────────────────────────────────────

@Serializable
data class Collateral(
    val collateralObtained: Boolean,
    /** Required if collateralObtained == false (Policy Rule 9 soft flag) */
    val collateralDeclinedReason: String? = null,
    val collateralSources: List<CollateralSource> = emptyList()
)

@Serializable
data class CollateralSource(
    val name: String,
    val relationship: String,
    val informationProvided: String
)

// ── History of Presenting Complaint ──────────────────────────────────────────

@Serializable
data class HistoryOfPresentingComplaint(
    val reasonForPresentation: String,
    val precipitatingFactors: String,
    val courseAndDuration: String,
    val featuresOfMentalIllness: String,
    val managementToDate: String,
    /** Policy Rule 8: HARD BLOCK if occurred is null */
    val presentingSuicideEvents48hrs: PresentingSuicideEvents
)

@Serializable
data class PresentingSuicideEvents(
    val occurred: Boolean?,              // null = not documented → Policy Rule 8 fires
    val description: String? = null,
    val method: String? = null,
    val intentLevel: String? = null
)

// ── Mental State Examination ──────────────────────────────────────────────────

@Serializable
data class MentalStateExamination(
    val appearanceBehaviour: AppearanceBehaviour,
    val speechThoughtForm: SpeechThoughtForm,
    val moodAffect: MoodAffect,
    /** Policy Rules 1 & 2 operate on suicidality */
    val suicidality: Suicidality,
    val thoughtContent: ThoughtContent,
    val perceptualDisturbances: PerceptualDisturbances,
    val cognition: Cognition,
    val insight: Insight
)

@Serializable
data class AppearanceBehaviour(
    val clothingAppropriateness: String,
    val selfCare: String,
    val motorActivity: String,
    val rapport: String,
    val eyeContact: String,
    val overallNarrative: String
)

@Serializable
data class SpeechThoughtForm(
    val spontaneity: String,
    val coherence: String,
    val rate: String,
    val tone: String,
    val volume: String,
    val thoughtDisorderPresent: Boolean,
    val thoughtDisorderDescription: String? = null,
    val overallNarrative: String
)

@Serializable
data class MoodAffect(
    val subjectiveMood: String,
    val objectiveAffect: String,
    val affectFluctuation: String,
    val moodClassification: MoodClassification,
    val anxietyPresent: Boolean,
    val anxietyDescription: String? = null,
    val reactivity: String,
    val overallNarrative: String
)

@Serializable
enum class MoodClassification { DEPRESSED, EUTHYMIC, ELATED, MIXED }

/**
 * CRITICAL SUICIDALITY BLOCK
 * - Policy Rule 1: tlnwl must not be null/blank and suicidalIdeationPresent must have a narrative.
 * - Policy Rule 2: BOTH tlnwl AND tsh must be addressed separately.
 * Both are HARD BLOCKs. Clinical abbreviations: TLNWL = Thoughts of Life Not Worth Living,
 * TSH = Thoughts of Self Harm.
 */
@Serializable
data class Suicidality(
    /** Thoughts of Life Not Worth Living — HARD BLOCK if null/blank */
    val tlnwl: String?,
    val suicidalIdeationPresent: Boolean,
    val suicidalIdeationDescription: String? = null,
    val planPresent: Boolean,
    val planDescription: String? = null,
    val intentPresent: Boolean,
    val intentDescription: String? = null,
    /** Thoughts of Self Harm — HARD BLOCK if null/blank */
    val tsh: String?,
    val selfHarmPlan: String? = null,
    val suicideAttemptThisPresentation: Boolean,
    val adviceGivenOnReattemptRisk: String,
    val overallNarrative: String
)

@Serializable
data class ThoughtContent(
    val preoccupations: String? = null,
    val obsessions: String? = null,
    val delusionsPresent: Boolean,
    val delusionsDescription: String? = null,
    val paranoidThoughts: String? = null,
    val thoughtInterference: Boolean,
    val passivityPhenomena: Boolean,
    val violentThoughts: Boolean,
    val violentThoughtsDescription: String? = null,
    val overallNarrative: String
)

@Serializable
data class PerceptualDisturbances(
    val auditoryHallucinations: Boolean,
    val auditoryDescription: String? = null,
    val visualHallucinations: Boolean,
    val visualDescription: String? = null,
    val gustatoryHallucinations: Boolean,
    val olfactoryHallucinations: Boolean,
    val tactileHallucinations: Boolean,
    val overallNarrative: String
)

@Serializable
data class Cognition(
    val orientationTime: Boolean,
    val orientationPlace: Boolean,
    val orientationPerson: Boolean,
    val attentionIntact: Boolean,
    val memoryIntact: Boolean,
    val mmseIndicated: Boolean,
    val mmseScore: Int? = null,
    val overallNarrative: String
)

@Serializable
data class Insight(
    val awarenessOfIllness: String,
    val willingnessToEngage: String,
    val insightLevel: InsightLevel,
    val overallNarrative: String
)

@Serializable
enum class InsightLevel { FULL, PARTIAL, NONE }

// ── History ──────────────────────────────────────────────────────────────────

@Serializable
data class History(
    val mentalHealthHistory: MentalHealthHistory,
    val personalHistory: PersonalHistory,
    val familyHistory: FamilyHistory
)

@Serializable
data class MentalHealthHistory(
    val diagnosis: String? = null,
    val previousServiceContact: String? = null,
    val previousAdmissions: String? = null,
    val mhoUse: Boolean,
    val mhoDetails: String? = null,
    val previousSelfHarm: String? = null,
    val recentSuicideEventsLast2Months: String? = null,
    val recentSuicideEventsBefore2Months: String? = null
)

@Serializable
data class PersonalHistory(
    val earlyChildhood: String? = null,
    val developmentalMilestones: String? = null,
    val schooling: String? = null,
    val psychosexualHistory: String? = null,
    val traumaHistory: String? = null,
    val relationshipHistory: String? = null,
    val employmentHistory: String? = null,
    val spiritualCulturalNeeds: String? = null
)

@Serializable
data class FamilyHistory(
    val livingArrangements: String,
    val familyDynamics: String? = null,
    val accommodationType: String,
    val abilityToManageIndependently: String,
    val carersInvolved: Boolean,
    val carerDetails: String? = null,
    /** Policy Rule 3 (UNOCINI trigger) checks this */
    val accessToLethalMeans: Boolean,
    val lethalMeansDescription: String? = null,
    val fccResponseToTreatment: String? = null,
    val parentsHistory: String? = null,
    val siblingsHistory: String? = null,
    val familyHistorySuicide: Boolean,
    val familyHistoryAddictions: Boolean,
    val familyHistoryMentalIllness: Boolean
)

// ── Abuse ─────────────────────────────────────────────────────────────────────

@Serializable
data class Abuse(
    val abuseIssuesIdentified: Boolean,
    val vulnerabilityIdentified: Boolean,
    val abuseDetails: String? = null
)

// ── Offending History ─────────────────────────────────────────────────────────

/**
 * Policy Rule 4 (HARD BLOCK): accessToWeapons and gunLicence must not be null.
 */
@Serializable
data class OffendingHistory(
    val forensicHistory: String? = null,
    val currentCharges: String? = null,
    val pendingCharges: String? = null,
    val custodialHistory: String? = null,
    /** HARD BLOCK if null */
    val accessToWeapons: Boolean?,
    /** HARD BLOCK if null */
    val gunLicence: Boolean?,
    val gunLicenceDetails: String? = null
)

// ── Social Circumstances ──────────────────────────────────────────────────────

@Serializable
data class SocialCircumstances(
    val housing: String,
    val finances: String? = null,
    val debts: String? = null,
    val relationships: String? = null,
    val friendships: String? = null,
    val supportNetwork: String? = null,
    val socialising: String? = null,
    val hobbies: String? = null,
    val strengths: String? = null,
    val occupationalNeeds: String? = null
)

// ── Medications ───────────────────────────────────────────────────────────────

@Serializable
data class Medications(
    val currentMedications: List<Medication> = emptyList(),
    val medicationAutonomy: MedicationAutonomy
)

@Serializable
data class Medication(
    val name: String,
    val dosage: String,
    val frequency: String,
    val sideEffects: String? = null,
    val allergies: String? = null,
    /** Policy Rule 6: soft flag if all items null when medications are prescribed */
    val complianceIssues: String? = null
)

@Serializable
enum class MedicationAutonomyLevel { INDEPENDENT, ASSISTED, DEPENDENT, NOT_APPLICABLE }

@Serializable
data class MedicationAutonomy(
    val removingFromPackaging: MedicationAutonomyLevel,
    val readingLabels: MedicationAutonomyLevel,
    val takingRightDoseRightTime: MedicationAutonomyLevel,
    val swallowingTablets: MedicationAutonomyLevel,
    val usingEquipmentAids: MedicationAutonomyLevel,
    val storingSafely: MedicationAutonomyLevel,
    val disposingSafely: MedicationAutonomyLevel,
    val ordering: MedicationAutonomyLevel,
    val collecting: MedicationAutonomyLevel
)

// ── Substance Misuse ──────────────────────────────────────────────────────────

@Serializable
data class SubstanceMisuse(
    val currentAlcoholUse: AlcoholUse,
    val currentDrugUse: DrugUse,
    val impactOnLife: String? = null,
    val previousSubstanceUse: String? = null,
    val abstinenceHistory: String? = null,
    val complexFactors: SubstanceComplexFactors,
    /** AUDIT scores — calculated in Kotlin, not extracted from notes */
    val auditScore: AuditScore,
    /** LDQ scores — calculated in Kotlin, not extracted from notes */
    val ldqScore: LdqScore
)

@Serializable
data class AlcoholUse(
    val present: Boolean,
    val frequency: String? = null,
    val amount: String? = null,
    val duration: String? = null,
    val withdrawalSymptoms: Boolean,
    val cravings: Boolean
)

@Serializable
data class DrugUse(
    val present: Boolean,
    val substances: List<String> = emptyList(),
    val frequency: String? = null,
    val duration: String? = null,
    val withdrawalSymptoms: Boolean,
    val cravings: Boolean,
    val polysubstance: Boolean
)

@Serializable
data class SubstanceComplexFactors(
    val pregnancy: Boolean,
    /** Triggers UNOCINI check if children in home */
    val injectingHistory: Boolean,
    val hiv: Boolean? = null,
    val hepB: Boolean? = null,
    val hepC: Boolean? = null
)

/**
 * AUDIT (Alcohol Use Disorders Identification Test) — 10 questions.
 * Scoring: Q1-Q8: 0-4, Q9-Q10: 0, 2, or 4.
 * Scoring thresholds implemented in AuditMapper — never extracted by AI.
 */
@Serializable
data class AuditScore(
    val q1: Int? = null, val q2: Int? = null, val q3: Int? = null,
    val q4: Int? = null, val q5: Int? = null, val q6: Int? = null,
    val q7: Int? = null, val q8: Int? = null, val q9: Int? = null,
    val q10: Int? = null,
    val total: Int? = null
)

/**
 * LDQ (Leeds Dependence Questionnaire) — 10 questions, scored 0-3.
 * threshold >20 → CAT referral. Implemented in LdqMapper — never extracted by AI.
 */
@Serializable
data class LdqScore(
    val q1: Int? = null, val q2: Int? = null, val q3: Int? = null,
    val q4: Int? = null, val q5: Int? = null, val q6: Int? = null,
    val q7: Int? = null, val q8: Int? = null, val q9: Int? = null,
    val q10: Int? = null,
    val total: Int? = null,
    val dependenceLevel: DependenceLevel? = null,
    /** True if total > 20 — computed by LdqMapper, never by AI */
    val catReferralIndicated: Boolean = false
)

@Serializable
enum class DependenceLevel { LOW, MEDIUM, HIGH }

// ── Child Protection ──────────────────────────────────────────────────────────

/**
 * Policy Rule 3 (HARD BLOCK): if childrenInRegularContact == true,
 * unociniReferralRequired must be resolved.
 * UNOCINI trigger logic is implemented in ChildProtectionMapper — never by AI.
 */
@Serializable
data class ChildProtection(
    val childrenInRegularContact: Boolean,
    val children: List<ChildDetail> = emptyList(),
    val partnerDetails: String? = null,
    val fccSocialServicesInvolved: Boolean,
    val unociniReferralRequired: Boolean,
    val unociniTriggerReason: String? = null
)

@Serializable
data class ChildDetail(
    val name: String,
    val sex: String,
    val dateOfBirth: String,            // ISO 8601
    val relationshipToPatient: String,
    val relationshipToPartner: String? = null,
    val otherParentDetails: String? = null
)

// ── PISANI Risk Assessment ────────────────────────────────────────────────────

/**
 * Eight-domain PISANI framework.
 * Fields 3 (impulsivityAndSelfControl) and 8 (engagementAndReliability)
 * are JUDGMENT_REQUIRED — confidence threshold 0.80f applies.
 */
@Serializable
data class PisaniRiskAssessment(
    val strengthsAndProtectiveFactors: String,
    // Policy Rule 5 (soft): must not be blank if longTermRiskFactors is present
    val longTermRiskFactors: String,
    /** JUDGMENT_REQUIRED — goes to ClarificationQueue if confidence < 0.80 */
    val impulsivityAndSelfControl: String?,
    val pastSuicidalBehaviours: String,
    val recentAndPresentSuicidalBehaviours: String,
    val stressorsAndPrecipitants: String,
    val symptomsSufferingRecentChanges: String,
    /** JUDGMENT_REQUIRED — goes to ClarificationQueue if confidence < 0.80 */
    val engagementAndReliability: String?,
    val overallRiskLevel: RiskLevel
)

@Serializable
enum class RiskLevel { LOW, MEDIUM, HIGH, VERY_HIGH }

// ── Safety Plan ───────────────────────────────────────────────────────────────

/**
 * Stanley-Brown 6-step Safety Plan.
 * Policy Rule 7 (HARD BLOCK): nurseSig natureCaptured must be true before finalising.
 * Policy Rule 10 (HARD BLOCK): crisis numbers must be present in mapper output.
 * Crisis numbers are NEVER extracted from notes — they are pre-printed in SafetyPlanMapper.
 */
@Serializable
data class SafetyPlan(
    val step1WarningSignals: String,
    val step2InternalCopingStrategies: String,
    val step3SocialSettingsForDistraction: String,
    val step4PeopleToAskForHelp: List<SafetyContact> = emptyList(),
    val step5ProfessionalsAndAgencies: Step5Professionals,
    val step6MakingEnvironmentSafe: String,
    val mostImportantReasonToLive: String,
    val followUpCallAgreed: Boolean,
    val followUpCallDatetime: String? = null, // ISO 8601 datetime
    /** HARD BLOCK (Rule 7) if false */
    val nurseSignatureCaptured: Boolean = false,
    val nurseSignatureTimestamp: String? = null
)

@Serializable
data class SafetyContact(val name: String, val phone: String)

/**
 * Step 5 crisis contacts — ALL values are hard-coded in SafetyPlanMapper.
 * NEVER extract these from notes.
 */
@Serializable
data class Step5Professionals(
    val gpName: String,
    val gpPhone: String,
    /** PRE-PRINTED — Belfast Doctor on Call */
    val gpOohBeldoc: String = "02890744447",
    /** PRE-PRINTED — South and East Belfast Doctor on Call */
    val gpOohSebdoc: String = "02890796220",
    /** PRE-PRINTED — Lifeline 24/7 */
    val lifeline247: String = "0808 808 8000",
    val otherContacts: List<String> = emptyList()
)

// ── Epic Contact Note ─────────────────────────────────────────────────────────

@Serializable
data class EpicContactNote(
    val typeOfContact: String,
    val purposeOfContact: String,
    val contactDetailsOverview: String,
    val interventionsUsed: List<EpicIntervention> = emptyList(),
    val riskSummary: String,
    val plan: String
)

/**
 * Pre-defined interventions checkbox list for Epic EHR Contact Note (Form 20).
 * The nurse selects which apply — AI does not choose interventions autonomously.
 */
@Serializable
enum class EpicIntervention {
    ACTIVITY_PLANNING,
    BEHAVIOURAL_ACTIVATION,
    BRIEF_INTERVENTION,
    CAFFEINE_INTAKE_ADVICE,
    CBT_TECHNIQUES,
    GOAL_SETTING,
    HEALTH_PROMOTION,
    MEDICATION_CONCORDANCE_MONITORING,
    MEDICATION_EDUCATION,
    MENTAL_STATE_MONITORING,
    PROBLEM_SOLVING,
    PSYCHOSOCIAL_INTERVENTIONS,
    REFRAMING_THOUGHTS,
    RECOVERING_WELL_SESSION,
    RELAPSE_PREVENTION,
    RISK_ASSESSING,
    RISK_MANAGEMENT,
    SLEEP_HYGIENE,
    SOCIAL_ACTIVITY,
    SOLUTION_FOCUSED_APPROACH,
    SUPPORT_AND_REASSURANCE,
    SYMPTOM_MANAGEMENT,
    SYMPTOM_RECOGNITION
}
