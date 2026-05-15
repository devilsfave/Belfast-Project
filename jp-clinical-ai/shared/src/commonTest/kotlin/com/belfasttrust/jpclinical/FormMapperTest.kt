package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.domain.testdata.SyntheticNoteFactory
import com.belfasttrust.jpclinical.domain.validator.PolicyValidator
import com.belfasttrust.jpclinical.domain.validator.Severity
import com.belfasttrust.jpclinical.schema.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// ─────────────────────────────────────────────────────────────────────────────
// FORM MAPPER TESTS — Safety Plan + PISANI
//
// Tests the two gate forms against all 10 synthetic profiles.
// Verifies:
//  1. Crisis numbers are ALWAYS pre-printed (never from notes)
//  2. JUDGMENT_REQUIRED flags fire on domains 3 and 8
//  3. Policy rules integrate correctly with mapper outputs
//  4. All 10 profiles produce valid form outputs
// ─────────────────────────────────────────────────────────────────────────────

class FormMapperTest {

    private val profiles = SyntheticNoteFactory.getAllProfiles()

    // ── Safety Plan Mapper Tests ─────────────────────────────────────────────

    @Test
    fun `safety plan always contains correct crisis numbers for all profiles`() {
        profiles.forEach { schema ->
            val output = SafetyPlanMapper.map(schema)
            assertEquals("02890744447", output.step5GpOohBeldoc,
                "BelDOC wrong for ${schema.patient.fullName}")
            assertEquals("02890796220", output.step5GpOohSebdoc,
                "SEBDOC wrong for ${schema.patient.fullName}")
            assertEquals("0808 808 8000", output.step5Lifeline247,
                "Lifeline wrong for ${schema.patient.fullName}")
        }
    }

    @Test
    fun `safety plan contains lifeline accessibility note`() {
        val output = SafetyPlanMapper.map(profiles[0])
        assertTrue(output.step5LifelineAccessibilityNote.contains("SignVideo"),
            "Lifeline accessibility note missing SignVideo reference")
        assertTrue(output.step5LifelineAccessibilityNote.contains("18001"),
            "Lifeline accessibility note missing text phone number")
    }

    @Test
    fun `safety plan splits warning signals into 3 lines`() {
        val output = SafetyPlanMapper.map(profiles[0])
        assertEquals(3, output.step1WarningSignals.size)
    }

    @Test
    fun `safety plan splits coping strategies into 3 lines`() {
        val output = SafetyPlanMapper.map(profiles[0])
        assertEquals(3, output.step2InternalCopingStrategies.size)
    }

    @Test
    fun `safety plan splits environment safe into 2 lines`() {
        val output = SafetyPlanMapper.map(profiles[0])
        assertEquals(2, output.step6MakingEnvironmentSafe.size)
    }

    @Test
    fun `safety plan maps supporters from schema`() {
        val output = SafetyPlanMapper.map(profiles[0])
        assertTrue(output.step4Supporters.isNotEmpty())
        assertEquals("Sister Mary", output.step4Supporters[0].name)
        assertEquals("07700900001", output.step4Supporters[0].phone)
    }

    @Test
    fun `safety plan carries GP name from patient record`() {
        val output = SafetyPlanMapper.map(profiles[0])
        assertEquals("Dr Gilleland", output.step5GpName)
        assertEquals("02890311118", output.step5GpPhone)
    }

    @Test
    fun `safety plan reports validation passed when signed`() {
        val output = SafetyPlanMapper.map(profiles[0])
        assertTrue(output.validationPassed, "Profile 1 should pass validation")
        assertTrue(output.nurseSignatureCaptured)
    }

    @Test
    fun `safety plan parses social settings into distraction entries`() {
        val output = SafetyPlanMapper.map(profiles[0])
        assertTrue(output.step3DistractionPeoplePlaces.isNotEmpty(),
            "Distraction entries should be parsed")
    }

    @Test
    fun `safety plan includes most important reason to live`() {
        val output = SafetyPlanMapper.map(profiles[0])
        assertEquals("My grandchildren", output.mostImportantThingWorthLivingFor)
    }

    // ── PISANI Mapper Tests ──────────────────────────────────────────────────

    @Test
    fun `pisani maps all 8 domains for all profiles`() {
        profiles.forEach { schema ->
            val output = PisaniMapper.map(schema)
            assertTrue(output.strengthsAndProtectiveFactors.isNotBlank(),
                "Strengths blank for ${schema.patient.fullName}")
            assertTrue(output.longTermRiskFactors.isNotBlank(),
                "Long-term risk blank for ${schema.patient.fullName}")
            assertTrue(output.pastSuicidalBehaviours.isNotBlank(),
                "Past suicidal blank for ${schema.patient.fullName}")
        }
    }

