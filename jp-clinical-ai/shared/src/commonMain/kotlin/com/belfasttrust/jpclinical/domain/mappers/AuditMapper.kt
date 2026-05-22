package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class AuditOutput(
    val patientName: String,
    val hcNumber: String,
    val q1Score: Int?,
    val q2Score: Int?,
    val q3Score: Int?,
    val q4Score: Int?,
    val q5Score: Int?,
    val q6Score: Int?,
    val q7Score: Int?,
    val q8Score: Int?,
    val q9Score: Int?,
    val q10Score: Int?,
    val totalScore: Int?,
    val riskLevel: String,
    val followUpRequired: Boolean
)

object AuditMapper {
    fun map(schema: MasterSchema): AuditOutput {
        val audit = schema.substanceMisuse.auditScore
        val patient = schema.patient
        
        val scores = listOfNotNull(
            audit.q1, audit.q2, audit.q3, audit.q4, audit.q5,
            audit.q6, audit.q7, audit.q8, audit.q9, audit.q10
        )
        
        val total = if (scores.size == 10) scores.sum() else null
        
        val risk = when {
            total == null -> "Incomplete assessment"
            total < 8 -> "Lower risk"
            total in 8..15 -> "Increasing risk"
            total in 16..19 -> "Higher risk"
            else -> "Possible dependence"
        }
        
        val followUp = total != null && total >= 8
        
        return AuditOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            q1Score = audit.q1,
            q2Score = audit.q2,
            q3Score = audit.q3,
            q4Score = audit.q4,
            q5Score = audit.q5,
            q6Score = audit.q6,
            q7Score = audit.q7,
            q8Score = audit.q8,
            q9Score = audit.q9,
            q10Score = audit.q10,
            totalScore = total,
            riskLevel = risk,
            followUpRequired = followUp
        )
    }
}
