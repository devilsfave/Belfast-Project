package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// PISANI RISK ASSESSMENT MAPPER — Form 18
//
// Maps MasterSchema → PisaniOutput (the printable/reviewable form).
// Lives in shared/commonMain — KMP-safe, zero Android imports.
//
// KEY RULES:
//  • 8 domains, each a free-text narrative field
//  • Domains 3 (Impulsivity/Self-control) and 8 (Engagement/Reliability)
//    are JUDGMENT_REQUIRED — if confidence < 0.80, routed to ClarificationQueue
//  • Risk level is NEVER auto-calculated — always requires nurse confirmation
//  • Policy Rule 5 (SOFT FLAG): if longTermRiskFactors is present,
//    strengthsAndProtectiveFactors must not be blank
//
// Source: JP's actual form (Image 14 — PISANI paper form)
//  • Section heading: "PISANI" (bold, underlined)
//  • 8 sequential free-text boxes, each with a bold domain label
//  • All are mandatory; most require clinical judgment
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Output model for the Belfast Trust PISANI Risk Assessment form.
 * Mirrors the exact layout of JP's paper form (Image 14).
 */
@Serializable
data class PisaniOutput(
    val patientName: String,
    val hcNumber: String,
    val assessmentDate: String,
    val assessorName: String,

    // Domain 1: Strengths & Protective Factors
    val strengthsAndProtectiveFactors: String,
    val strengthsConfidence: Float,

    // Domain 2: Long Term Risk Factors
    val longTermRiskFactors: String,
    val longTermRiskConfidence: Float,

    // Domain 3: Impulsivity/Self Control — JUDGMENT_REQUIRED
    val impulsivityAndSelfControl: String,
    val impulsivityConfidence: Float,
    val impulsivityJudgmentRequired: Boolean,

    // Domain 4: Past Suicidal Behaviours
    val pastSuicidalBehaviours: String,
    val pastSuicidalConfidence: Float,

    // Domain 5: Recent/Present Suicidal Behaviours
    val recentAndPresentSuicidalBehaviours: String,
    val recentSuicidalConfidence: Float,

    // Domain 6: Stressors/Precipitants
    val stressorsAndPrecipitants: String,
    val stressorsConfidence: Float,

    // Domain 7: Symptoms, Suffering and Recent Changes
    val symptomsSufferingRecentChanges: String,
    val symptomsConfidence: Float,

    // Domain 8: Engagement and Reliability — JUDGMENT_REQUIRED
    val engagementAndReliability: String,
    val engagementConfidence: Float,
    val engagementJudgmentRequired: Boolean,

    // Overall risk level — always requires nurse confirmation
    val overallRiskLevel: RiskLevel,
    val riskLevelConfirmedByNurse: Boolean,

    // Validation metadata
    val validationPassed: Boolean,
    val validationMessages: List<String>,

    // Total fields requiring nurse attention
    val judgmentRequiredCount: Int
)

/**
 * Maps the master schema to a printable PISANI form output.
 *
 * This is the SECOND GATE form — JP must review this alongside the Safety Plan
 * on 10 synthetic notes before Phase 9 begins.
 */
object PisaniMapper {

    /** Confidence threshold — below this, field is routed to ClarificationQueue */
    private const val CONFIDENCE_THRESHOLD = 0.80f

    fun map(schema: MasterSchema): PisaniOutput {
        val pisani = schema.pisaniRiskAssessment
        val patient = schema.patient
        val scores = schema.fieldConfidenceScores

        // Get confidence scores for each domain (default to 1.0 if not tracked)
        val strengthsConf = scores["pisaniRiskAssessment.strengthsAndProtectiveFactors"] ?: 1.0f
        val longTermConf = scores["pisaniRiskAssessment.longTermRiskFactors"] ?: 1.0f
        val impulsivityConf = scores["pisaniRiskAssessment.impulsivityAndSelfControl"] ?: 0.0f
        val pastSuicidalConf = scores["pisaniRiskAssessment.pastSuicidalBehaviours"] ?: 1.0f
        val recentSuicidalConf = scores["pisaniRiskAssessment.recentAndPresentSuicidalBehaviours"] ?: 1.0f
        val stressorsConf = scores["pisaniRiskAssessment.stressorsAndPrecipitants"] ?: 1.0f
        val symptomsConf = scores["pisaniRiskAssessment.symptomsSufferingRecentChanges"] ?: 1.0f
        val engagementConf = scores["pisaniRiskAssessment.engagementAndReliability"] ?: 0.0f

        // Domains 3 and 8 are always JUDGMENT_REQUIRED unless confidence is high
        val impulsivityJudgment = impulsivityConf < CONFIDENCE_THRESHOLD
        val engagementJudgment = engagementConf < CONFIDENCE_THRESHOLD

        // Validation
        val messages = mutableListOf<String>()
        if (pisani.longTermRiskFactors.isNotBlank() && pisani.strengthsAndProtectiveFactors.isBlank()) {
            messages.add("Rule 5: Long-term risk factors are documented but strengths/protective factors are blank.")
        }
        if (pisani.impulsivityAndSelfControl.isNullOrBlank()) {
            messages.add("Domain 3 (Impulsivity/Self-control) requires nurse clinical judgment input.")
        }
        if (pisani.engagementAndReliability.isNullOrBlank()) {
            messages.add("Domain 8 (Engagement/Reliability) requires nurse clinical judgment input.")
        }

        val judgmentCount = listOf(impulsivityJudgment, engagementJudgment).count { it }

        return PisaniOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            assessmentDate = schema.assessmentDate,
            assessorName = schema.referral.assessorName,

            strengthsAndProtectiveFactors = pisani.strengthsAndProtectiveFactors,
            strengthsConfidence = strengthsConf,

            longTermRiskFactors = pisani.longTermRiskFactors,
            longTermRiskConfidence = longTermConf,

            impulsivityAndSelfControl = pisani.impulsivityAndSelfControl ?: "[JUDGMENT REQUIRED — Nurse must provide clinical assessment]",
            impulsivityConfidence = impulsivityConf,
            impulsivityJudgmentRequired = impulsivityJudgment,

            pastSuicidalBehaviours = pisani.pastSuicidalBehaviours,
            pastSuicidalConfidence = pastSuicidalConf,

            recentAndPresentSuicidalBehaviours = pisani.recentAndPresentSuicidalBehaviours,
            recentSuicidalConfidence = recentSuicidalConf,

            stressorsAndPrecipitants = pisani.stressorsAndPrecipitants,
            stressorsConfidence = stressorsConf,

            symptomsSufferingRecentChanges = pisani.symptomsSufferingRecentChanges,
            symptomsConfidence = symptomsConf,

            engagementAndReliability = pisani.engagementAndReliability ?: "[JUDGMENT REQUIRED — Nurse must assess therapeutic alliance]",
            engagementConfidence = engagementConf,
            engagementJudgmentRequired = engagementJudgment,

            overallRiskLevel = pisani.overallRiskLevel,
            riskLevelConfirmedByNurse = false, // always starts unconfirmed

            validationPassed = messages.isEmpty(),
            validationMessages = messages,
            judgmentRequiredCount = judgmentCount
        )
    }
}
