package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class CollateralOutput(
    val patientName: String,
    val hcNumber: String,
    val collateralObtained: Boolean,
    val collateralDeclinedReason: String?,
    val collateralSources: List<CollateralSourceEntry>,
    val validationPassed: Boolean,
    val validationMessages: List<String>
)

@Serializable
data class CollateralSourceEntry(
    val name: String,
    val relationship: String,
    val informationProvided: String
)

object CollateralMapper {
    fun map(schema: MasterSchema): CollateralOutput {
        val collateral = schema.collateral
        val patient = schema.patient
        
        val messages = mutableListOf<String>()
        if (!collateral.collateralObtained && collateral.collateralDeclinedReason.isNullOrBlank()) {
            messages.add("Rule 9: Collateral not obtained but no reason documented.")
        }
        
        return CollateralOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            collateralObtained = collateral.collateralObtained,
            collateralDeclinedReason = collateral.collateralDeclinedReason,
            collateralSources = collateral.collateralSources.map {
                CollateralSourceEntry(it.name, it.relationship, it.informationProvided)
            },
            validationPassed = messages.isEmpty(),
            validationMessages = messages
        )
    }
}
