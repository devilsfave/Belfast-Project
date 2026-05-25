package com.belfasttrust.jpclinical.domain.extraction

object ExtractionPromptBuilder {
    private const val GLOSSARY = """
BELFAST TRUST CLINICAL ABBREVIATIONS GLOSSARY:
- HTT: Home Treatment Team
- TLNWL: Thoughts of Life Not Worth Living
- TSH: Thoughts of Self Harm
- MSE: Mental State Examination
- PISANI: Risk assessment framework (8 domains)
- UNOCINI: Understanding the Needs of Children in Northern Ireland (referral form)
- MHO: Mental Health Officer
- FCC: Family and Children's Carers
- AUDIT: Alcohol Use Disorders Identification Test
- LDQ: Leeds Dependence Questionnaire
- CAT: Community Addictions Team
- SOAP: Subjective Objective Assessment Plan (note format)
- H&C: Health and Care (patient ID number)
- BelDOC: Belfast Doctor on Call (GP out of hours, 02890744447)
- SEBDOC: South and East Belfast Doctor on Call (02890796220)
- OOH: Out of Hours
- NMC: Nursing and Midwifery Council
- FHIR: Fast Healthcare Interoperability Resources
- EHR: Electronic Health Record
- IG: Information Governance
- DPA: Data Processing Agreement
"""

    private const val EXTRACTION_RULES = """
EXTRACTION RULES:

Rule 1: Output ONLY valid JSON. No prose. No markdown. No explanation. Raw JSON only.

Rule 2: Every field must have a companion confidence field. Example:
  "tlnwl": "patient denied TLNWL",
  "tlnwl_confidence": 0.94

Rule 3: If confidence is below 0.80, set the value to null and add: "fieldname_judgment_required": true

Rule 4 CRITICAL: Negation means absence. If notes say "no suicidal ideation" or "denied TLNWL" the suicidality fields must reflect ABSENT. Discussing a symptom is not the same as its presence.

Rule 5: PISANI fields for impulsivity_and_self_control and engagement_and_reliability must ALWAYS include _judgment_required: true regardless of confidence. These always go to the clarification queue.

Rule 6: NEVER populate safety_plan.step5_professionals from the notes. Leave all fields in that object null. They are pre-filled by the system with pre-printed crisis numbers.
"""

    fun buildCluster1Prompt(rawNotes: String): String {
        return """
You are MedGemma, a specialized clinical AI assistant. Your task is to extract structured clinical information from the raw nursing notes provided.
You will extract fields for Cluster 1: patient demographics, referral details, confidentiality section, collateral section, history of presenting complaint including the 48-hour suicide events subsection.

$GLOSSARY

$EXTRACTION_RULES

RAW NOTES:
$rawNotes
""".trimIndent()
    }

    fun buildCluster2Prompt(rawNotes: String, cluster1Json: String): String {
        return """
You are MedGemma, a specialized clinical AI assistant. Your task is to extract structured clinical information from the raw nursing notes provided, building upon previously extracted Cluster 1 information.
You will extract fields for Cluster 2: full Mental State Examination all 8 subdomains, medication section including the 8-criterion autonomy table.

$GLOSSARY

$EXTRACTION_RULES

CLUSTER 1 EXTRACTED JSON:
$cluster1Json

RAW NOTES:
$rawNotes
""".trimIndent()
    }

    fun buildCluster3Prompt(rawNotes: String, cluster1Json: String, cluster2Json: String): String {
        return """
You are MedGemma, a specialized clinical AI assistant. Your task is to extract structured clinical information from the raw nursing notes provided, building upon previously extracted Cluster 1 and Cluster 2 information.
You will extract fields for Cluster 3: PISANI risk assessment all 8 domains, substance misuse, AUDIT questions, LDQ questions, child protection details, social circumstances, safety plan all 6 steps.

$GLOSSARY

$EXTRACTION_RULES

CLUSTER 1 EXTRACTED JSON:
$cluster1Json

CLUSTER 2 EXTRACTED JSON:
$cluster2Json

RAW NOTES:
$rawNotes
""".trimIndent()
    }

    fun buildMasterSchemaPrompt(
        rawNotes: String,
        hcNumber: String,
        sessionId: String,
        nurseId: String,
        cluster1Json: String,
        cluster2Json: String,
        cluster3Json: String,
        clarificationAnswers: Map<String, String> = emptyMap()
    ): String {
        val answersBlock = if (clarificationAnswers.isEmpty()) {
            "No nurse clarification answers have been supplied yet."
        } else {
            clarificationAnswers.entries.joinToString("\n") { (field, answer) ->
                "- $field: $answer"
            }
        }

        return """
You are MedGemma, a specialized clinical AI assistant. Convert the three extraction cluster JSON blocks into ONE final Kotlin MasterSchema JSON object for Belfast Clinical AI.

$GLOSSARY

$EXTRACTION_RULES

STRICT OUTPUT CONTRACT:
- Output only one raw JSON object.
- Use camelCase property names exactly as shown in the Kotlin MasterSchema model.
- Root values must include:
  sessionId: "$sessionId"
  nurseId: "$nurseId"
  schemaVersion: "1.0"
  schemaFinalised: false
  extractionCluster: 3
- If the H&C number below is non-blank, use it for patient.hcNumber unless the notes explicitly provide a different H&C number.
- Required enum tokens must use these exact uppercase values:
  visitType: NEW_ASSESSMENT, REVIEW, CRISIS
  moodClassification: DEPRESSED, EUTHYMIC, ELATED, MIXED
  insightLevel: FULL, PARTIAL, NONE
  medication autonomy: INDEPENDENT, ASSISTED, DEPENDENT, NOT_APPLICABLE
  dependenceLevel: LOW, MEDIUM, HIGH
  overallRiskLevel: LOW, MEDIUM, HIGH, VERY_HIGH
- Use fieldConfidenceScores as a flat object of dot-path to number, for example:
  "pisaniRiskAssessment.impulsivityAndSelfControl": 0.0
- PISANI impulsivityAndSelfControl and engagementAndReliability must be null unless the nurse explicitly supplied a clinical judgment answer.
- Never invent crisis numbers. Safety plan step 5 must keep BelDOC 02890744447, SEBDOC 02890796220, and Lifeline 0808 808 8000 as system pre-printed values.
- Do not include companion "_confidence" fields beside normal values. Put all confidence scores in fieldConfidenceScores only.
- If a required non-null text field is genuinely not documented, use an empty string and set that field's confidence score to 0.0. Do not write plausible filler.

SESSION ID:
$sessionId

NURSE ID:
$nurseId

H&C NUMBER ENTERED BY NURSE:
$hcNumber

NURSE CLARIFICATION ANSWERS:
$answersBlock

CLUSTER 1 EXTRACTED JSON:
$cluster1Json

CLUSTER 2 EXTRACTED JSON:
$cluster2Json

CLUSTER 3 EXTRACTED JSON:
$cluster3Json

RAW NOTES:
$rawNotes
""".trimIndent()
    }
}
