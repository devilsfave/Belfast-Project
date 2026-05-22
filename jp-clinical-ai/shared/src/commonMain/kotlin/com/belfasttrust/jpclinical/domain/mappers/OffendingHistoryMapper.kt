package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class OffendingHistoryOutput(
    val patientName: String,
    val hcNumber: String,
    val forensicHistory: String?,
    val currentCharges: String?,
    val pendingCharges: String?,
    val custodialHistory: String?,
    val accessToWeapons: Boolean?,
    val gunLicence: Boolean?,
    val gunLicenceDetails: String?,
    val validationPassed: Boolean,
    val validationMessages: List<String>
)

object OffendingHistoryMapper {
    fun map(schema: MasterSchema): OffendingHistoryOutput {
        val offending = schema.offendingHistory
        val patient = schema.patient
        
        val messages = mutableListOf<String>()
        if (offending.accessToWeapons == null) {
            messages.add("Rule 4: Weapons access must be explicitly documented.")
        }
        if (offending.gunLicence == null) {
            messages.add("Rule 4: Gun licence possession must be explicitly documented.")
        }
        
        return OffendingHistoryOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            forensicHistory = offending.forensicHistory,
            currentCharges = offending.currentCharges,
            pendingCharges = offending.pendingCharges,
            custodialHistory = offending.custodialHistory,
            accessToWeapons = offending.accessToWeapons,
            gunLicence = offending.gunLicence,
            gunLicenceDetails = offending.gunLicenceDetails,
            validationPassed = messages.isEmpty(),
            validationMessages = messages
        )
    }
}
