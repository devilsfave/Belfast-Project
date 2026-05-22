package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class AbuseOutput(
    val patientName: String,
    val hcNumber: String,
    val abuseIssuesIdentified: Boolean,
    val vulnerabilityIdentified: Boolean,
    val abuseDetails: String?
)

object AbuseMapper {
    fun map(schema: MasterSchema): AbuseOutput {
        val abuse = schema.abuse
        val patient = schema.patient
        return AbuseOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            abuseIssuesIdentified = abuse.abuseIssuesIdentified,
            vulnerabilityIdentified = abuse.vulnerabilityIdentified,
            abuseDetails = abuse.abuseDetails
        )
    }
}
