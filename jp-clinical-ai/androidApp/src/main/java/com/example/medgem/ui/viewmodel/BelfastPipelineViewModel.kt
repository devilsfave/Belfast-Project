package com.example.medgem.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belfasttrust.jpclinical.android.data.db.AuditEntryEntity
import com.belfasttrust.jpclinical.domain.AuditEventType
import com.belfasttrust.jpclinical.domain.extraction.ExtractionPromptBuilder
import com.belfasttrust.jpclinical.domain.mappers.AbuseMapper
import com.belfasttrust.jpclinical.domain.mappers.AuditMapper
import com.belfasttrust.jpclinical.domain.mappers.ChildProtectionMapper
import com.belfasttrust.jpclinical.domain.mappers.CollateralMapper
import com.belfasttrust.jpclinical.domain.mappers.ConfidentialityMapper
import com.belfasttrust.jpclinical.domain.mappers.EmergencyAssessmentMapper
import com.belfasttrust.jpclinical.domain.mappers.EpicContactNoteMapper
import com.belfasttrust.jpclinical.domain.mappers.FamilyHistoryMapper
import com.belfasttrust.jpclinical.domain.mappers.HistoryPresentingComplaintMapper
import com.belfasttrust.jpclinical.domain.mappers.LdqMapper
import com.belfasttrust.jpclinical.domain.mappers.MedicationsMapper
import com.belfasttrust.jpclinical.domain.mappers.MentalHealthHistoryMapper
import com.belfasttrust.jpclinical.domain.mappers.MentalStateExaminationMapper
import com.belfasttrust.jpclinical.domain.mappers.OccupationalNeedsMapper
import com.belfasttrust.jpclinical.domain.mappers.OffendingHistoryMapper
import com.belfasttrust.jpclinical.domain.mappers.PersonalHistoryMapper
import com.belfasttrust.jpclinical.domain.mappers.PisaniMapper
import com.belfasttrust.jpclinical.domain.mappers.SafetyPlanMapper
import com.belfasttrust.jpclinical.domain.mappers.SocialCircumstancesMapper
import com.belfasttrust.jpclinical.domain.mappers.SubstanceMisuseMapper
import com.belfasttrust.jpclinical.domain.validator.PolicyValidator
import com.belfasttrust.jpclinical.domain.validator.Severity
import com.belfasttrust.jpclinical.domain.validator.ValidationResult
import com.belfasttrust.jpclinical.schema.MasterSchema
import com.example.medgem.LlmInferenceService
import com.example.medgem.LlmInferenceService.LlmGenerationEvent
import com.example.medgem.ui.screens.ClarificationQueueItem
import com.example.medgem.ui.screens.ReviewField
import com.example.medgem.ui.screens.ReviewForm
import com.example.medgem.ui.screens.ReviewIssue
import dagger.hilt.android.lifecycle.HiltViewModel
import io.objectbox.Box
import io.objectbox.BoxStore
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import java.util.UUID

enum class BelfastPipelineStage {
    IDLE,
    PROCESSING,
    NEEDS_CLARIFICATION,
    READY_FOR_REVIEW,
    FAILED
}

data class BelfastPipelineUiState(
    val stage: BelfastPipelineStage = BelfastPipelineStage.IDLE,
    val sessionId: String = "",
    val statusMessage: String = "",
    val errorMessage: String? = null,
    val clarificationItems: List<ClarificationQueueItem> = emptyList(),
    val reviewForms: List<ReviewForm> = emptyList(),
    val hardBlocks: List<ReviewIssue> = emptyList(),
    val softFlags: List<ReviewIssue> = emptyList()
)

private data class BelfastPipelineResult(
    val schema: MasterSchema,
    val validationResults: List<ValidationResult>,
    val clarificationItems: List<ClarificationQueueItem>,
    val reviewForms: List<ReviewForm>,
    val hardBlocks: List<ReviewIssue>,
    val softFlags: List<ReviewIssue>
)

