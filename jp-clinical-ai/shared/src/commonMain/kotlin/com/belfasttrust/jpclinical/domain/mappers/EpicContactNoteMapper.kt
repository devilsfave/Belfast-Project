package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class EpicContactNoteOutput(
    val patientName: String,
    val hcNumber: String,
    val typeOfContact: String,
    val purposeOfContact: String,
    val contactDetailsOverview: String,
    val mseAppearanceBehaviour: String,
    val mseSpeechThoughtForm: String,
    val mseMoodAffect: String,
    val mseSuicidality: String,
    val mseThoughtContent: String,
    val msePerceptualDisturbances: String,
    val mseCognition: String,
    val mseInsight: String,
    val collateralSummary: String,
    val medicationsSummary: String,
    val interventionsUsed: List<String>,
    val riskSummary: String,
    val plan: String
)

object EpicContactNoteMapper {
    fun map(schema: MasterSchema): EpicContactNoteOutput {
        val epic = schema.epicContactNote
        val patient = schema.patient
        val mse = schema.mentalStateExamination
        val collateral = schema.collateral
        val meds = schema.medications
        
        // Build collateral summary
        val collateralSum = if (collateral.collateralObtained) {
            "Collateral obtained from: " + collateral.collateralSources.joinToString("; ") {
                "${it.name} (${it.relationship}): ${it.informationProvided}"
            }
        } else {
            "Collateral not obtained. Reason: ${collateral.collateralDeclinedReason ?: "Not specified"}"
        }
        
        // Build medications summary
        val medsSum = if (meds.currentMedications.isEmpty()) {
            "No medications prescribed."
        } else {
            "Current medications: " + meds.currentMedications.joinToString("; ") {
                "${it.name} ${it.dosage} ${it.frequency} (${it.complianceIssues ?: "Compliance details missing"})"
            }
        }
        
        return EpicContactNoteOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            typeOfContact = epic.typeOfContact,
            purposeOfContact = epic.purposeOfContact,
            contactDetailsOverview = epic.contactDetailsOverview,
            mseAppearanceBehaviour = mse.appearanceBehaviour.overallNarrative,
            mseSpeechThoughtForm = mse.speechThoughtForm.overallNarrative,
            mseMoodAffect = mse.moodAffect.overallNarrative,
            mseSuicidality = mse.suicidality.overallNarrative,
            mseThoughtContent = mse.thoughtContent.overallNarrative,
            msePerceptualDisturbances = mse.perceptualDisturbances.overallNarrative,
            mseCognition = mse.cognition.overallNarrative,
            mseInsight = mse.insight.overallNarrative,
            collateralSummary = collateralSum,
            medicationsSummary = medsSum,
            interventionsUsed = epic.interventionsUsed.map { it.name },
            riskSummary = epic.riskSummary,
            plan = epic.plan
        )
    }
}
