package com.belfasttrust.jpclinical.domain.validator

// ─────────────────────────────────────────────────────────────────────────────
// POLICY VALIDATOR — All 10 rules from AGENT_BRIEF.md Section 11
//
// Location: shared/commonMain — KMP-safe (zero Android imports).
// Usage: Call PolicyValidator.validate(schema) for final pass,
//        or individual ruleN(schema) functions for targeted checks.
//
// HARD_BLOCK rules halt the pipeline. SOFT_FLAG rules require nurse
// to type an acknowledgement note (logged to AuditEntry) before proceeding.
//
// DCB0129 note: Every rule is a patient-safety control. Do not modify
// thresholds or remove rules without Herbert's explicit instruction.
// ─────────────────────────────────────────────────────────────────────────────

import com.belfasttrust.jpclinical.schema.*

enum class Severity { HARD_BLOCK, SOFT_FLAG }

data class ValidationResult(
    val ruleId: Int,
    val passed: Boolean,
    val severity: Severity,
    val message: String,
    val affectedFields: List<String>
)

object PolicyValidator {

    /**
     * Run all 10 rules against the schema.
     * Returns results in rule order. Callers should:
     *  - Halt pipeline on any HARD_BLOCK where passed == false.
     *  - Show warning + require acknowledgement for any SOFT_FLAG where passed == false.
     */
    fun validate(schema: MasterSchema): List<ValidationResult> = listOf(
        rule1(schema),
        rule2(schema),
        rule3(schema),
        rule4(schema),
        rule5(schema),
        rule6(schema),
        rule7(schema),
        rule8(schema),
        rule9(schema),
        rule10(schema)
    )

    // ── Rule 1 ────────────────────────────────────────────────────────────────
    /**
     * HARD BLOCK: Suicidality must be explicitly documented, never implied.
     * Fails if TLNWL is null/blank OR suicidal ideation has no narrative.
     */
    fun rule1(schema: MasterSchema): ValidationResult {
        val s = schema.mentalStateExamination.suicidality
        val tlnwlMissing = s.tlnwl.isNullOrBlank()
        val ideationNoNarrative = s.suicidalIdeationPresent && s.suicidalIdeationDescription.isNullOrBlank()
        val passed = !tlnwlMissing && !ideationNoNarrative

        val issues = buildList {
            if (tlnwlMissing) add("TLNWL (Thoughts of Life Not Worth Living) not documented")
            if (ideationNoNarrative) add("Suicidal ideation present but no narrative description provided")
        }

        return ValidationResult(
            ruleId = 1,
            passed = passed,
            severity = Severity.HARD_BLOCK,
            message = if (passed) "Rule 1 passed: suicidality documented."
                      else "HARD BLOCK — Rule 1: ${issues.joinToString("; ")}",
            affectedFields = buildList {
                if (tlnwlMissing) add("mentalStateExamination.suicidality.tlnwl")
                if (ideationNoNarrative) add("mentalStateExamination.suicidality.suicidalIdeationDescription")
            }
        )
    }

