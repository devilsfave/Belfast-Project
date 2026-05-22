package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

@Serializable
data class SocialCircumstancesOutput(
    val patientName: String,
    val hcNumber: String,
    val housing: String,
    val finances: String?,
    val debts: String?,
    val relationships: String?,
    val friendships: String?,
    val supportNetwork: String?,
    val socialising: String?,
    val hobbies: String?,
    val strengths: String?
)

object SocialCircumstancesMapper {
    fun map(schema: MasterSchema): SocialCircumstancesOutput {
        val sc = schema.socialCircumstances
        val patient = schema.patient
        return SocialCircumstancesOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            housing = sc.housing,
            finances = sc.finances,
            debts = sc.debts,
            relationships = sc.relationships,
            friendships = sc.friendships,
            supportNetwork = sc.supportNetwork,
            socialising = sc.socialising,
            hobbies = sc.hobbies,
            strengths = sc.strengths
        )
    }
}