    @Test
    fun `pisani flags impulsivity as judgment required when no confidence score`() {
        // Profile 1 has no confidence scores → defaults to 0.0 for domains 3 and 8
        val output = PisaniMapper.map(profiles[0])
        assertTrue(output.impulsivityJudgmentRequired,
            "Impulsivity should be judgment required with no confidence score")
    }

    @Test
    fun `pisani flags engagement as judgment required when no confidence score`() {
        val output = PisaniMapper.map(profiles[0])
        assertTrue(output.engagementJudgmentRequired,
            "Engagement should be judgment required with no confidence score")
    }

    @Test
    fun `pisani judgment count reflects number of flagged domains`() {
        val output = PisaniMapper.map(profiles[0])
        assertEquals(2, output.judgmentRequiredCount,
            "Both domain 3 and 8 should be flagged")
    }

    @Test
    fun `pisani risk level not confirmed by nurse initially`() {
        val output = PisaniMapper.map(profiles[0])
        assertFalse(output.riskLevelConfirmedByNurse)
    }

    @Test
    fun `pisani maps correct risk level from schema`() {
        assertEquals(RiskLevel.LOW, PisaniMapper.map(profiles[0]).overallRiskLevel)
        assertEquals(RiskLevel.MEDIUM, PisaniMapper.map(profiles[1]).overallRiskLevel)
        assertEquals(RiskLevel.HIGH, PisaniMapper.map(profiles[2]).overallRiskLevel)
        assertEquals(RiskLevel.VERY_HIGH, PisaniMapper.map(profiles[3]).overallRiskLevel)
    }

    @Test
    fun `pisani validation fails when strengths blank but risk present`() {
        val schema = profiles[0].copy(
            pisaniRiskAssessment = profiles[0].pisaniRiskAssessment.copy(
                strengthsAndProtectiveFactors = ""
            )
        )
        val output = PisaniMapper.map(schema)
        assertFalse(output.validationPassed)
        assertTrue(output.validationMessages.any { it.contains("Rule 5") })
    }

    @Test
    fun `pisani high confidence scores suppress judgment required flags`() {
        val schema = profiles[0].copy(
            fieldConfidenceScores = mapOf(
                "pisaniRiskAssessment.impulsivityAndSelfControl" to 0.95f,
                "pisaniRiskAssessment.engagementAndReliability" to 0.90f
            )
        )
        val output = PisaniMapper.map(schema)
        assertFalse(output.impulsivityJudgmentRequired)
        assertFalse(output.engagementJudgmentRequired)
        assertEquals(0, output.judgmentRequiredCount)
    }

    // ── Cross-validation: Policy Rules + Mappers ─────────────────────────────

    @Test
    fun `profile 8 triggers rules 1 2 and 8 via policy validator`() {
        val results = PolicyValidator.validate(profiles[7]) // profile8 = index 7
        val hardBlocks = results.filter { !it.passed && it.severity == Severity.HARD_BLOCK }
        assertTrue(hardBlocks.any { it.ruleId == 1 }, "Rule 1 should fire")
        assertTrue(hardBlocks.any { it.ruleId == 2 }, "Rule 2 should fire")
        assertTrue(hardBlocks.any { it.ruleId == 8 }, "Rule 8 should fire")
    }

    @Test
    fun `profile 9 triggers rule 6 via policy validator`() {
        val results = PolicyValidator.validate(profiles[8]) // profile9 = index 8
        val softFlags = results.filter { !it.passed && it.severity == Severity.SOFT_FLAG }
        assertTrue(softFlags.any { it.ruleId == 6 }, "Rule 6 should fire for no compliance docs")
    }

    @Test
    fun `profile 10 triggers rule 9 via policy validator`() {
        val results = PolicyValidator.validate(profiles[9]) // profile10 = index 9
        val softFlags = results.filter { !it.passed && it.severity == Severity.SOFT_FLAG }
        assertTrue(softFlags.any { it.ruleId == 9 }, "Rule 9 should fire for collateral declined")
    }

    @Test
    fun `profile 1 passes all 10 policy rules`() {
        val results = PolicyValidator.validate(profiles[0])
        val failures = results.filter { !it.passed }
        assertTrue(failures.isEmpty(), "Profile 1 should pass all rules but failed: ${failures.map { "Rule ${it.ruleId}: ${it.message}" }}")
    }
}
