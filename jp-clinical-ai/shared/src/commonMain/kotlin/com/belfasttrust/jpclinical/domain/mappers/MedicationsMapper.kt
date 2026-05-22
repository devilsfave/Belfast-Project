package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class MedicationsOutput(
    val patientName: String,
    val hcNumber: String,
    val medicationsList: List<MedicationEntry>,
    val autonomy: MedicationAutonomyOutput,
    val validationPassed: Boolean,
    val validationMessages: List<String>
)

@Serializable
data class MedicationEntry(
    val name: String,
    val dosage: String,
    val frequency: String,
    val sideEffects: String?,
    val allergies: String?,
    val complianceIssues: String?
)

@Serializable
data class MedicationAutonomyOutput(
    val removingFromPackaging: String,
    val readingLabels: String,
    val takingRightDoseRightTime: String,
    val swallowingTablets: String,
    val usingEquipmentAids: String,
    val storingSafely: String,
    val disposingSafely: String,
    val ordering: String,
    val collecting: String
)

object MedicationsMapper {
    fun map(schema: MasterSchema): MedicationsOutput {
        val meds = schema.medications
        val patient = schema.patient
        
        val messages = mutableListOf<String>()
        val medsList = meds.currentMedications.map {
            MedicationEntry(
                name = it.name,
                dosage = it.dosage,
                frequency = it.frequency,
                sideEffects = it.sideEffects,
                allergies = it.allergies,
                complianceIssues = it.complianceIssues
            )
        }
        
        // Rule 6: Soft flag if medications are prescribed but all compliance issues details are missing/null
        if (medsList.isNotEmpty() && medsList.all { it.complianceIssues.isNullOrBlank() }) {
            messages.add("Rule 6: Prescribed medications lack documented compliance details in note.")
        }
        
        val aut = meds.medicationAutonomy
        val autOutput = MedicationAutonomyOutput(
            removingFromPackaging = aut.removingFromPackaging.name,
            readingLabels = aut.readingLabels.name,
            takingRightDoseRightTime = aut.takingRightDoseRightTime.name,
            swallowingTablets = aut.swallowingTablets.name,
            usingEquipmentAids = aut.usingEquipmentAids.name,
            storingSafely = aut.storingSafely.name,
            disposingSafely = aut.disposingSafely.name,
            ordering = aut.ordering.name,
            collecting = aut.collecting.name
        )
        
        return MedicationsOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            medicationsList = medsList,
            autonomy = autOutput,
            validationPassed = messages.isEmpty(),
            validationMessages = messages
        )
    }
}
