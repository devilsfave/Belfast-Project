package com.belfasttrust.jpclinical.domain.validator

import com.belfasttrust.jpclinical.schema.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

// ─────────────────────────────────────────────────────────────────────────────
// POLICY VALIDATOR UNIT TESTS
// 10 rules × 3 cases each (pass, fail, edge) = 30 test cases minimum
// All tests must pass before Phase 5 is considered complete.
// DCB0129 requirement: each patient-safety control must have a documented test.
// ─────────────────────────────────────────────────────────────────────────────

class PolicyValidatorTest {

    // ── Helper builders ──────────────────────────────────────────────────────

    private fun baseSuicidality(
        tlnwl: String? = "No TLNWL expressed",
        tsh: String? = "No TSH expressed",
        ideationPresent: Boolean = false,
        ideationDescription: String? = null
    ) = Suicidality(
        tlnwl = tlnwl,
        tsh = tsh,
        suicidalIdeationPresent = ideationPresent,
        suicidalIdeationDescription = ideationDescription,
        planPresent = false, planDescription = null,
        intentPresent = false, intentDescription = null,
        selfHarmPlan = null,
        suicideAttemptThisPresentation = false,
        adviceGivenOnReattemptRisk = "Standard safety advice given",
        overallNarrative = "Patient denied suicidal ideation."
    )

    private fun baseSafetyPlan(
        signatureCaptured: Boolean = true,
        signatureTimestamp: String? = "2026-05-12T09:00:00Z",
        lifeline: String = "0808 808 8000",
        beldoc: String = "02890744447",
        sebdoc: String = "02890796220"
    ) = SafetyPlan(
        step1WarningSignals = "Low mood, withdrawing",
        step2InternalCopingStrategies = "Breathing exercises",
        step3SocialSettingsForDistraction = "Walk in park",
        step4PeopleToAskForHelp = listOf(SafetyContact("Sister", "07700900000")),
        step5ProfessionalsAndAgencies = Step5Professionals(
            gpName = "Dr Smith", gpPhone = "02890123456",
            gpOohBeldoc = beldoc, gpOohSebdoc = sebdoc, lifeline247 = lifeline
        ),
        step6MakingEnvironmentSafe = "Medications stored by family",
        mostImportantReasonToLive = "Children",
        followUpCallAgreed = true,
        followUpCallDatetime = "2026-05-13T10:00:00Z",
        nurseSignatureCaptured = signatureCaptured,
        nurseSignatureTimestamp = signatureTimestamp
    )

    private fun baseChildProtection(
        childrenInContact: Boolean = false,
        unociniRequired: Boolean = false,
        unociniReason: String? = null
    ) = ChildProtection(
        childrenInRegularContact = childrenInContact,
        children = emptyList(),
        partnerDetails = null,
        fccSocialServicesInvolved = false,
        unociniReferralRequired = unociniRequired,
        unociniTriggerReason = unociniReason
    )

    private fun baseOffendingHistory(
        accessToWeapons: Boolean? = false,
        gunLicence: Boolean? = false
    ) = OffendingHistory(
        forensicHistory = null, currentCharges = null, pendingCharges = null,
        custodialHistory = null,
        accessToWeapons = accessToWeapons,
        gunLicence = gunLicence,
        gunLicenceDetails = null
    )

    private fun basePisani(
        strengths: String = "Good family support",
        longTermRisk: String = "History of self-harm"
    ) = PisaniRiskAssessment(
        strengthsAndProtectiveFactors = strengths,
        longTermRiskFactors = longTermRisk,
        impulsivityAndSelfControl = "Moderate",
        pastSuicidalBehaviours = "Two previous attempts",
        recentAndPresentSuicidalBehaviours = "None at this assessment",
        stressorsAndPrecipitants = "Financial stress",
        symptomsSufferingRecentChanges = "Improved sleep",
        engagementAndReliability = "Good",
        overallRiskLevel = RiskLevel.MEDIUM
    )

    private fun baseMedications(meds: List<Medication> = emptyList()) = Medications(
        currentMedications = meds,
        medicationAutonomy = MedicationAutonomy(
            removingFromPackaging = MedicationAutonomyLevel.INDEPENDENT,
            readingLabels = MedicationAutonomyLevel.INDEPENDENT,
            takingRightDoseRightTime = MedicationAutonomyLevel.INDEPENDENT,
            swallowingTablets = MedicationAutonomyLevel.INDEPENDENT,
            usingEquipmentAids = MedicationAutonomyLevel.NOT_APPLICABLE,
            storingSafely = MedicationAutonomyLevel.INDEPENDENT,
            disposingSafely = MedicationAutonomyLevel.INDEPENDENT,
            ordering = MedicationAutonomyLevel.INDEPENDENT,
            collecting = MedicationAutonomyLevel.INDEPENDENT
        )
    )

