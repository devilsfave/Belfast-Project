package com.belfasttrust.jpclinical.domain.testdata

import com.belfasttrust.jpclinical.schema.*

// ─────────────────────────────────────────────────────────────────────────────
// SYNTHETIC NOTE FACTORY — 10 profiles for Phase 8 Gate Testing
//
// Each profile returns a fully populated MasterSchema representing a different
// clinical scenario. These are used to:
//  1. Test SafetyPlanMapper and PisaniMapper outputs
//  2. Validate PolicyValidator rules fire correctly
//  3. Give JP real-looking form outputs for clinical review
//
// RULES:
//  • All data is SYNTHETIC — no real patients. Names are fictional.
//  • Each profile covers a different risk level and diagnosis.
//  • Profiles 6-10 deliberately trigger specific Policy Rules to test safety controls.
// ─────────────────────────────────────────────────────────────────────────────

object SyntheticNoteFactory {

    fun getAllProfiles(): List<MasterSchema> = listOf(
        profile1LowRiskDepression(),
        profile2MediumRiskAnxiety(),
        profile3HighRiskPTSD(),
        profile4VeryHighRiskPsychosis(),
        profile5MediumRiskSubstanceMisuse(),
        profile6ChildrenInHomeUnocini(),
        profile7WeaponsAccess(),
        profile8MissingSuicidality(),
        profile9MedicationNoCompliance(),
        profile10CollateralDeclined()
    )

    // ── Shared helpers ───────────────────────────────────────────────────────

    private fun basePatient(name: String, hcNumber: String) = Patient(
        fullName = name, hcNumber = hcNumber, dateOfBirth = "1975-03-15",
        address = "42 Falls Road, Belfast BT12 4PD",
        gpName = "Dr Gilleland", gpAddress = "University Street Surgery",
        gpPhone = "02890311118", placeOfAssessment = "Patient's home"
    )

    private fun baseReferral() = Referral(
        referralAgent = "GP", referralSource = "GP Referral",
        referralDate = "2026-05-10", assessorName = "JP",
        assessorDesignation = "Mental Health Nurse"
    )

    private fun baseConfidentiality() = Confidentiality(confidentialityExplained = true)

    private fun baseCollateral(obtained: Boolean = true, reason: String? = null) = Collateral(
        collateralObtained = obtained, collateralDeclinedReason = reason,
        collateralSources = if (obtained) listOf(
            CollateralSource("Wife", "Spouse", "Corroborates patient account, notes mood decline since July")
        ) else emptyList()
    )

    private fun baseSuicidality(
        tlnwl: String? = "No TLNWL expressed",
        tsh: String? = "No TSH reported",
        ideation: Boolean = false,
        attempt: Boolean = false
    ) = Suicidality(
        tlnwl = tlnwl, tsh = tsh,
        suicidalIdeationPresent = ideation,
        suicidalIdeationDescription = if (ideation) "Passive thoughts, no specific plan" else null,
        planPresent = false, planDescription = null,
        intentPresent = false, intentDescription = null,
        selfHarmPlan = null,
        suicideAttemptThisPresentation = attempt,
        adviceGivenOnReattemptRisk = "Standard safety advice given regarding no safe limits with self-poisoning",
        overallNarrative = if (ideation) "Patient reports passive suicidal ideation without plan or intent" else "Patient denied suicidal ideation"
    )

    private fun baseSafetyPlan(signed: Boolean = true) = SafetyPlan(
        step1WarningSignals = "Low mood; Withdrawing from family; Poor sleep",
        step2InternalCopingStrategies = "Breathing exercises; Walk in the park; Listen to music",
        step3SocialSettingsForDistraction = "Name: Sister, Place: Her house; Name: Church group, Place: Parish hall",
        step4PeopleToAskForHelp = listOf(
            SafetyContact("Sister Mary", "07700900001"),
            SafetyContact("Brother Sean", "07700900002")
        ),
        step5ProfessionalsAndAgencies = Step5Professionals(
            gpName = "Dr Gilleland", gpPhone = "02890311118"
        ),
        step6MakingEnvironmentSafe = "Wife stores all medications in locked cabinet; No weapons in home",
        mostImportantReasonToLive = "My grandchildren",
        followUpCallAgreed = true,
        followUpCallDatetime = "2026-05-13T10:00:00Z",
        nurseSignatureCaptured = signed,
        nurseSignatureTimestamp = if (signed) "2026-05-12T15:30:00Z" else null
    )

