package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class MentalStateExaminationOutput(
    val patientName: String,
    val hcNumber: String,
    val appearanceBehaviour: AppearanceBehaviourOutput,
    val speechThoughtForm: SpeechThoughtFormOutput,
    val moodAffect: MoodAffectOutput,
    val suicidality: SuicidalityOutput,
    val thoughtContent: ThoughtContentOutput,
    val perceptualDisturbances: PerceptualDisturbancesOutput,
    val cognition: CognitionOutput,
    val insight: InsightOutput,
    val validationPassed: Boolean,
    val validationMessages: List<String>
)

@Serializable
data class AppearanceBehaviourOutput(
    val clothingAppropriateness: String,
    val selfCare: String,
    val motorActivity: String,
    val rapport: String,
    val eyeContact: String,
    val overallNarrative: String
)

@Serializable
data class SpeechThoughtFormOutput(
    val spontaneity: String,
    val coherence: String,
    val rate: String,
    val tone: String,
    val volume: String,
    val thoughtDisorderPresent: Boolean,
    val thoughtDisorderDescription: String?,
    val overallNarrative: String
)

@Serializable
data class MoodAffectOutput(
    val subjectiveMood: String,
    val objectiveAffect: String,
    val affectFluctuation: String,
    val moodClassification: String,
    val anxietyPresent: Boolean,
    val anxietyDescription: String?,
    val reactivity: String,
    val overallNarrative: String
)

@Serializable
data class SuicidalityOutput(
    val tlnwl: String?,
    val suicidalIdeationPresent: Boolean,
    val suicidalIdeationDescription: String?,
    val planPresent: Boolean,
    val planDescription: String?,
    val intentPresent: Boolean,
    val intentDescription: String?,
    val tsh: String?,
    val selfHarmPlan: String?,
    val suicideAttemptThisPresentation: Boolean,
    val adviceGivenOnReattemptRisk: String,
    val overallNarrative: String
)

@Serializable
data class ThoughtContentOutput(
    val preoccupations: String?,
    val obsessions: String?,
    val delusionsPresent: Boolean,
    val delusionsDescription: String?,
    val paranoidThoughts: String?,
    val thoughtInterference: Boolean,
    val passivityPhenomena: Boolean,
    val violentThoughts: Boolean,
    val violentThoughtsDescription: String?,
    val overallNarrative: String
)

@Serializable
data class PerceptualDisturbancesOutput(
    val auditoryHallucinations: Boolean,
    val auditoryDescription: String?,
    val visualHallucinations: Boolean,
    val visualDescription: String?,
    val gustatoryHallucinations: Boolean,
    val olfactoryHallucinations: Boolean,
    val tactileHallucinations: Boolean,
    val overallNarrative: String
)

@Serializable
data class CognitionOutput(
    val orientationTime: Boolean,
    val orientationPlace: Boolean,
    val orientationPerson: Boolean,
    val attentionIntact: Boolean,
    val memoryIntact: Boolean,
    val mmseIndicated: Boolean,
    val mmseScore: Int?,
    val overallNarrative: String
)

@Serializable
data class InsightOutput(
    val awarenessOfIllness: String,
    val willingnessToEngage: String,
    val insightLevel: String,
    val overallNarrative: String
)