@HiltViewModel
class BelfastPipelineViewModel @Inject constructor(
    private val llmInferenceService: LlmInferenceService,
    boxStore: BoxStore
) : ViewModel() {
    private val auditBox: Box<AuditEntryEntity> = boxStore.boxFor(AuditEntryEntity::class.java)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private val _uiState = MutableStateFlow(BelfastPipelineUiState())
    val uiState: StateFlow<BelfastPipelineUiState> = _uiState.asStateFlow()

    private var rawNotes: String = ""
    private var hcNumber: String = ""
    private var sessionId: String = ""
    private val nurseId: String = "JP"

    fun startSession(hcNumber: String, noteText: String) {
        val trimmedNotes = noteText.trim()
        if (trimmedNotes.isBlank()) return

        this.rawNotes = trimmedNotes
        this.hcNumber = hcNumber.trim()
        this.sessionId = UUID.randomUUID().toString()

        runExtraction(clarificationAnswers = emptyMap())
    }

    fun submitClarifications(answers: Map<String, String>) {
        val cleanedAnswers = answers
            .mapValues { it.value.trim() }
            .filterValues { it.isNotBlank() }

        if (rawNotes.isBlank() || sessionId.isBlank()) {
            _uiState.value = BelfastPipelineUiState(
                stage = BelfastPipelineStage.FAILED,
                errorMessage = "No active clinical session found. Please start a new session."
            )
            return
        }

        runExtraction(clarificationAnswers = cleanedAnswers)
    }

    fun resetForNewSession() {
        rawNotes = ""
        hcNumber = ""
        sessionId = ""
        _uiState.value = BelfastPipelineUiState()
    }

    private fun runExtraction(clarificationAnswers: Map<String, String>) {
        _uiState.value = _uiState.value.copy(
            stage = BelfastPipelineStage.PROCESSING,
            statusMessage = "Extracting clinical data...",
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    extractAndMap(clarificationAnswers)
                }

                val nextStage = if (result.clarificationItems.isNotEmpty()) {
                    BelfastPipelineStage.NEEDS_CLARIFICATION
                } else {
                    BelfastPipelineStage.READY_FOR_REVIEW
                }

                _uiState.value = BelfastPipelineUiState(
                    stage = nextStage,
                    sessionId = sessionId,
                    statusMessage = "Forms prepared for nurse review.",
                    clarificationItems = result.clarificationItems,
                    reviewForms = result.reviewForms,
                    hardBlocks = result.hardBlocks,
                    softFlags = result.softFlags
                )
            } catch (t: Throwable) {
                Log.e(TAG, "Belfast extraction failed", t)
                logAudit(
                    AuditEventType.VALIDATION_RESULT,
                    """{"status":"failed","reason":"${t.safeJsonMessage()}"}"""
                )
                _uiState.value = BelfastPipelineUiState(
                    stage = BelfastPipelineStage.FAILED,
                    sessionId = sessionId,
                    errorMessage = t.message ?: "The notes could not be converted into the Belfast master schema."
                )
            }
        }
    }

    private suspend fun extractAndMap(
        clarificationAnswers: Map<String, String>
    ): BelfastPipelineResult {
        logAudit(
            AuditEventType.EXTRACTION_START,
            """{"noteLength":${rawNotes.length},"clarificationAnswerCount":${clarificationAnswers.size}}"""
        )

        val cluster1Json = generateText(
            prompt = ExtractionPromptBuilder.buildCluster1Prompt(rawNotes),
            status = "Extracting patient and referral data..."
        )
        val cluster2Json = generateText(
            prompt = ExtractionPromptBuilder.buildCluster2Prompt(rawNotes, cluster1Json),
            status = "Extracting mental state and medication data..."
        )
        val cluster3Json = generateText(
            prompt = ExtractionPromptBuilder.buildCluster3Prompt(rawNotes, cluster1Json, cluster2Json),
            status = "Extracting risk, social, and safety plan data..."
        )

        val masterSchemaResponse = generateText(
            prompt = ExtractionPromptBuilder.buildMasterSchemaPrompt(
                rawNotes = rawNotes,
                hcNumber = hcNumber,
                sessionId = sessionId,
                nurseId = nurseId,
                cluster1Json = cluster1Json,
                cluster2Json = cluster2Json,
                cluster3Json = cluster3Json,
                clarificationAnswers = clarificationAnswers
            ),
            status = "Preparing the Belfast master schema...",
            maxTokens = MASTER_SCHEMA_MAX_TOKENS
        )

        val masterSchemaJson = extractJsonObject(masterSchemaResponse)
        val schema = json.decodeFromString<MasterSchema>(masterSchemaJson)

        logAudit(
            AuditEventType.EXTRACTION_COMPLETE,
            """{"schemaParsed":true,"cluster":3}"""
        )

        logAudit(AuditEventType.VALIDATION_RUN, """{"ruleCount":10}""")
        val validationResults = PolicyValidator.validate(schema)
        validationResults.forEach { result ->
            logAudit(
                AuditEventType.VALIDATION_RESULT,
                """{"ruleId":${result.ruleId},"passed":${result.passed},"severity":"${result.severity.name}","affectedFields":${json.encodeToString(result.affectedFields)}}"""
            )
        }

        return BelfastPipelineResult(
            schema = schema,
            validationResults = validationResults,
            clarificationItems = buildClarificationItems(schema, validationResults),
            reviewForms = buildReviewForms(schema),
            hardBlocks = validationResults
                .filter { !it.passed && it.severity == Severity.HARD_BLOCK && it.ruleId != RULE_SAFETY_SIGNATURE }
                .map { it.toReviewIssue() },
            softFlags = validationResults
                .filter { !it.passed && it.severity == Severity.SOFT_FLAG }
                .map { it.toReviewIssue() }
        )
    }

    private suspend fun generateText(
        prompt: String,
        status: String,
        maxTokens: Int = CLUSTER_MAX_TOKENS
    ): String {
        withContext(Dispatchers.Main) {
            _uiState.value = _uiState.value.copy(statusMessage = status)
        }

        llmInferenceService.reset()

        val response = StringBuilder()
        val wrappedPrompt = "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"

        llmInferenceService.generateResponseFlow(
            prompt = wrappedPrompt,
            images = emptyList(),
            maxTokens = maxTokens,
            numBos = 1,
            temperature = 0.0f,
            topP = 0.9f
        ).collect { event ->
            when (event) {
                is LlmGenerationEvent.Content -> response.append(event.text)
                is LlmGenerationEvent.Done -> Unit
                is LlmGenerationEvent.Stats -> Unit
            }
        }

        return response.toString()
            .replace("<end_of_turn>", "")
            .trim()
    }

    private fun extractJsonObject(modelResponse: String): String {
        val cleaned = modelResponse
            .replace("```json", "")
            .replace("```", "")
            .trim()
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')

        if (start < 0 || end <= start) {
            throw IllegalStateException(
                "MedGemma did not return a valid JSON object for the Belfast master schema."
            )
        }

        return cleaned.substring(start, end + 1)
    }

    private fun buildClarificationItems(
        schema: MasterSchema,
        validationResults: List<ValidationResult>
    ): List<ClarificationQueueItem> {
        val items = linkedMapOf<String, ClarificationQueueItem>()

        validationResults
            .filter { !it.passed && it.ruleId != RULE_SAFETY_SIGNATURE }
            .forEach { result ->
                val fieldPath = result.affectedFields.firstOrNull() ?: "rule.${result.ruleId}"
                val id = "rule-${result.ruleId}-${fieldPath.toStableId()}"
                items[id] = ClarificationQueueItem(
                    id = id,
                    formName = formNameFor(fieldPath),
                    question = result.toClarificationQuestion(),
                    mandatory = result.severity == Severity.HARD_BLOCK
                )
            }

        addConfidenceClarification(
            items = items,
            fieldPath = "pisaniRiskAssessment.impulsivityAndSelfControl",
            valueMissing = schema.pisaniRiskAssessment.impulsivityAndSelfControl.isNullOrBlank(),
            confidence = schema.fieldConfidenceScores["pisaniRiskAssessment.impulsivityAndSelfControl"] ?: 0.0f,
            question = "Provide your clinical judgment for PISANI domain 3: impulsivity and self-control."
        )
        addConfidenceClarification(
            items = items,
            fieldPath = "pisaniRiskAssessment.engagementAndReliability",
            valueMissing = schema.pisaniRiskAssessment.engagementAndReliability.isNullOrBlank(),
            confidence = schema.fieldConfidenceScores["pisaniRiskAssessment.engagementAndReliability"] ?: 0.0f,
            question = "Provide your clinical judgment for PISANI domain 8: engagement and reliability."
        )

        schema.fieldConfidenceScores
            .filter { (_, confidence) -> confidence < CONFIDENCE_THRESHOLD }
            .forEach { (fieldPath, confidence) ->
                val id = "confidence-${fieldPath.toStableId()}"
                if (!items.containsKey(id)) {
                    items[id] = ClarificationQueueItem(
                        id = id,
                        formName = formNameFor(fieldPath),
                        question = "Confirm or correct the value for ${fieldPath.toReadableLabel()} (confidence ${"%.2f".format(confidence)}).",
                        mandatory = true
                    )
                }
            }

        return items.values.toList()
    }

    private fun addConfidenceClarification(
        items: MutableMap<String, ClarificationQueueItem>,
        fieldPath: String,
        valueMissing: Boolean,
        confidence: Float,
        question: String
    ) {
        if (!valueMissing && confidence >= CONFIDENCE_THRESHOLD) return

        val id = "judgment-${fieldPath.toStableId()}"
        items[id] = ClarificationQueueItem(
            id = id,
            formName = formNameFor(fieldPath),
            question = question,
            mandatory = true
        )
    }

    private fun buildReviewForms(schema: MasterSchema): List<ReviewForm> = listOf(
        reviewForm("emergency-assessment", "Emergency Assessment", EmergencyAssessmentMapper.map(schema)),
        reviewForm("confidentiality", "Confidentiality", ConfidentialityMapper.map(schema)),
        reviewForm("collateral", "Collateral", CollateralMapper.map(schema)),
        reviewForm("hpc", "Presenting Complaint", HistoryPresentingComplaintMapper.map(schema)),
        reviewForm("mse", "Mental State", MentalStateExaminationMapper.map(schema)),
        reviewForm("mental-health-history", "Mental Health History", MentalHealthHistoryMapper.map(schema)),
        reviewForm("personal-history", "Personal History", PersonalHistoryMapper.map(schema)),
        reviewForm("family-history", "Family History", FamilyHistoryMapper.map(schema)),
        reviewForm("abuse", "Abuse Section", AbuseMapper.map(schema)),
        reviewForm("offending", "Offending History", OffendingHistoryMapper.map(schema)),
        reviewForm("social", "Social Circumstances", SocialCircumstancesMapper.map(schema)),
        reviewForm("medications", "Medication", MedicationsMapper.map(schema)),
        reviewForm("occupational", "Occupational Needs", OccupationalNeedsMapper.map(schema)),
        reviewForm("substance", "Substance Misuse", SubstanceMisuseMapper.map(schema)),
        reviewForm("audit", "AUDIT", AuditMapper.map(schema)),
        reviewForm("ldq", "LDQ", LdqMapper.map(schema)),
        reviewForm("child-protection", "Child Protection", ChildProtectionMapper.map(schema)),
        reviewForm("pisani", "PISANI Risk Assessment", PisaniMapper.map(schema)),
        reviewForm("safety-plan", "Safety Plan", SafetyPlanMapper.map(schema), requiresSignature = true),
        reviewForm("epic-note", "Epic Contact Note", EpicContactNoteMapper.map(schema))
    )

    private inline fun <reified T> reviewForm(
        id: String,
        name: String,
        output: T,
        requiresSignature: Boolean = false
    ): ReviewForm {
        val element = json.parseToJsonElement(json.encodeToString(output))
        return ReviewForm(
            id = id,
            name = name,
            fields = flattenJsonFields(element),
            requiresSignature = requiresSignature
        )
    }

    private fun flattenJsonFields(
        element: JsonElement,
        path: String = "",
        section: String = "Details"
    ): List<ReviewField> {
        return when (element) {
            is JsonObject -> element.entries.flatMap { (key, value) ->
                if (key.shouldSkipReviewField()) {
                    emptyList()
                } else {
                    val childPath = if (path.isBlank()) key else "$path.$key"
                    val childSection = if (path.isBlank()) "Details" else path.toReadableSection()
                    flattenJsonFields(value, childPath, childSection)
                }
            }

            is JsonArray -> flattenJsonArray(element, path, section)
            is JsonPrimitive -> listOf(
                ReviewField(
                    id = path.toStableId(),
                    section = section,
                    label = path.substringAfterLast('.').toReadableLabel(),
                    value = element.toDisplayValue(),
                    judgmentRequired = path.isJudgmentField()
                )
            )
        }
    }

    private fun flattenJsonArray(
        array: JsonArray,
        path: String,
        section: String
    ): List<ReviewField> {
        if (array.isEmpty()) {
            return listOf(
                ReviewField(
                    id = path.toStableId(),
                    section = section,
                    label = path.substringAfterLast('.').toReadableLabel(),
                    value = "",
                    judgmentRequired = path.isJudgmentField()
                )
            )
        }

        if (array.all { it is JsonPrimitive }) {
            val value = array.joinToString(", ") { item ->
                when (item) {
                    is JsonPrimitive -> item.toDisplayValue().orEmpty()
                    else -> ""
                }
            }
            return listOf(
                ReviewField(
                    id = path.toStableId(),
                    section = section,
                    label = path.substringAfterLast('.').toReadableLabel(),
                    value = value,
                    judgmentRequired = path.isJudgmentField()
                )
            )
        }

        val arrayLabel = path.substringAfterLast('.').toReadableLabel()
        return array.flatMapIndexed { index, item ->
            flattenJsonFields(
                element = item,
                path = "$path.${index + 1}",
                section = "$arrayLabel ${index + 1}"
            )
        }
    }

    private fun logAudit(eventType: AuditEventType, details: String) {
        if (sessionId.isBlank()) return

        try {
            auditBox.put(
                AuditEntryEntity(
                    sessionId = sessionId,
                    timestamp = System.currentTimeMillis(),
                    eventType = eventType.name,
                    details = details,
                    nurseId = nurseId
                )
            )
        } catch (t: Throwable) {
            Log.w(TAG, "Unable to write Belfast audit entry", t)
        }
    }

    private fun ValidationResult.toClarificationQuestion(): String {
        return when (ruleId) {
            1 -> "Document suicidality explicitly. Include the patient's TLNWL response and any suicidal ideation narrative."
            2 -> "Document TLNWL and TSH separately for this assessment."
            3 -> "Children are in regular contact. Resolve and document the UNOCINI assessment status."
            4 -> "Document access to weapons and gun licence status explicitly."
            5 -> "Document strengths and protective factors alongside the risk factors."
            6 -> "Document medication concordance or compliance issues for the prescribed medication."
            8 -> "Document whether any suicide or self-harm event occurred in the 48 hours before assessment."
            9 -> "Document the reason collateral information was not obtained, or confirm collateral was obtained."
            10 -> "Confirm the Safety Plan contains Lifeline, BelDOC, and SEBDOC crisis numbers."
            else -> message
        }
    }

    private fun ValidationResult.toReviewIssue(): ReviewIssue {
        return ReviewIssue(
            title = "Rule $ruleId ${if (severity == Severity.HARD_BLOCK) "hard block" else "soft flag"}",
            detail = message
        )
    }

    private fun JsonPrimitive.toDisplayValue(): String? {
        booleanOrNull?.let { return if (it) "Yes" else "No" }
        return contentOrNull
    }

    private fun String.shouldSkipReviewField(): Boolean {
        return endsWith("Confidence") ||
            this == "validationPassed" ||
            this == "validationMessages" ||
            this == "judgmentRequiredCount"
    }

    private fun String.isJudgmentField(): Boolean {
        val normalized = lowercase()
        return normalized.contains("impulsivityandselfcontrol") ||
            normalized.contains("engagementandreliability")
    }

    private fun String.toReadableSection(): String {
        val parent = substringAfterLast('.')
        return parent.toReadableLabel()
    }

    private fun String.toReadableLabel(): String {
        if (isBlank()) return "Details"

        return replace('.', ' ')
            .replace('-', ' ')
            .replace('_', ' ')
            .replace(Regex("(?<=[a-z])(?=[A-Z])"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { token ->
                when (token.lowercase()) {
                    "hc" -> "H&C"
                    "gp" -> "GP"
                    "mse" -> "MSE"
                    "tlnwl" -> "TLNWL"
                    "tsh" -> "TSH"
                    "pisani" -> "PISANI"
                    "unocini" -> "UNOCINI"
                    "ldq" -> "LDQ"
                    "audit" -> "AUDIT"
                    "epic" -> "Epic"
                    "ehr" -> "EHR"
                    "ooh" -> "OOH"
                    else -> token.replaceFirstChar { it.uppercase() }
                }
            }
    }

    private fun String.toStableId(): String {
        return lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "field" }
    }

    private fun formNameFor(fieldPath: String): String {
        return when {
            fieldPath.startsWith("mentalStateExamination") -> "Mental State"
            fieldPath.startsWith("historyOfPresentingComplaint") -> "Presenting Complaint"
            fieldPath.startsWith("pisaniRiskAssessment") -> "PISANI"
            fieldPath.startsWith("safetyPlan") -> "Safety Plan"
            fieldPath.startsWith("offendingHistory") -> "Offending History"
            fieldPath.startsWith("childProtection") -> "Child Protection"
            fieldPath.startsWith("collateral") -> "Collateral"
            fieldPath.startsWith("medications") -> "Medication"
            else -> fieldPath.substringBefore('.').toReadableLabel()
        }
    }

    private fun Throwable.safeJsonMessage(): String {
        return (message ?: javaClass.simpleName)
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }

    companion object {
        private const val TAG = "BelfastPipeline"
        private const val CLUSTER_MAX_TOKENS = 4096
        private const val MASTER_SCHEMA_MAX_TOKENS = 8192
        private const val CONFIDENCE_THRESHOLD = 0.80f
        private const val RULE_SAFETY_SIGNATURE = 7
    }
}