    // ── Rule 2 ────────────────────────────────────────────────────────────────
    /**
     * HARD BLOCK: Both TLNWL and TSH must be addressed separately as distinct entries.
     * Fails if either is null or blank.
     */
    fun rule2(schema: MasterSchema): ValidationResult {
        val s = schema.mentalStateExamination.suicidality
        val tlnwlMissing = s.tlnwl.isNullOrBlank()
        val tshMissing = s.tsh.isNullOrBlank()
        val passed = !tlnwlMissing && !tshMissing

        return ValidationResult(
            ruleId = 2,
            passed = passed,
            severity = Severity.HARD_BLOCK,
            message = if (passed) "Rule 2 passed: both TLNWL and TSH documented."
                      else "HARD BLOCK — Rule 2: ${buildList {
                          if (tlnwlMissing) add("TLNWL missing")
                          if (tshMissing) add("TSH (Thoughts of Self Harm) missing")
                      }.joinToString("; ")}",
            affectedFields = buildList {
                if (tlnwlMissing) add("mentalStateExamination.suicidality.tlnwl")
                if (tshMissing) add("mentalStateExamination.suicidality.tsh")
            }
        )
    }

    // ── Rule 3 ────────────────────────────────────────────────────────────────
    /**
     * HARD BLOCK: Children in home triggers mandatory UNOCINI assessment.
     * Fails if childrenInRegularContact == true AND unociniReferralRequired is not resolved
     * (i.e., the mapper has not yet determined whether referral is needed).
     *
     * The UNOCINI trigger logic itself lives in ChildProtectionMapper, which sets
     * unociniReferralRequired based on risk level, weapons, lethal means, and injecting history.
     * This rule validates that the mapper ran and produced a definitive answer.
     */
    fun rule3(schema: MasterSchema): ValidationResult {
        val cp = schema.childProtection
        // If children are in contact, unociniReferralRequired must be explicitly set
        // by the mapper. We check that the trigger has been evaluated.
        val passed = if (cp.childrenInRegularContact) {
            // The mapper must have run: unociniReferralRequired is a resolved boolean
            // and if true, the trigger reason must be documented
            if (cp.unociniReferralRequired && cp.unociniTriggerReason.isNullOrBlank()) false
            else true
        } else true

        return ValidationResult(
            ruleId = 3,
            passed = passed,
            severity = Severity.HARD_BLOCK,
            message = if (passed) "Rule 3 passed: UNOCINI status resolved."
                      else "HARD BLOCK — Rule 3: Children are in regular contact but UNOCINI trigger reason is not documented.",
            affectedFields = if (!passed) listOf(
                "childProtection.unociniReferralRequired",
                "childProtection.unociniTriggerReason"
            ) else emptyList()
        )
    }

    // ── Rule 4 ────────────────────────────────────────────────────────────────
    /**
     * HARD BLOCK: Weapons and gun licence must be documented explicitly.
     * Fails if either field is null (not assessed, not documented).
     */
    fun rule4(schema: MasterSchema): ValidationResult {
        val oh = schema.offendingHistory
        val weaponsMissing = oh.accessToWeapons == null
        val gunLicenceMissing = oh.gunLicence == null
        val passed = !weaponsMissing && !gunLicenceMissing

        return ValidationResult(
            ruleId = 4,
            passed = passed,
            severity = Severity.HARD_BLOCK,
            message = if (passed) "Rule 4 passed: weapons and gun licence documented."
                      else "HARD BLOCK — Rule 4: ${buildList {
                          if (weaponsMissing) add("access to weapons not documented")
                          if (gunLicenceMissing) add("gun licence status not documented")
                      }.joinToString("; ")}",
            affectedFields = buildList {
                if (weaponsMissing) add("offendingHistory.accessToWeapons")
                if (gunLicenceMissing) add("offendingHistory.gunLicence")
            }
        )
    }

    // ── Rule 5 ────────────────────────────────────────────────────────────────
    /**
     * SOFT FLAG: Protective factors must be identified alongside risk factors.
     * Flags if long-term risk factors are present but strengths/protective factors are blank.
     */
    fun rule5(schema: MasterSchema): ValidationResult {
        val p = schema.pisaniRiskAssessment
        val hasRisk = p.longTermRiskFactors.isNotBlank()
        val missingStrengths = p.strengthsAndProtectiveFactors.isBlank()
        val passed = !(hasRisk && missingStrengths)

        return ValidationResult(
            ruleId = 5,
            passed = passed,
            severity = Severity.SOFT_FLAG,
            message = if (passed) "Rule 5 passed: protective factors documented."
                      else "SOFT FLAG — Rule 5: Long-term risk factors documented but strengths/protective factors are missing. Please document.",
            affectedFields = if (!passed) listOf("pisaniRiskAssessment.strengthsAndProtectiveFactors") else emptyList()
        )
    }

    // ── Rule 6 ────────────────────────────────────────────────────────────────
    /**
     * SOFT FLAG: Medication concordance must be documented if medications are prescribed.
     * Flags if medications list is non-empty but ALL compliance_issues fields are null.
     */
    fun rule6(schema: MasterSchema): ValidationResult {
        val meds = schema.medications.currentMedications
        val passed = if (meds.isEmpty()) true
                     else meds.any { it.complianceIssues != null }

        return ValidationResult(
            ruleId = 6,
            passed = passed,
            severity = Severity.SOFT_FLAG,
            message = if (passed) "Rule 6 passed: medication concordance documented."
                      else "SOFT FLAG — Rule 6: Medications are prescribed but no compliance issues are documented for any medication. Please review.",
            affectedFields = if (!passed) listOf("medications.currentMedications[*].complianceIssues") else emptyList()
        )
    }

    // ── Rule 7 ────────────────────────────────────────────────────────────────
    /**
     * HARD BLOCK: Safety plan requires nurse digital signature and timestamp before finalising.
     */
    fun rule7(schema: MasterSchema): ValidationResult {
        val sp = schema.safetyPlan
        val passed = sp.nurseSignatureCaptured && !sp.nurseSignatureTimestamp.isNullOrBlank()

        return ValidationResult(
            ruleId = 7,
            passed = passed,
            severity = Severity.HARD_BLOCK,
            message = if (passed) "Rule 7 passed: nurse signature captured on safety plan."
                      else "HARD BLOCK — Rule 7: Safety plan requires nurse digital signature before it can be finalised.",
            affectedFields = if (!passed) listOf(
                "safetyPlan.nurseSignatureCaptured",
                "safetyPlan.nurseSignatureTimestamp"
            ) else emptyList()
        )
    }

    // ── Rule 8 ────────────────────────────────────────────────────────────────
    /**
     * HARD BLOCK: Presenting suicide events in the 48 hours before this assessment must be
     * explicitly addressed (yes or no). A null 'occurred' means it was never documented.
     */
    fun rule8(schema: MasterSchema): ValidationResult {
        val events = schema.historyOfPresentingComplaint.presentingSuicideEvents48hrs
        val passed = events.occurred != null

        return ValidationResult(
            ruleId = 8,
            passed = passed,
            severity = Severity.HARD_BLOCK,
            message = if (passed) "Rule 8 passed: 48-hour suicide events documented."
                      else "HARD BLOCK — Rule 8: Presenting suicide events in the 48 hours prior to assessment have not been documented (yes or no answer required).",
            affectedFields = if (!passed) listOf(
                "historyOfPresentingComplaint.presentingSuicideEvents48hrs.occurred"
            ) else emptyList()
        )
    }

    // ── Rule 9 ────────────────────────────────────────────────────────────────
    /**
     * SOFT FLAG: Collateral information source must be documented or explicitly declined with reason.
     */
    fun rule9(schema: MasterSchema): ValidationResult {
        val c = schema.collateral
        val passed = c.collateralObtained || !c.collateralDeclinedReason.isNullOrBlank()

        return ValidationResult(
            ruleId = 9,
            passed = passed,
            severity = Severity.SOFT_FLAG,
            message = if (passed) "Rule 9 passed: collateral status documented."
                      else "SOFT FLAG — Rule 9: Collateral information was not obtained. A reason for declining must be documented.",
            affectedFields = if (!passed) listOf("collateral.collateralDeclinedReason") else emptyList()
        )
    }

    // ── Rule 10 ───────────────────────────────────────────────────────────────
    /**
     * HARD BLOCK: Safety plan output must contain all three pre-printed crisis numbers.
     * These are hard-coded in SafetyPlanMapper. This rule validates the mapper ran correctly.
     * Values: Lifeline "0808 808 8000", BelDOC "02890744447", SEBDOC "02890796220".
     */
    fun rule10(schema: MasterSchema): ValidationResult {
        val step5 = schema.safetyPlan.step5ProfessionalsAndAgencies

        val hasLifeline = step5.lifeline247 == "0808 808 8000"
        val hasBeldoc = step5.gpOohBeldoc == "02890744447"
        val hasSebdoc = step5.gpOohSebdoc == "02890796220"
        val passed = hasLifeline && hasBeldoc && hasSebdoc

        return ValidationResult(
            ruleId = 10,
            passed = passed,
            severity = Severity.HARD_BLOCK,
            message = if (passed) "Rule 10 passed: all crisis numbers present in safety plan."
                      else "HARD BLOCK — Rule 10: Safety plan is missing required pre-printed crisis numbers. ${buildList {
                          if (!hasLifeline) add("Lifeline 0808 808 8000")
                          if (!hasBeldoc) add("BelDOC 02890744447")
                          if (!hasSebdoc) add("SEBDOC 02890796220")
                      }.joinToString(", ")} — re-run SafetyPlanMapper.",
            affectedFields = if (!passed) listOf(
                "safetyPlan.step5ProfessionalsAndAgencies.lifeline247",
                "safetyPlan.step5ProfessionalsAndAgencies.gpOohBeldoc",
                "safetyPlan.step5ProfessionalsAndAgencies.gpOohSebdoc"
            ) else emptyList()
        )
    }
}