    /** Build a minimal valid MasterSchema for testing individual fields */
    private fun buildSchema(
        suicidality: Suicidality = baseSuicidality(),
        safetyPlan: SafetyPlan = baseSafetyPlan(),
        childProtection: ChildProtection = baseChildProtection(),
        offendingHistory: OffendingHistory = baseOffendingHistory(),
        pisani: PisaniRiskAssessment = basePisani(),
        medications: Medications = baseMedications(),
        presentingSuicideEvents: PresentingSuicideEvents = PresentingSuicideEvents(occurred = false),
        collateralObtained: Boolean = true,
        collateralDeclinedReason: String? = null
    ) = MasterSchema(
        sessionId = "test-session-001",
        nurseId = "jp-nurse-001",
        assessmentDate = "2026-05-12",
        assessmentTime = "14:00",
        visitType = VisitType.REVIEW,
        patient = Patient(
            fullName = "Test Patient", hcNumber = "HC123456",
            dateOfBirth = "1980-01-01", address = "123 Test St, Belfast",
            gpName = "Dr Smith", gpAddress = "Test Surgery", gpPhone = "02890111111",
            placeOfAssessment = "Patient's home"
        ),
        referral = Referral(
            referralAgent = "GP", referralSource = "GP Referral",
            referralDate = "2026-05-10", assessorName = "JP",
            assessorDesignation = "Mental Health Nurse"
        ),
        confidentiality = Confidentiality(confidentialityExplained = true),
        collateral = Collateral(
            collateralObtained = collateralObtained,
            collateralDeclinedReason = collateralDeclinedReason
        ),
        historyOfPresentingComplaint = HistoryOfPresentingComplaint(
            reasonForPresentation = "Low mood",
            precipitatingFactors = "Relationship breakdown",
            courseAndDuration = "3 weeks",
            featuresOfMentalIllness = "Depressive episode",
            managementToDate = "GP prescribed sertraline",
            presentingSuicideEvents48hrs = presentingSuicideEvents
        ),
        mentalStateExamination = MentalStateExamination(
            appearanceBehaviour = AppearanceBehaviour("appropriate", "adequate", "calm", "good", "maintained", "Well-presented"),
            speechThoughtForm = SpeechThoughtForm("spontaneous", "coherent", "normal", "normal", "normal", false, null, "Normal speech"),
            moodAffect = MoodAffect("low", "depressed", "congruent", MoodClassification.DEPRESSED, false, null, "Reactive", "Low mood"),
            suicidality = suicidality,
            thoughtContent = ThoughtContent(null, null, false, null, null, false, false, false, null, "No thought disorder"),
            perceptualDisturbances = PerceptualDisturbances(false, null, false, null, false, false, false, "No perceptual disturbances"),
            cognition = Cognition(true, true, true, true, true, false, null, "Fully orientated"),
            insight = Insight("full awareness", "willing to engage", InsightLevel.FULL, "Good insight")
        ),
        history = History(
            mentalHealthHistory = MentalHealthHistory(diagnosis = "Depression", mhoUse = false),
            personalHistory = PersonalHistory(),
            familyHistory = FamilyHistory(
                livingArrangements = "Lives alone", accommodationType = "Private rental",
                abilityToManageIndependently = "Independent", carersInvolved = false,
                accessToLethalMeans = false, familyHistorySuicide = false,
                familyHistoryAddictions = false, familyHistoryMentalIllness = false
            )
        ),
        abuse = Abuse(false, false, null),
        offendingHistory = offendingHistory,
        socialCircumstances = SocialCircumstances(housing = "Private rental"),
        medications = medications,
        substanceMisuse = SubstanceMisuse(
            currentAlcoholUse = AlcoholUse(false, null, null, null, false, false),
            currentDrugUse = DrugUse(false, emptyList(), null, null, false, false, false),
            complexFactors = SubstanceComplexFactors(false, false),
            auditScore = AuditScore(),
            ldqScore = LdqScore()
        ),
        childProtection = childProtection,
        pisaniRiskAssessment = pisani,
        safetyPlan = safetyPlan,
        epicContactNote = EpicContactNote(
            typeOfContact = "Home visit", purposeOfContact = "Mental state review",
            contactDetailsOverview = "Patient at home", riskSummary = "Medium risk", plan = "Continue current plan"
        )
    )

    // ── Rule 1 Tests ─────────────────────────────────────────────────────────

    @Test
    fun `rule1 passes when tlnwl and ideation narrative both present`() {
        val schema = buildSchema(suicidality = baseSuicidality(
            tlnwl = "Patient denied TLNWL",
            ideationPresent = false
        ))
        val result = PolicyValidator.rule1(schema)
        assertTrue(result.passed)
        assertEquals(Severity.HARD_BLOCK, result.severity)
    }

