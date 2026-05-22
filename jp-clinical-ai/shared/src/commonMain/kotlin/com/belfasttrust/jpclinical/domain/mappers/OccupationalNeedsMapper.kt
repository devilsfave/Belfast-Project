package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class OccupationalNeedsOutput(
    val patientName: String,
    val hcNumber: String,
    val occupationalNeeds: String?
)

object OccupationalNeedsMapper {
    fun map(schema: MasterSchema): OccupationalNeedsOutput {
        val sc = schema.socialCircumstances
        val patient = schema.patient
        return OccupationalNeedsOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            occupationalNeeds = sc.occupationalNeeds
        )
    }
}
