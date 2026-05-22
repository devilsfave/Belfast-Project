package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class FamilyHistoryOutput(
    val patientName: String,
    val hcNumber: String,
    val livingArrangements: String,
    val familyDynamics: String?,
    val accommodationType: String,
    val abilityToManageIndependently: String,
    val carersInvolved: Boolean,
    val carerDetails: String?,
    val accessToLethalMeans: Boolean,
    val lethalMeansDescription: String?,
    val fccResponseToTreatment: String?,
    val parentsHistory: String?,
    val siblingsHistory: String?,
    val familyHistorySuicide: Boolean,
    val familyHistoryAddictions: Boolean,
    val familyHistoryMentalIllness: Boolean
)

object FamilyHistoryMapper {
    fun map(schema: MasterSchema): FamilyHistoryOutput {
        val history = schema.history.familyHistory
        val patient = schema.patient
        return FamilyHistoryOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            livingArrangements = history.livingArrangements,
            familyDynamics = history.familyDynamics,
            accommodationType = history.accommodationType,
            abilityToManageIndependently = history.abilityToManageIndependently,
            carersInvolved = history.carersInvolved,
            carerDetails = history.carerDetails,
            accessToLethalMeans = history.accessToLethalMeans,
            lethalMeansDescription = history.lethalMeansDescription,
            fccResponseToTreatment = history.fccResponseToTreatment,
            parentsHistory = history.parentsHistory,
            siblingsHistory = history.siblingsHistory,
            familyHistorySuicide = history.familyHistorySuicide,
            familyHistoryAddictions = history.familyHistoryAddictions,
            familyHistoryMentalIllness = history.familyHistoryMentalIllness
        )
    }
}
