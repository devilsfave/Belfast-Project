package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class SubstanceMisuseOutput(
    val patientName: String,
    val hcNumber: String,
    val alcoholPresent: Boolean,
    val alcoholFrequency: String?,
    val alcoholAmount: String?,
    val alcoholDuration: String?,
    val alcoholWithdrawal: Boolean,
    val alcoholCravings: Boolean,
    val drugPresent: Boolean,
    val drugSubstances: List<String>,
    val drugFrequency: String?,
    val drugDuration: String?,
    val drugWithdrawal: Boolean,
    val drugCravings: Boolean,
    val polysubstance: Boolean,
    val impactOnLife: String?,
    val previousSubstanceUse: String?,
    val abstinenceHistory: String?,
    val pregnancy: Boolean,
    val injectingHistory: Boolean,
    val hiv: Boolean?,
    val hepB: Boolean?,
    val hepC: Boolean?
)

object SubstanceMisuseMapper {
    fun map(schema: MasterSchema): SubstanceMisuseOutput {
        val sm = schema.substanceMisuse
        val patient = schema.patient
        
        return SubstanceMisuseOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            alcoholPresent = sm.currentAlcoholUse.present,
            alcoholFrequency = sm.currentAlcoholUse.frequency,
            alcoholAmount = sm.currentAlcoholUse.amount,
            alcoholDuration = sm.currentAlcoholUse.duration,
            alcoholWithdrawal = sm.currentAlcoholUse.withdrawalSymptoms,
            alcoholCravings = sm.currentAlcoholUse.cravings,
            drugPresent = sm.currentDrugUse.present,
            drugSubstances = sm.currentDrugUse.substances,
            drugFrequency = sm.currentDrugUse.frequency,
            drugDuration = sm.currentDrugUse.duration,
            drugWithdrawal = sm.currentDrugUse.withdrawalSymptoms,
            drugCravings = sm.currentDrugUse.cravings,
            polysubstance = sm.currentDrugUse.polysubstance,
            impactOnLife = sm.impactOnLife,
            previousSubstanceUse = sm.previousSubstanceUse,
            abstinenceHistory = sm.abstinenceHistory,
            pregnancy = sm.complexFactors.pregnancy,
            injectingHistory = sm.complexFactors.injectingHistory,
            hiv = sm.complexFactors.hiv,
            hepB = sm.complexFactors.hepB,
            hepC = sm.complexFactors.hepC
        )
    }
}