    @Test
    fun `rule1 fails when tlnwl is null`() {
        val schema = buildSchema(suicidality = baseSuicidality(tlnwl = null))
        val result = PolicyValidator.rule1(schema)
        assertFalse(result.passed)
        assertTrue(result.affectedFields.contains("mentalStateExamination.suicidality.tlnwl"))
    }

    @Test
    fun `rule1 fails when suicidal ideation present but no description narrative`() {
        val schema = buildSchema(suicidality = baseSuicidality(
            ideationPresent = true, ideationDescription = null
        ))
        val result = PolicyValidator.rule1(schema)
        assertFalse(result.passed)
        assertTrue(result.affectedFields.contains("mentalStateExamination.suicidality.suicidalIdeationDescription"))
    }

    // ── Rule 2 Tests ─────────────────────────────────────────────────────────

    @Test
    fun `rule2 passes when both tlnwl and tsh are documented`() {
        val result = PolicyValidator.rule2(buildSchema())
        assertTrue(result.passed)
    }

    @Test
    fun `rule2 fails when tsh is blank`() {
        val schema = buildSchema(suicidality = baseSuicidality(tsh = ""))
        val result = PolicyValidator.rule2(schema)
        assertFalse(result.passed)
        assertTrue(result.affectedFields.contains("mentalStateExamination.suicidality.tsh"))
    }

    @Test
    fun `rule2 fails when both tlnwl and tsh are null`() {
        val schema = buildSchema(suicidality = baseSuicidality(tlnwl = null, tsh = null))
        val result = PolicyValidator.rule2(schema)
        assertFalse(result.passed)
        assertEquals(2, result.affectedFields.size)
    }

    // ── Rule 3 Tests ─────────────────────────────────────────────────────────

    @Test
    fun `rule3 passes when no children in contact`() {
        val result = PolicyValidator.rule3(buildSchema(childProtection = baseChildProtection(childrenInContact = false)))
        assertTrue(result.passed)
    }

    @Test
    fun `rule3 passes when children in contact and unocini resolved with reason`() {
        val result = PolicyValidator.rule3(buildSchema(childProtection = baseChildProtection(
            childrenInContact = true, unociniRequired = true, unociniReason = "High risk + access to weapons"
        )))
        assertTrue(result.passed)
    }

    @Test
    fun `rule3 fails when children in contact and unocini required but reason missing`() {
        val result = PolicyValidator.rule3(buildSchema(childProtection = baseChildProtection(
            childrenInContact = true, unociniRequired = true, unociniReason = null
        )))
        assertFalse(result.passed)
    }

    // ── Rule 4 Tests ─────────────────────────────────────────────────────────

    @Test
    fun `rule4 passes when both weapons and gun licence explicitly documented as false`() {
        val result = PolicyValidator.rule4(buildSchema(offendingHistory = baseOffendingHistory(false, false)))
        assertTrue(result.passed)
    }

    @Test
    fun `rule4 fails when access to weapons is null`() {
        val result = PolicyValidator.rule4(buildSchema(offendingHistory = baseOffendingHistory(null, false)))
        assertFalse(result.passed)
        assertTrue(result.affectedFields.contains("offendingHistory.accessToWeapons"))
    }

    @Test
    fun `rule4 fails when both fields are null`() {
        val result = PolicyValidator.rule4(buildSchema(offendingHistory = baseOffendingHistory(null, null)))
        assertFalse(result.passed)
        assertEquals(2, result.affectedFields.size)
    }

    // ── Rule 5 Tests ─────────────────────────────────────────────────────────

    @Test
    fun `rule5 passes when strengths and risk both present`() {
        val result = PolicyValidator.rule5(buildSchema(pisani = basePisani("Good family support", "History of self-harm")))
        assertTrue(result.passed)
        assertEquals(Severity.SOFT_FLAG, result.severity)
    }

    @Test
    fun `rule5 fails when risk present but strengths blank`() {
        val result = PolicyValidator.rule5(buildSchema(pisani = basePisani(strengths = "", longTermRisk = "History of violence")))
        assertFalse(result.passed)
    }

    @Test
    fun `rule5 passes when both risk and strengths are blank`() {
        // No risk documented → rule does not fire
        val result = PolicyValidator.rule5(buildSchema(pisani = basePisani(strengths = "", longTermRisk = "")))
        assertTrue(result.passed)
    }

    // ── Rule 6 Tests ─────────────────────────────────────────────────────────

    @Test
    fun `rule6 passes when no medications prescribed`() {
        val result = PolicyValidator.rule6(buildSchema(medications = baseMedications(emptyList())))
        assertTrue(result.passed)
    }

