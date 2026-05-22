package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class LdqOutput(
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
    val dependenceLevel: String,
    val catReferralIndicated: Boolean,
    val alternativeReferralRecommendation: String?
)

object LdqMapper {
    fun map(schema: MasterSchema): LdqOutput {
        val ldq = schema.substanceMisuse.ldqScore
        val patient = schema.patient
        
        val scores = listOfNotNull(
            ldq.q1, ldq.q2, ldq.q3, ldq.q4, ldq.q5,
            ldq.q6, ldq.q7, ldq.q8, ldq.q9, ldq.q10
        )
        
        val total = if (scores.size == 10) scores.sum() else null
        
        val depLevel = when {
            total == null -> "Incomplete assessment"
            total < 10 -> "Low dependence"
            total in 10..22 -> "Medium dependence"
            else -> "High dependence"
        }
        
        val catReferral = total != null && total > 20
        
        // Calculate age
        val birthYearStr = patient.dateOfBirth.split("-").firstOrNull()
        val birthYear = birthYearStr?.toIntOrNull() ?: 1900
        val isUnder25 = (2026 - birthYear) < 25
        
        val altReferral = when {
            total == null -> null
            total in 1..20 -> {
                if (isUnder25) {
                    "Consider referral to Daisy (youth service)"
                } else {
                    "Consider referral to Addictions NI or Dunlewy"
                }
            }
            else -> null
        }
        
        return LdqOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            q1Score = ldq.q1,
            q2Score = ldq.q2,
            q3Score = ldq.q3,
            q4Score = ldq.q4,
            q5Score = ldq.q5,
            q6Score = ldq.q6,
            q7Score = ldq.q7,
            q8Score = ldq.q8,
            q9Score = ldq.q9,
            q10Score = ldq.q10,
            totalScore = total,
            dependenceLevel = depLevel,
            catReferralIndicated = catReferral,
            alternativeReferralRecommendation = altReferral
        )
    }
}