    private fun refusedSafetyPlan() = SafetyPlan(
        step1WarningSignals = "INCOMPLETE",
        step2InternalCopingStrategies = "INCOMPLETE",
        step3SocialSettingsForDistraction = "INCOMPLETE",
        step4PeopleToAskForHelp = listOf(SafetyContact("INCOMPLETE", "INCOMPLETE")),
        step5ProfessionalsAndAgencies = Step5Professionals(
            gpName = "INCOMPLETE",
            gpPhone = "INCOMPLETE",
            gpOohBeldoc = "INCOMPLETE",
            gpOohSebdoc = "INCOMPLETE",
            lifeline247 = "INCOMPLETE",
            otherContacts = listOf("INCOMPLETE")
        ),
        step6MakingEnvironmentSafe = "INCOMPLETE",
        mostImportantReasonToLive = "INCOMPLETE",
        followUpCallAgreed = false,
        followUpCallDatetime = null,
        nurseSignatureCaptured = false,
        nurseSignatureTimestamp = null
    )

    private fun basePisani(risk: RiskLevel, strengths: String = "Good family support, future-orientated") = PisaniRiskAssessment(
        strengthsAndProtectiveFactors = strengths,
        longTermRiskFactors = "Family history of depression. Previous episode 2019.",
        impulsivityAndSelfControl = "Good impulse control, no history of impulsive behaviours",
        pastSuicidalBehaviours = "One episode of self-harm 2019, superficial lacerations",
        recentAndPresentSuicidalBehaviours = "None at this assessment",
        stressorsAndPrecipitants = "Recent bereavement, financial stress",
        symptomsSufferingRecentChanges = "Improved sleep over past week, appetite returning",
        engagementAndReliability = "Fully engaged with HTT, attends all appointments",
        overallRiskLevel = risk
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

    private fun baseSubstanceMisuse() = SubstanceMisuse(
        currentAlcoholUse = AlcoholUse(false, null, null, null, false, false),
        currentDrugUse = DrugUse(false, emptyList(), null, null, false, false, false),
        complexFactors = SubstanceComplexFactors(false, false),
        auditScore = AuditScore(), ldqScore = LdqScore()
    )

    private fun baseChildProtection(children: Boolean = false) = ChildProtection(
        childrenInRegularContact = children, children = emptyList(),
        partnerDetails = null, fccSocialServicesInvolved = false,
        unociniReferralRequired = false, unociniTriggerReason = null
    )

    private fun baseMSE(suicidality: Suicidality = baseSuicidality()) = MentalStateExamination(
        appearanceBehaviour = AppearanceBehaviour("appropriate", "adequate", "calm", "good", "maintained", "Well-presented, kempt, good eye contact"),
        speechThoughtForm = SpeechThoughtForm("spontaneous", "coherent", "normal", "normal", "normal", false, null, "Normal speech, no thought disorder"),
        moodAffect = MoodAffect("low", "depressed", "congruent", MoodClassification.DEPRESSED, false, null, "reactive", "Low mood, congruent affect"),
        suicidality = suicidality,
        thoughtContent = ThoughtContent(null, null, false, null, null, false, false, false, null, "No abnormal thought content"),
        perceptualDisturbances = PerceptualDisturbances(false, null, false, null, false, false, false, "No perceptual disturbances"),
        cognition = Cognition(true, true, true, true, true, false, null, "Fully orientated, attention intact"),
        insight = Insight("full awareness", "willing to engage", InsightLevel.FULL, "Good insight, willing to engage")
    )

    private fun baseHistory() = History(
        mentalHealthHistory = MentalHealthHistory(diagnosis = "Major Depressive Disorder", mhoUse = false),
        personalHistory = PersonalHistory(earlyChildhood = "Normal childhood, no trauma"),
        familyHistory = FamilyHistory(
            livingArrangements = "Lives with wife", accommodationType = "Owner-occupied house",
            abilityToManageIndependently = "Independent with support from wife", carersInvolved = true,
            carerDetails = "Wife Nuala", accessToLethalMeans = false,
            familyHistorySuicide = false, familyHistoryAddictions = false, familyHistoryMentalIllness = true
        )
    )

    private fun baseEpicNote() = EpicContactNote(
        typeOfContact = "Home visit", purposeOfContact = "Review mental state, mood, risk",
        contactDetailsOverview = "Patient reviewed at home. Cooperative and engaged.",
        riskSummary = "No TLNWL, No TSH. Medium risk.", plan = "Continue current plan, medics to r/v meds"
    )

    private fun buildSchema(
        name: String, hcNumber: String, risk: RiskLevel,
        suicidality: Suicidality = baseSuicidality(),
        safetyPlan: SafetyPlan = baseSafetyPlan(),
        childProtection: ChildProtection = baseChildProtection(),
        offendingHistory: OffendingHistory = OffendingHistory(accessToWeapons = false, gunLicence = false),
        pisani: PisaniRiskAssessment = basePisani(risk),
        medications: Medications = baseMedications(listOf(Medication("Sertraline", "100mg", "once daily", complianceIssues = "Good compliance"))),
        collateral: Collateral = baseCollateral(),
        suicideEvents: PresentingSuicideEvents = PresentingSuicideEvents(occurred = false),
        confidenceScores: Map<String, Float> = emptyMap()
    ) = MasterSchema(
        sessionId = "synth-${hcNumber}", nurseId = "jp-nurse-001",
        assessmentDate = "2026-05-12", assessmentTime = "14:00",
        visitType = VisitType.REVIEW,
        patient = basePatient(name, hcNumber),
        referral = baseReferral(),
        confidentiality = baseConfidentiality(),
        collateral = collateral,
        historyOfPresentingComplaint = HistoryOfPresentingComplaint(
            "Low mood", "Relationship breakdown", "3 weeks",
            "Depressive episode", "GP prescribed sertraline", suicideEvents
        ),
        mentalStateExamination = baseMSE(suicidality),
        history = baseHistory(),
        abuse = Abuse(false, false, null),
        offendingHistory = offendingHistory,
        socialCircumstances = SocialCircumstances(housing = "Owner-occupied"),
        medications = medications,
        substanceMisuse = baseSubstanceMisuse(),
        childProtection = childProtection,
        pisaniRiskAssessment = pisani,
        safetyPlan = safetyPlan,
        epicContactNote = baseEpicNote(),
        fieldConfidenceScores = confidenceScores
    )

    // ── 10 Profiles ──────────────────────────────────────────────────────────

    /** Profile 1: Low risk depression — all fields clean, all rules pass */
    fun profile1LowRiskDepression() = buildSchema(
        name = "Ciaran O'Donnell", hcNumber = "HC200001", risk = RiskLevel.LOW,
        safetyPlan = baseSafetyPlan().copy(
            step1WarningSignals = "Mood dropping in evenings; Cancelling family calls; Poor sleep",
            step2InternalCopingStrategies = "Gardening for 20 minutes; Listening to Irish folk music; Cup of tea with Nuala",
            step3SocialSettingsForDistraction = "Name: Grandchildren, Place: Family kitchen; Name: Nuala, Place: Short walk on Falls Road",
            step4PeopleToAskForHelp = listOf(
                SafetyContact("Wife Nuala", "07700900101")
            ),
            step6MakingEnvironmentSafe = "Wife Nuala stores medications; No weapons in home",
            mostImportantReasonToLive = "Grandchildren"
        )
    )

    /** Profile 2: Medium risk anxiety — includes panic features */
    fun profile2MediumRiskAnxiety() = buildSchema(
        name = "Siobhan McAllister", hcNumber = "HC200002", risk = RiskLevel.MEDIUM,
        safetyPlan = baseSafetyPlan().copy(
            step1WarningSignals = "Panic attacks; Hyperventilating; Withdrawing from friends",
            step2InternalCopingStrategies = "Breathing exercises; Short walks; Calling sister",
            step3SocialSettingsForDistraction = "Name: Sister, Place: Her garden; Name: Friend Claire, Place: Quiet coffee shop",
            step4PeopleToAskForHelp = listOf(
                SafetyContact("Sister", "07700900102")
            ),
            step6MakingEnvironmentSafe = "Medications stored safely by Siobhan herself; Keeps only current weekly supply accessible",
            mostImportantReasonToLive = "Independence, family"
        ),
        pisani = basePisani(RiskLevel.MEDIUM).copy(
            stressorsAndPrecipitants = "COVID-19 fears, shielding since March, loss of independence",
            symptomsSufferingRecentChanges = "Panic attacks 3x weekly, hyperventilation, chest tightness"
        )
    )

    /** Profile 3: High risk PTSD — passive TLNWL with trauma history */
    fun profile3HighRiskPTSD() = buildSchema(
        name = "Patrick Byrne", hcNumber = "HC200003", risk = RiskLevel.HIGH,
        suicidality = baseSuicidality(
            tlnwl = "Passive TLNWL expressed — 'some days I feel life isn't worth it'",
            tsh = "No TSH reported", ideation = true
        ),
        safetyPlan = baseSafetyPlan().copy(
            step1WarningSignals = "Flashbacks; Social withdrawal; Sleep disturbance",
            step2InternalCopingStrategies = "Grounding exercise naming five objects; Breathing routine; Sitting with family photographs",
            step3SocialSettingsForDistraction = "Name: Grandchildren, Place: Family visit; Name: Nuala, Place: Living room",
            step4PeopleToAskForHelp = listOf(
                SafetyContact("Wife Nuala", "07700900103"),
                SafetyContact("GP Dr Gilleland", "02890311118")
            ),
            step6MakingEnvironmentSafe = "Wife Nuala stores all medications; Weapons already surrendered to PSNI",
            mostImportantReasonToLive = "Grandchildren"
        ),
        pisani = basePisani(RiskLevel.HIGH).copy(
            longTermRiskFactors = "25-year trauma history, uncle died by suicide (hanging), family hx depression",
            pastSuicidalBehaviours = "Uncle SI by hanging. Patient handed guns to PSNI 12 years ago.",
            recentAndPresentSuicidalBehaviours = "Passive ideation, no plan or intent"
        )
    )

    /** Profile 4: Very high risk psychosis — active SI with plan */
    fun profile4VeryHighRiskPsychosis() = buildSchema(
        name = "Declan Murphy", hcNumber = "HC200004", risk = RiskLevel.VERY_HIGH,
        suicidality = baseSuicidality(
            tlnwl = "Active TLNWL — 'I'd be better off dead'",
            tsh = "TSH present — has been cutting arms",
            ideation = true, attempt = false
        ),
        safetyPlan = baseSafetyPlan().copy(
            step1WarningSignals = "Thoughts of overdose; Self-harm urges; Isolating",
            step2InternalCopingStrategies = "Use crisis card; Move to shared room with wife; Cold water grounding",
            step3SocialSettingsForDistraction = "Name: Wife, Place: Kitchen table; Name: Brother, Place: Phone call",
            step4PeopleToAskForHelp = listOf(
                SafetyContact("Wife", "07700900104")
            ),
            step6MakingEnvironmentSafe = "Wife locks all medications in cabinet; Psychiatric review pending",
            mostImportantReasonToLive = "Wife and children"
        ),
        pisani = basePisani(RiskLevel.VERY_HIGH).copy(
            impulsivityAndSelfControl = "Poor impulse control, acts without considering consequences",
            recentAndPresentSuicidalBehaviours = "Active SI with vague plan (medication overdose). No immediate intent.",
            engagementAndReliability = "Partially engaged, misses some appointments"
        )
    )

    /** Profile 5: Medium risk with active substance misuse — AUDIT/LDQ scores populated */
    fun profile5MediumRiskSubstanceMisuse() = buildSchema(
        name = "Roisin Kelly", hcNumber = "HC200005", risk = RiskLevel.MEDIUM,
        safetyPlan = baseSafetyPlan().copy(
            step1WarningSignals = "Mood swings; Urge to drink; Missing medication",
            step2InternalCopingStrategies = "Attending AA; Calling sponsor; Not buying alcohol",
            step3SocialSettingsForDistraction = "Name: AA group, Place: Community hall; Name: Sober friend, Place: Evening phone call",
            step4PeopleToAskForHelp = listOf(
                SafetyContact("AA sponsor", "07700900105")
            ),
            step6MakingEnvironmentSafe = "Mirtazapine and Diazepam stored by family member; No alcohol kept in home",
            mostImportantReasonToLive = "Her children"
        ),
        medications = baseMedications(listOf(
            Medication("Mirtazapine", "45mg", "nocte", complianceIssues = "Compliance poor — misses doses when drinking"),
            Medication("Diazepam", "5mg", "PRN", complianceIssues = null)
        ))
    )

    /** Profile 6: Medium risk WITH children — triggers UNOCINI check (Rule 3) */
    fun profile6ChildrenInHomeUnocini() = buildSchema(
        name = "Aisling Doherty", hcNumber = "HC200006", risk = RiskLevel.MEDIUM,
        safetyPlan = baseSafetyPlan().copy(
            step1WarningSignals = "Becoming tearful after school run; Feeling overwhelmed at bedtime; Avoiding calls from family",
            step2InternalCopingStrategies = "Five-minute breathing pause; Make tea and sit in kitchen; Write down immediate worries",
            step3SocialSettingsForDistraction = "Name: Conor Doherty, Place: Kitchen; Name: Children, Place: Supervised family activity",
            step4PeopleToAskForHelp = listOf(
                SafetyContact("Partner Conor Doherty", "07700900106")
            ),
            step6MakingEnvironmentSafe = "Partner stores medications; UNOCINI referral has been made",
            mostImportantReasonToLive = "Son Cian and daughter Aoife"
        ),
        childProtection = ChildProtection(
            childrenInRegularContact = true,
            children = listOf(
                ChildDetail("Cian Doherty", "M", "2018-06-15", "Son"),
                ChildDetail("Aoife Doherty", "F", "2020-11-22", "Daughter")
            ),
            partnerDetails = "Partner: Conor Doherty, 15 Glen Road",
            fccSocialServicesInvolved = false,
            unociniReferralRequired = true,
            unociniTriggerReason = "Medium risk + children in regular contact"
        )
    )

    /** Profile 7: High risk WITH weapons access — triggers Rule 4 */
    fun profile7WeaponsAccess() = buildSchema(
        name = "Robert Wilson", hcNumber = "HC200007", risk = RiskLevel.HIGH,
        offendingHistory = OffendingHistory(
            forensicHistory = "No criminal record",
            accessToWeapons = true,
            gunLicence = true,
            gunLicenceDetails = "Registered shotgun for clay pigeon shooting. Stored in locked cabinet."
        ),
        safetyPlan = baseSafetyPlan().copy(
            step1WarningSignals = "Deep isolation; Not leaving house; Dark thoughts",
            step2InternalCopingStrategies = "Open curtains and make tea; Phone clay pigeon club contact; Sit outside for ten minutes",
            step3SocialSettingsForDistraction = "Name: Nearest neighbour, Place: Farm lane check-in; Name: Local clay pigeon club, Place: Clubhouse",
            step4PeopleToAskForHelp = listOf(
                SafetyContact("GP Dr Gilleland", "02890311118"),
                SafetyContact("Neighbour", "07700900107")
            ),
            step6MakingEnvironmentSafe = "Shotgun licence surrendered to PSNI; Medications stored with neighbour until psychiatric review",
            mostImportantReasonToLive = "Clay pigeon shooting, sense of purpose"
        ),
        pisani = basePisani(RiskLevel.HIGH).copy(
            longTermRiskFactors = "Access to firearms, isolated rural location, recent divorce"
        )
    )

    /** Profile 8: Deliberately MISSING suicidality fields — triggers Rules 1, 2, 8 */
    fun profile8MissingSuicidality() = buildSchema(
        name = "Brendan McCann", hcNumber = "HC200008", risk = RiskLevel.MEDIUM,
        suicidality = baseSuicidality(tlnwl = null, tsh = null),
        safetyPlan = refusedSafetyPlan(),
        suicideEvents = PresentingSuicideEvents(occurred = null)
    )

    /** Profile 9: Medications but ALL compliance fields null — triggers Rule 6 */
    fun profile9MedicationNoCompliance() = buildSchema(
        name = "Mairead Quinn", hcNumber = "HC200009", risk = RiskLevel.LOW,
        safetyPlan = baseSafetyPlan().copy(
            step1WarningSignals = "Forgetting evening tablets; Sleeping through morning routine; Becoming muddled with blister pack",
            step2InternalCopingStrategies = "Use written medication checklist; Sit with a cup of tea; Phone daughter before bedtime",
            step3SocialSettingsForDistraction = "Name: Daughter, Place: Her house; Name: Grandchildren, Place: Weekend visit",
            step4PeopleToAskForHelp = listOf(
                SafetyContact("Daughter", "07700900109")
            ),
            step6MakingEnvironmentSafe = "Daughter stores medications; Weekly blister pack checked at family visit",
            mostImportantReasonToLive = "Daughter and grandchildren"
        ),
        medications = baseMedications(listOf(
            Medication("Olanzapine", "5mg", "nocte", complianceIssues = null),
            Medication("Sertraline", "50mg", "once daily", complianceIssues = null)
        ))
    )

    /** Profile 10: Collateral declined with no reason — triggers Rule 9 */
    fun profile10CollateralDeclined() = buildSchema(
        name = "Connor Gallagher", hcNumber = "HC200010", risk = RiskLevel.LOW,
        safetyPlan = baseSafetyPlan().copy(
            step1WarningSignals = "Closing down discussion; Irritability when collateral discussed; Spending evenings alone",
            step2InternalCopingStrategies = "Step away from argument; Breathing routine; Watch familiar television programme",
            step3SocialSettingsForDistraction = "Name: Wife Nuala, Place: Kitchen; Name: Family, Place: Sunday meal",
            step4PeopleToAskForHelp = listOf(
                SafetyContact("Wife Nuala", "07700900110")
            ),
            step6MakingEnvironmentSafe = "Wife Nuala stores medications; Crisis plan reviewed despite collateral declined",
            mostImportantReasonToLive = "Wife Nuala and family"
        ),
        collateral = baseCollateral(obtained = false, reason = null)
    )
}
