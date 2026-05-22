package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class ConfidentialityOutput(
    val patientName: String,
    val hcNumber: String,
    val confidentialityExplained: Boolean,
    val capacityToConsent: String, // "Yes", "No", "Not assessed"
    val consentToSeekInformation: Boolean?,
    val consentToShareInformation: Boolean?,
    val informationShareableWith: List<String>,
    val familyCarerConsent: Boolean?,
    val consentToPhoneContact: Boolean?,
    val thirdPartyInformationRestrictions: String?
)

object ConfidentialityMapper {
    fun map(schema: MasterSchema): ConfidentialityOutput {
        val conf = schema.confidentiality
        val patient = schema.patient
        val capacityStr = when (conf.capacityToConsent) {
            true -> "Yes"
            false -> "No"
            null -> "Not assessed"
        }
        return ConfidentialityOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            confidentialityExplained = conf.confidentialityExplained,
            capacityToConsent = capacityStr,
            consentToSeekInformation = conf.consentToSeekInformation,
            consentToShareInformation = conf.consentToShareInformation,
            informationShareableWith = conf.informationShareableWith,
            familyCarerConsent = conf.familyCarerConsent,
            consentToPhoneContact = conf.consentToPhoneContact,
            thirdPartyInformationRestrictions = conf.thirdPartyInformationRestrictions
        )
    }
}
