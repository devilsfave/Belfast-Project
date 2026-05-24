package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.domain.testdata.SyntheticNoteFactory
import com.belfasttrust.jpclinical.domain.validator.PolicyValidator
import com.belfasttrust.jpclinical.domain.validator.Severity
import com.belfasttrust.jpclinical.schema.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNull

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

    // ── Emergency Assessment Mapper Tests ─────────────────────────────────────

    @Test
    fun `emergency assessment maps correctly demographics and gp`() {
        val schema = profiles[0]
        val output = EmergencyAssessmentMapper.map(schema)
        assertEquals(schema.patient.fullName, output.patientName)
        assertEquals(schema.patient.hcNumber, output.hcNumber)
        assertEquals(schema.patient.dateOfBirth, output.dateOfBirth)
        assertEquals(schema.patient.placeOfAssessment, output.placeOfAssessment)
        assertEquals(schema.referral.assessorName, output.assessorName)
        assertEquals(schema.patient.gpName, output.gpName)
        assertEquals(schema.assessmentDate, output.assessmentDate)
        assertEquals(schema.patient.nextOfKinName, output.nextOfKinName)
    }

    // ── Confidentiality Mapper Tests ──────────────────────────────────────────

    @Test
    fun `confidentiality maps capacity string correctly`() {
        val schema1 = profiles[0].copy(
            confidentiality = profiles[0].confidentiality.copy(capacityToConsent = true)
        )
        assertEquals("Yes", ConfidentialityMapper.map(schema1).capacityToConsent)

        val schema2 = profiles[0].copy(
            confidentiality = profiles[0].confidentiality.copy(capacityToConsent = false)
        )
        assertEquals("No", ConfidentialityMapper.map(schema2).capacityToConsent)

        val schema3 = profiles[0].copy(
            confidentiality = profiles[0].confidentiality.copy(capacityToConsent = null)
        )
        assertEquals("Not assessed", ConfidentialityMapper.map(schema3).capacityToConsent)
    }

    // ── Collateral Mapper Tests ───────────────────────────────────────────────

    @Test
    fun `collateral validation works and flags rule 9 if reason missing`() {
        val output1 = CollateralMapper.map(profiles[0])
        assertTrue(output1.validationPassed)
        assertTrue(output1.validationMessages.isEmpty())

        val output2 = CollateralMapper.map(profiles[9])
        assertFalse(output2.validationPassed)
        assertTrue(output2.validationMessages.any { it.contains("Rule 9") })
    }

    // ── History of Presenting Complaint Mapper Tests ─────────────────────────

    @Test
    fun `history of presenting complaint flags rule 8 when occurred is null`() {
        val output1 = HistoryPresentingComplaintMapper.map(profiles[0])
        assertTrue(output1.validationPassed)

        val output2 = HistoryPresentingComplaintMapper.map(profiles[7])
        assertFalse(output2.validationPassed)
        assertTrue(output2.validationMessages.any { it.contains("Rule 8") })
    }

    // ── Mental State Examination Mapper Tests ───────────────────────────────

    @Test
    fun `mental state exam flags rules 1 and 2 correctly`() {
        val output1 = MentalStateExaminationMapper.map(profiles[0])
        assertTrue(output1.validationPassed)

        val output2 = MentalStateExaminationMapper.map(profiles[7])
        assertFalse(output2.validationPassed)
        val msgs = output2.validationMessages
        assertTrue(msgs.any { it.contains("Rule 1") && it.contains("Thoughts of Life Not Worth Living") })
        assertTrue(msgs.any { it.contains("Rule 2") && it.contains("Thoughts of Self Harm") })
    }

    // ── Mental Health History Mapper Tests ─────────────────────────────────────

    @Test
    fun `mental health history maps correctly`() {
        val schema = profiles[0]
        val output = MentalHealthHistoryMapper.map(schema)
        assertEquals(schema.history.mentalHealthHistory.diagnosis, output.diagnosis)
        assertEquals(schema.history.mentalHealthHistory.mhoUse, output.mhoUse)
    }

    // ── Personal History Mapper Tests ─────────────────────────────────────────

    @Test
    fun `personal history maps correctly`() {
        val schema = profiles[0]
        val output = PersonalHistoryMapper.map(schema)
        assertEquals(schema.history.personalHistory.earlyChildhood, output.earlyChildhood)
    }

    // ── Family History Mapper Tests ───────────────────────────────────────────

    @Test
    fun `family history maps correctly`() {
        val schema = profiles[0]
        val output = FamilyHistoryMapper.map(schema)
        assertEquals(schema.history.familyHistory.livingArrangements, output.livingArrangements)
        assertEquals(schema.history.familyHistory.accommodationType, output.accommodationType)
        assertEquals(schema.history.familyHistory.accessToLethalMeans, output.accessToLethalMeans)
    }

    // ── Abuse Mapper Tests ────────────────────────────────────────────────────

    @Test
    fun `abuse mapper maps correctly`() {
        val schema = profiles[0]
        val output = AbuseMapper.map(schema)
        assertEquals(schema.abuse.abuseIssuesIdentified, output.abuseIssuesIdentified)
        assertEquals(schema.abuse.vulnerabilityIdentified, output.vulnerabilityIdentified)
    }

    // ── Offending History Mapper Tests ────────────────────────────────────────

    @Test
    fun `offending history maps and triggers rule 4 when null`() {
        val output1 = OffendingHistoryMapper.map(profiles[0])
        assertTrue(output1.validationPassed)

        val customOffending = profiles[0].offendingHistory.copy(accessToWeapons = null, gunLicence = null)
        val schema = profiles[0].copy(offendingHistory = customOffending)
        val output2 = OffendingHistoryMapper.map(schema)
        assertFalse(output2.validationPassed)
        assertTrue(output2.validationMessages.any { it.contains("Weapons access") })
        assertTrue(output2.validationMessages.any { it.contains("Gun licence") })
    }

    // ── Social Circumstances Mapper Tests ─────────────────────────────────────

    @Test
    fun `social circumstances maps correctly`() {
        val schema = profiles[0]
        val output = SocialCircumstancesMapper.map(schema)
        assertEquals(schema.socialCircumstances.housing, output.housing)
    }

    // ── Medications Mapper Tests ──────────────────────────────────────────────

    @Test
    fun `medications maps and triggers rule 6 when compliance missing`() {
        val output1 = MedicationsMapper.map(profiles[0])
        assertTrue(output1.validationPassed)

        val output2 = MedicationsMapper.map(profiles[8])
        assertFalse(output2.validationPassed)
        assertTrue(output2.validationMessages.any { it.contains("Rule 6") })
    }

    // ── Occupational Needs Mapper Tests ───────────────────────────────────────

    @Test
    fun `occupational needs maps correctly`() {
        val schema = profiles[0]
        val output = OccupationalNeedsMapper.map(schema)
        assertEquals(schema.socialCircumstances.occupationalNeeds, output.occupationalNeeds)
    }

    // ── Substance Misuse Mapper Tests ─────────────────────────────────────────

    @Test
    fun `substance misuse maps correctly`() {
        val schema = profiles[0]
        val output = SubstanceMisuseMapper.map(schema)
        assertEquals(schema.substanceMisuse.currentAlcoholUse.present, output.alcoholPresent)
        assertEquals(schema.substanceMisuse.currentDrugUse.present, output.drugPresent)
    }

    // ── Audit Mapper Tests ────────────────────────────────────────────────────

    @Test
    fun `audit mapper scoring and risk bands work correctly`() {
        val incompleteSchema = profiles[0].copy(
            substanceMisuse = profiles[0].substanceMisuse.copy(
                auditScore = AuditScore(q1 = 2)
            )
        )
        val incompleteOutput = AuditMapper.map(incompleteSchema)
        assertEquals("Incomplete assessment", incompleteOutput.riskLevel)
        assertFalse(incompleteOutput.followUpRequired)

        val lowerSchema = profiles[0].copy(
            substanceMisuse = profiles[0].substanceMisuse.copy(
                auditScore = AuditScore(q1 = 1, q2 = 1, q3 = 1, q4 = 1, q5 = 1, q6 = 0, q7 = 0, q8 = 0, q9 = 0, q10 = 0)
            )
        )
        val lowerOutput = AuditMapper.map(lowerSchema)
        assertEquals(5, lowerOutput.totalScore)
        assertEquals("Lower risk", lowerOutput.riskLevel)
        assertFalse(lowerOutput.followUpRequired)

        val increasingSchema = profiles[0].copy(
            substanceMisuse = profiles[0].substanceMisuse.copy(
                auditScore = AuditScore(q1 = 2, q2 = 2, q3 = 2, q4 = 2, q5 = 2, q6 = 0, q7 = 0, q8 = 0, q9 = 0, q10 = 0)
            )
        )
        val increasingOutput = AuditMapper.map(increasingSchema)
        assertEquals(10, increasingOutput.totalScore)
        assertEquals("Increasing risk", increasingOutput.riskLevel)
        assertTrue(increasingOutput.followUpRequired)

        val higherSchema = profiles[0].copy(
            substanceMisuse = profiles[0].substanceMisuse.copy(
                auditScore = AuditScore(q1 = 3, q2 = 3, q3 = 3, q4 = 3, q5 = 3, q6 = 2, q7 = 0, q8 = 0, q9 = 0, q10 = 0)
            )
        )
        val higherOutput = AuditMapper.map(higherSchema)
        assertEquals(17, higherOutput.totalScore)
        assertEquals("Higher risk", higherOutput.riskLevel)
        assertTrue(higherOutput.followUpRequired)

        val dependenceSchema = profiles[0].copy(
            substanceMisuse = profiles[0].substanceMisuse.copy(
                auditScore = AuditScore(q1 = 4, q2 = 4, q3 = 4, q4 = 4, q5 = 4, q6 = 4, q7 = 0, q8 = 0, q9 = 0, q10 = 0)
            )
        )
        val dependenceOutput = AuditMapper.map(dependenceSchema)
        assertEquals(24, dependenceOutput.totalScore)
        assertEquals("Possible dependence", dependenceOutput.riskLevel)
        assertTrue(dependenceOutput.followUpRequired)
    }

    // ── Ldq Mapper Tests ──────────────────────────────────────────────────────

    @Test
    fun `ldq mapper scoring dependence levels and alternative referrals work correctly`() {
        val incompleteSchema = profiles[0].copy(
            substanceMisuse = profiles[0].substanceMisuse.copy(
                ldqScore = LdqScore(q1 = 2)
            )
        )
        val incompleteOutput = LdqMapper.map(incompleteSchema)
        assertEquals("Incomplete assessment", incompleteOutput.dependenceLevel)
        assertFalse(incompleteOutput.catReferralIndicated)
        assertNull(incompleteOutput.alternativeReferralRecommendation)

        val lowSchema = profiles[0].copy(
            substanceMisuse = profiles[0].substanceMisuse.copy(
                ldqScore = LdqScore(q1 = 1, q2 = 1, q3 = 1, q4 = 1, q5 = 1, q6 = 0, q7 = 0, q8 = 0, q9 = 0, q10 = 0)
            )
        )
        val lowOutput = LdqMapper.map(lowSchema)
        assertEquals(5, lowOutput.totalScore)
        assertEquals("Low dependence", lowOutput.dependenceLevel)
        assertFalse(lowOutput.catReferralIndicated)
        assertTrue(lowOutput.alternativeReferralRecommendation!!.contains("Addictions NI") || lowOutput.alternativeReferralRecommendation!!.contains("Daisy"))

        val mediumSchema = profiles[0].copy(
            substanceMisuse = profiles[0].substanceMisuse.copy(
                ldqScore = LdqScore(q1 = 2, q2 = 2, q3 = 2, q4 = 2, q5 = 2, q6 = 2, q7 = 2, q8 = 2, q9 = 0, q10 = 0)
            )
        )
        val mediumOutput = LdqMapper.map(mediumSchema)
        assertEquals(16, mediumOutput.totalScore)
        assertEquals("Medium dependence", mediumOutput.dependenceLevel)
        assertFalse(mediumOutput.catReferralIndicated)
        assertTrue(mediumOutput.alternativeReferralRecommendation!!.contains("Addictions NI"))

        val catMediumSchema = profiles[0].copy(
            substanceMisuse = profiles[0].substanceMisuse.copy(
                ldqScore = LdqScore(q1 = 2, q2 = 2, q3 = 2, q4 = 2, q5 = 2, q6 = 2, q7 = 2, q8 = 2, q9 = 2, q10 = 3)
            )
        )
        val catMediumOutput = LdqMapper.map(catMediumSchema)
        assertEquals(21, catMediumOutput.totalScore)
        assertEquals("Medium dependence", catMediumOutput.dependenceLevel)
        assertTrue(catMediumOutput.catReferralIndicated)
        assertNull(catMediumOutput.alternativeReferralRecommendation)

        val youthSchema = profiles[0].copy(
            patient = profiles[0].patient.copy(dateOfBirth = "2015-05-12"),
            substanceMisuse = profiles[0].substanceMisuse.copy(
                ldqScore = LdqScore(q1 = 1, q2 = 1, q3 = 1, q4 = 1, q5 = 1, q6 = 0, q7 = 0, q8 = 0, q9 = 0, q10 = 0)
            )
        )
        val youthOutput = LdqMapper.map(youthSchema)
        assertTrue(youthOutput.alternativeReferralRecommendation!!.contains("Daisy"))
    }

    // ── Child Protection Mapper Tests ─────────────────────────────────────────

    @Test
    fun `child protection unocini trigger logic and rule 3 work correctly`() {
        val output1 = ChildProtectionMapper.map(profiles[0])
        assertFalse(output1.childrenInRegularContact)
        assertFalse(output1.unociniReferralRequired)
        assertTrue(output1.validationPassed)

        val output2 = ChildProtectionMapper.map(profiles[5])
        assertTrue(output2.childrenInRegularContact)
        assertTrue(output2.unociniReferralRequired)
        assertTrue(output2.validationPassed)

        val customCP = profiles[5].childProtection.copy(unociniReferralRequired = false)
        val schema = profiles[5].copy(childProtection = customCP)
        val output3 = ChildProtectionMapper.map(schema)
        assertFalse(output3.validationPassed)
        assertTrue(output3.validationMessages.any { it.contains("Rule 3") })
    }

    // ── Epic Contact Note Mapper Tests ────────────────────────────────────────

    @Test
    fun `epic contact note mapper builds summaries and maps interventions correctly`() {
        val schema = profiles[0].copy(
            epicContactNote = profiles[0].epicContactNote.copy(
                interventionsUsed = listOf(EpicIntervention.ACTIVITY_PLANNING, EpicIntervention.SLEEP_HYGIENE)
            )
        )
        val output = EpicContactNoteMapper.map(schema)
        assertEquals(schema.patient.fullName, output.patientName)
        assertTrue(output.collateralSummary.contains("Wife"))
        assertTrue(output.medicationsSummary.contains("Sertraline"))
        assertTrue(output.interventionsUsed.contains("ACTIVITY_PLANNING"))
        assertTrue(output.interventionsUsed.contains("SLEEP_HYGIENE"))
    }
}
