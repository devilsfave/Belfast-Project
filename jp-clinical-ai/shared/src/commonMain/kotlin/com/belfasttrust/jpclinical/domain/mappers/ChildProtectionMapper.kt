package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class ChildProtectionOutput(
    val patientName: String,
    val hcNumber: String,
    val maritalStatus: String?,
    val partnerDetails: String?,
    val childrenInRegularContact: Boolean,
    val children: List<ChildDetailEntry>,
    val fccSocialServicesInvolved: Boolean,
    val unociniReferralRequired: Boolean,
    val unociniTriggerReason: String?,
    val validationPassed: Boolean,
    val validationMessages: List<String>
)

@Serializable
data class ChildDetailEntry(
    val name: String,
    val sex: String,
    val dateOfBirth: String,
    val relationshipToPatient: String,
    val relationshipToPartner: String?,
    val otherParentDetails: String?
)

object ChildProtectionMapper {
    fun map(schema: MasterSchema): ChildProtectionOutput {
        val cp = schema.childProtection
        val patient = schema.patient
        val pisani = schema.pisaniRiskAssessment
        val offending = schema.offendingHistory
        val familyHistory = schema.history.familyHistory
        val substance = schema.substanceMisuse
        
        val childrenInContact = cp.childrenInRegularContact
        
        // UNOCINI Trigger logic
        val isHighOrVeryHighRisk = pisani.overallRiskLevel == RiskLevel.HIGH || pisani.overallRiskLevel == RiskLevel.VERY_HIGH
        val hasWeaponsAccess = offending.accessToWeapons == true
        val hasLethalMeansAccess = familyHistory.accessToLethalMeans
        val hasInjectingHistory = substance.complexFactors.injectingHistory
        
        val triggers = mutableListOf<String>()
        if (isHighOrVeryHighRisk) triggers.add("overall risk level is ${pisani.overallRiskLevel}")
        if (hasWeaponsAccess) triggers.add("weapons access is documented")
        if (hasLethalMeansAccess) triggers.add("access to lethal means in family history")
        if (hasInjectingHistory) triggers.add("substance misuse injecting history")
        
        val calculatedUnociniRequired = childrenInContact && triggers.isNotEmpty()
        val calculatedTriggerReason = if (calculatedUnociniRequired) {
            "Mandatory UNOCINI trigger: Children in regular contact + ${triggers.joinToString(" AND ")}"
        } else {
            null
        }
        
        val messages = mutableListOf<String>()
        // Rule 3: If children in regular contact, UNOCINI trigger must be resolved
        if (childrenInContact && calculatedUnociniRequired && !cp.unociniReferralRequired) {
            messages.add("Rule 3: Children in regular contact and safety triggers exist. UNOCINI referral must be initiated.")
        }
        
        return ChildProtectionOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            maritalStatus = patient.maritalStatus,
            partnerDetails = cp.partnerDetails,
            childrenInRegularContact = childrenInContact,
            children = cp.children.map {
                ChildDetailEntry(
                    name = it.name,
                    sex = it.sex,
                    dateOfBirth = it.dateOfBirth,
                    relationshipToPatient = it.relationshipToPatient,
                    relationshipToPartner = it.relationshipToPartner,
                    otherParentDetails = it.otherParentDetails
                )
            },
            fccSocialServicesInvolved = cp.fccSocialServicesInvolved,
            unociniReferralRequired = calculatedUnociniRequired || cp.unociniReferralRequired,
            unociniTriggerReason = calculatedTriggerReason ?: cp.unociniTriggerReason,
            validationPassed = messages.isEmpty(),
            validationMessages = messages
        )
    }
}
