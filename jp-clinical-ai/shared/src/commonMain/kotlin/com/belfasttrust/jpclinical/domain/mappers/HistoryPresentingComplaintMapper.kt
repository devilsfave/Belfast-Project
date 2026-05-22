package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class HistoryPresentingComplaintOutput(
    val patientName: String,
    val hcNumber: String,
    val reasonForPresentation: String,
    val precipitatingFactors: String,
    val courseAndDuration: String,
    val featuresOfMentalIllness: String,
    val managementToDate: String,
    val presentingSuicideEventsOccurred: Boolean?,
    val presentingSuicideEventsDescription: String?,
    val presentingSuicideEventsMethod: String?,
    val presentingSuicideEventsIntent: String?,
    val validationPassed: Boolean,
    val validationMessages: List<String>
)

object HistoryPresentingComplaintMapper {
    fun map(schema: MasterSchema): HistoryPresentingComplaintOutput {
        val hpc = schema.historyOfPresentingComplaint
        val patient = schema.patient
        
        val messages = mutableListOf<String>()
        if (hpc.presentingSuicideEvents48hrs.occurred == null) {
            messages.add("Rule 8: Presenting suicide events in the last 48 hours must be explicitly documented.")
        }
        
        return HistoryPresentingComplaintOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            reasonForPresentation = hpc.reasonForPresentation,
            precipitatingFactors = hpc.precipitatingFactors,
            courseAndDuration = hpc.courseAndDuration,
            featuresOfMentalIllness = hpc.featuresOfMentalIllness,
            managementToDate = hpc.managementToDate,
            presentingSuicideEventsOccurred = hpc.presentingSuicideEvents48hrs.occurred,
            presentingSuicideEventsDescription = hpc.presentingSuicideEvents48hrs.description,
            presentingSuicideEventsMethod = hpc.presentingSuicideEvents48hrs.method,
            presentingSuicideEventsIntent = hpc.presentingSuicideEvents48hrs.intentLevel,
            validationPassed = messages.isEmpty(),
            validationMessages = messages
        )
    }
}