object MentalStateExaminationMapper {
    fun map(schema: MasterSchema): MentalStateExaminationOutput {
        val mse = schema.mentalStateExamination
        val patient = schema.patient
        
        val messages = mutableListOf<String>()
        val suicidality = mse.suicidality
        
        // Rule 1: TLNWL must not be null/blank and SI narrative must be present if SI is true
        if (suicidality.tlnwl.isNullOrBlank()) {
            messages.add("Rule 1: Thoughts of Life Not Worth Living (TLNWL) must be explicitly documented.")
        }
        if (suicidality.suicidalIdeationPresent && suicidality.suicidalIdeationDescription.isNullOrBlank()) {
            messages.add("Rule 1: Suicidal Ideation is present but has no detailed narrative.")
        }
        
        // Rule 2: Both TLNWL and TSH must be addressed separately
        if (suicidality.tlnwl.isNullOrBlank()) {
            messages.add("Rule 2: Thoughts of Life Not Worth Living (TLNWL) is missing from note.")
        }
        if (suicidality.tsh.isNullOrBlank()) {
            messages.add("Rule 2: Thoughts of Self Harm (TSH) is missing from note.")
        }
        
        return MentalStateExaminationOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            appearanceBehaviour = AppearanceBehaviourOutput(
                clothingAppropriateness = mse.appearanceBehaviour.clothingAppropriateness,
                selfCare = mse.appearanceBehaviour.selfCare,
                motorActivity = mse.appearanceBehaviour.motorActivity,
                rapport = mse.appearanceBehaviour.rapport,
                eyeContact = mse.appearanceBehaviour.eyeContact,
                overallNarrative = mse.appearanceBehaviour.overallNarrative
            ),
            speechThoughtForm = SpeechThoughtFormOutput(
                spontaneity = mse.speechThoughtForm.spontaneity,
                coherence = mse.speechThoughtForm.coherence,
                rate = mse.speechThoughtForm.rate,
                tone = mse.speechThoughtForm.tone,
                volume = mse.speechThoughtForm.volume,
                thoughtDisorderPresent = mse.speechThoughtForm.thoughtDisorderPresent,
                thoughtDisorderDescription = mse.speechThoughtForm.thoughtDisorderDescription,
                overallNarrative = mse.speechThoughtForm.overallNarrative
            ),
            moodAffect = MoodAffectOutput(
                subjectiveMood = mse.moodAffect.subjectiveMood,
                objectiveAffect = mse.moodAffect.objectiveAffect,
                affectFluctuation = mse.moodAffect.affectFluctuation,
                moodClassification = mse.moodAffect.moodClassification.name,
                anxietyPresent = mse.moodAffect.anxietyPresent,
                anxietyDescription = mse.moodAffect.anxietyDescription,
                reactivity = mse.moodAffect.reactivity,
                overallNarrative = mse.moodAffect.overallNarrative
            ),
            suicidality = SuicidalityOutput(
                tlnwl = suicidality.tlnwl,
                suicidalIdeationPresent = suicidality.suicidalIdeationPresent,
                suicidalIdeationDescription = suicidality.suicidalIdeationDescription,
                planPresent = suicidality.planPresent,
                planDescription = suicidality.planDescription,
                intentPresent = suicidality.intentPresent,
                intentDescription = suicidality.intentDescription,
                tsh = suicidality.tsh,
                selfHarmPlan = suicidality.selfHarmPlan,
                suicideAttemptThisPresentation = suicidality.suicideAttemptThisPresentation,
                adviceGivenOnReattemptRisk = suicidality.adviceGivenOnReattemptRisk,
                overallNarrative = suicidality.overallNarrative
            ),
            thoughtContent = ThoughtContentOutput(
                preoccupations = mse.thoughtContent.preoccupations,
                obsessions = mse.thoughtContent.obsessions,
                delusionsPresent = mse.thoughtContent.delusionsPresent,
                delusionsDescription = mse.thoughtContent.delusionsDescription,
                paranoidThoughts = mse.thoughtContent.paranoidThoughts,
                thoughtInterference = mse.thoughtContent.thoughtInterference,
                passivityPhenomena = mse.thoughtContent.passivityPhenomena,
                violentThoughts = mse.thoughtContent.violentThoughts,
                violentThoughtsDescription = mse.thoughtContent.violentThoughtsDescription,
                overallNarrative = mse.thoughtContent.overallNarrative
            ),
            perceptualDisturbances = PerceptualDisturbancesOutput(
                auditoryHallucinations = mse.perceptualDisturbances.auditoryHallucinations,
                auditoryDescription = mse.perceptualDisturbances.auditoryDescription,
                visualHallucinations = mse.perceptualDisturbances.visualHallucinations,
                visualDescription = mse.perceptualDisturbances.visualDescription,
                gustatoryHallucinations = mse.perceptualDisturbances.gustatoryHallucinations,
                olfactoryHallucinations = mse.perceptualDisturbances.olfactoryHallucinations,
                tactileHallucinations = mse.perceptualDisturbances.tactileHallucinations,
                overallNarrative = mse.perceptualDisturbances.overallNarrative
            ),
            cognition = CognitionOutput(
                orientationTime = mse.cognition.orientationTime,
                orientationPlace = mse.cognition.orientationPlace,
                orientationPerson = mse.cognition.orientationPerson,
                attentionIntact = mse.cognition.attentionIntact,
                memoryIntact = mse.cognition.memoryIntact,
                mmseIndicated = mse.cognition.mmseIndicated,
                mmseScore = mse.cognition.mmseScore,
                overallNarrative = mse.cognition.overallNarrative
            ),
            insight = InsightOutput(
                awarenessOfIllness = mse.insight.awarenessOfIllness,
                willingnessToEngage = mse.insight.willingnessToEngage,
                insightLevel = mse.insight.insightLevel.name,
                overallNarrative = mse.insight.overallNarrative
            ),
            validationPassed = messages.isEmpty(),
            validationMessages = messages.distinct()
        )
    }
}