    @Test
    fun `rule6 passes when medications present and at least one has compliance documented`() {
        val meds = listOf(
            Medication("Sertraline", "50mg", "once daily", complianceIssues = "Good compliance"),
            Medication("Diazepam", "5mg", "PRN", complianceIssues = null)
        )
        val result = PolicyValidator.rule6(buildSchema(medications = baseMedications(meds)))
        assertTrue(result.passed)
    }

    @Test
    fun `rule6 flags when medications present but all compliance fields are null`() {
        val meds = listOf(
            Medication("Sertraline", "50mg", "once daily", complianceIssues = null),
            Medication("Quetiapine", "100mg", "nocte", complianceIssues = null)
        )
        val result = PolicyValidator.rule6(buildSchema(medications = baseMedications(meds)))
        assertFalse(result.passed)
        assertEquals(Severity.SOFT_FLAG, result.severity)
    }

    // ── Rule 7 Tests ─────────────────────────────────────────────────────────

    @Test
    fun `rule7 passes when signature captured with timestamp`() {
        val result = PolicyValidator.rule7(buildSchema(safetyPlan = baseSafetyPlan(true, "2026-05-12T09:00:00Z")))
        assertTrue(result.passed)
    }

    @Test
    fun `rule7 fails when signature not captured`() {
        val result = PolicyValidator.rule7(buildSchema(safetyPlan = baseSafetyPlan(false, null)))
        assertFalse(result.passed)
        assertEquals(Severity.HARD_BLOCK, result.severity)
    }

    @Test
    fun `rule7 fails when signature captured but timestamp is null`() {
        val result = PolicyValidator.rule7(buildSchema(safetyPlan = baseSafetyPlan(true, null)))
        assertFalse(result.passed)
        assertTrue(result.affectedFields.contains("safetyPlan.nurseSignatureTimestamp"))
    }

    // ── Rule 8 Tests ─────────────────────────────────────────────────────────

    @Test
    fun `rule8 passes when 48hr suicide events explicitly recorded as false`() {
        val result = PolicyValidator.rule8(buildSchema(presentingSuicideEvents = PresentingSuicideEvents(occurred = false)))
        assertTrue(result.passed)
    }

    @Test
    fun `rule8 passes when 48hr suicide events explicitly recorded as true with description`() {
        val result = PolicyValidator.rule8(buildSchema(presentingSuicideEvents = PresentingSuicideEvents(
            occurred = true, description = "Overdose attempt Sunday evening", method = "Medication", intentLevel = "High"
        )))
        assertTrue(result.passed)
    }

    @Test
    fun `rule8 fails when 48hr suicide events are null (not assessed)`() {
        val result = PolicyValidator.rule8(buildSchema(presentingSuicideEvents = PresentingSuicideEvents(occurred = null)))
        assertFalse(result.passed)
        assertEquals(Severity.HARD_BLOCK, result.severity)
    }

    // ── Rule 9 Tests ─────────────────────────────────────────────────────────

    @Test
    fun `rule9 passes when collateral was obtained`() {
        val result = PolicyValidator.rule9(buildSchema(collateralObtained = true))
        assertTrue(result.passed)
        assertEquals(Severity.SOFT_FLAG, result.severity)
    }

    @Test
    fun `rule9 passes when collateral not obtained but reason documented`() {
        val result = PolicyValidator.rule9(buildSchema(collateralObtained = false, collateralDeclinedReason = "Patient declined consent"))
        assertTrue(result.passed)
    }

    @Test
    fun `rule9 flags when collateral not obtained and no reason documented`() {
        val result = PolicyValidator.rule9(buildSchema(collateralObtained = false, collateralDeclinedReason = null))
        assertFalse(result.passed)
    }

    // ── Rule 10 Tests ────────────────────────────────────────────────────────

    @Test
    fun `rule10 passes when all three crisis numbers are correct`() {
        val result = PolicyValidator.rule10(buildSchema(safetyPlan = baseSafetyPlan()))
        assertTrue(result.passed)
    }

    @Test
    fun `rule10 fails when lifeline number is wrong`() {
        val result = PolicyValidator.rule10(buildSchema(safetyPlan = baseSafetyPlan(lifeline = "0800000000")))
        assertFalse(result.passed)
        assertEquals(Severity.HARD_BLOCK, result.severity)
    }

    @Test
    fun `rule10 fails when all three crisis numbers are missing`() {
        val result = PolicyValidator.rule10(buildSchema(safetyPlan = baseSafetyPlan(
            lifeline = "", beldoc = "", sebdoc = ""
        )))
        assertFalse(result.passed)
        assertEquals(3, result.affectedFields.size)
    }
}
