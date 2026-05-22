package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class MentalHealthHistoryOutput(
    val patientName: String,
    val hcNumber: String,
    val diagnosis: String?,
    val previousServiceContact: String?,
    val previousAdmissions: String?,
    val mhoUse: Boolean,
    val mhoDetails: String?,
    val previousSelfHarm: String?,
    val recentSuicideEventsLast2Months: String?,
    val recentSuicideEventsBefore2Months: String?
)

object MentalHealthHistoryMapper {
    fun map(schema: MasterSchema): MentalHealthHistoryOutput {
        val history = schema.history.mentalHealthHistory
        val patient = schema.patient
        return MentalHealthHistoryOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            diagnosis = history.diagnosis,
            previousServiceContact = history.previousServiceContact,
            previousAdmissions = history.previousAdmissions,
            mhoUse = history.mhoUse,
            mhoDetails = history.mhoDetails,
            previousSelfHarm = history.previousSelfHarm,
            recentSuicideEventsLast2Months = history.recentSuicideEventsLast2Months,
            recentSuicideEventsBefore2Months = history.recentSuicideEventsBefore2Months
        )
    }
}
