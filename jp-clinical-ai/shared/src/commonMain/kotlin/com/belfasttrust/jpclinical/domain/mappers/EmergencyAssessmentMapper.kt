package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class EmergencyAssessmentOutput(
    val patientName: String,
    val hcNumber: String,
    val dateOfBirth: String,
    val placeOfAssessment: String,
    val assessorName: String,
    val assessorDesignation: String,
    val referralAgent: String,
    val gpName: String,
    val gpAddress: String,
    val assessmentDate: String,
    val assessmentTime: String,
    val nextOfKinName: String?,
    val nextOfKinRelationship: String?,
    val nextOfKinPhone: String?
)

object EmergencyAssessmentMapper {
    fun map(schema: MasterSchema): EmergencyAssessmentOutput {
        val patient = schema.patient
        val referral = schema.referral
        return EmergencyAssessmentOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            dateOfBirth = patient.dateOfBirth,
            placeOfAssessment = patient.placeOfAssessment,
            assessorName = referral.assessorName,
            assessorDesignation = referral.assessorDesignation,
            referralAgent = referral.referralAgent,
            gpName = patient.gpName,
            gpAddress = patient.gpAddress,
            assessmentDate = schema.assessmentDate,
            assessmentTime = schema.assessmentTime,
            nextOfKinName = patient.nextOfKinName,
            nextOfKinRelationship = patient.nextOfKinRelationship,
            nextOfKinPhone = patient.nextOfKinPhone
        )
    }
}
