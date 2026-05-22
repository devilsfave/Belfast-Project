package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class PersonalHistoryOutput(
    val patientName: String,
    val hcNumber: String,
    val earlyChildhood: String?,
    val developmentalMilestones: String?,
    val schooling: String?,
    val psychosexualHistory: String?,
    val traumaHistory: String?,
    val relationshipHistory: String?,
    val employmentHistory: String?,
    val spiritualCulturalNeeds: String?
)

object PersonalHistoryMapper {
    fun map(schema: MasterSchema): PersonalHistoryOutput {
        val history = schema.history.personalHistory
        val patient = schema.patient
        return PersonalHistoryOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            earlyChildhood = history.earlyChildhood,
            developmentalMilestones = history.developmentalMilestones,
            schooling = history.schooling,
            psychosexualHistory = history.psychosexualHistory,
            traumaHistory = history.traumaHistory,
            relationshipHistory = history.relationshipHistory,
            employmentHistory = history.employmentHistory,
            spiritualCulturalNeeds = history.spiritualCulturalNeeds
        )
    }
}
