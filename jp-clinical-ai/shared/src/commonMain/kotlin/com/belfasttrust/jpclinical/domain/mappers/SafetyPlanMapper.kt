package com.belfasttrust.jpclinical.domain.mappers

import com.belfasttrust.jpclinical.schema.*
import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// SAFETY PLAN MAPPER — Form 19
//
// Maps MasterSchema → SafetyPlanOutput (the printable/reviewable form).
// Lives in shared/commonMain — KMP-safe, zero Android imports.
//
// KEY RULES:
//  • Crisis phone numbers are PRE-PRINTED — never extracted from notes.
//  • Policy Rule 7 (HARD BLOCK): nurse signature required before finalising.
//  • Policy Rule 10 (HARD BLOCK): all 3 crisis numbers must be present.
//  • Step 5 GP details come from Patient, not from notes.
//  • This mapper is a PURE FUNCTION — no side effects, no AI, no guessing.
//
// Source: JP's actual form (Image 11 — Safety Plan paper form)
//  • 6 shaded step headers
//  • Step 5 pre-prints: GP OOH BelDOC 02890744447, SEBDOC 02890796220,
//    Lifeline (24/7) 0808 808 8000
//  • Step 6 includes "most important thing worth living for" and follow-up call
//  • Lifeline accessibility note for deaf/HOH users
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Output model for the Belfast Trust Safety Plan form.
 * This mirrors the exact layout of JP's paper form (Image 11).
 */
@Serializable
data class SafetyPlanOutput(
    val patientName: String,
    val hcNumber: String,
    val assessmentDate: String,
    val assessorName: String,

    // Step 1: Warning signs (3 numbered lines on the paper form)
    val step1WarningSignals: List<String>,

    // Step 2: Internal coping strategies (3 numbered lines)
    val step2InternalCopingStrategies: List<String>,

    // Step 3: People and social settings for distraction (name + place pairs)
    val step3DistractionPeoplePlaces: List<DistractionEntry>,

    // Step 4: People to ask for help / supporters (name + phone, up to 3)
    val step4Supporters: List<SupporterEntry>,

    // Step 5: Professionals and agencies (GP + OOH + Lifeline — pre-printed)
    val step5ProfessionalName: String?,
    val step5ProfessionalPhone: String?,
    val step5GpName: String,
    val step5GpPhone: String,
    val step5GpOohBeldoc: String,   // Always "02890744447"
    val step5GpOohSebdoc: String,   // Always "02890796220"
    val step5Lifeline247: String,   // Always "0808 808 8000"
    val step5LifelineAccessibilityNote: String,

    // Step 6: Making the environment safe (2 numbered lines)
    val step6MakingEnvironmentSafe: List<String>,

    // Bottom section
    val mostImportantThingWorthLivingFor: String,
    val followUpCallConsent: Boolean,
    val followUpCallDate: String?,

    // Nurse signature (Policy Rule 7)
    val nurseSignatureCaptured: Boolean,
    val nurseSignatureTimestamp: String?,

    // Validation metadata
    val validationPassed: Boolean,
    val validationMessages: List<String>
)

@Serializable
data class DistractionEntry(val name: String, val place: String)

@Serializable
data class SupporterEntry(val name: String, val phone: String)

/**
 * Maps the master schema to a printable Safety Plan output.
 *
 * This is the FIRST GATE form — JP must review this output on 10 synthetic
 * notes before Phase 9 begins.
 */
object SafetyPlanMapper {

    /** Pre-printed Lifeline accessibility note — exact text from JP's form (Image 11). */
    private const val LIFELINE_ACCESSIBILITY_NOTE =
        "Deaf and hard of hearing users can access Lifeline through the " +
        "SignVideo App available on smartphones by downloading through the " +
        "app store. Or via text phone on 18001 0808 808 8000. Calls to " +
        "Lifeline are free to people living in Northern Ireland who are " +
        "calling from UK landlines and mobiles."

    fun map(schema: MasterSchema): SafetyPlanOutput {
        val sp = schema.safetyPlan
        val patient = schema.patient

        // Split warning signals into numbered lines (paper form has 3)
        val warningSignals = splitToNumberedLines(sp.step1WarningSignals, 3)
        val copingStrategies = splitToNumberedLines(sp.step2InternalCopingStrategies, 3)
        val environmentSafe = splitToNumberedLines(sp.step6MakingEnvironmentSafe, 2)

        // Step 3: distraction people + places
        val distractions = parseSocialSettings(sp.step3SocialSettingsForDistraction)

        // Step 4: supporters from the typed list
        val supporters = sp.step4PeopleToAskForHelp.map { SupporterEntry(it.name, it.phone) }

        // Step 5: professionals — GP details from Patient, crisis numbers HARD-CODED
        val step5 = sp.step5ProfessionalsAndAgencies

        // Validation messages
        val messages = mutableListOf<String>()
        if (!sp.nurseSignatureCaptured) {
            messages.add("Rule 7: Nurse digital signature required before finalising.")
        }
        if (step5.lifeline247 != "0808 808 8000") {
            messages.add("Rule 10: Lifeline number is incorrect or missing.")
        }
        if (step5.gpOohBeldoc != "02890744447") {
            messages.add("Rule 10: BelDOC number is incorrect or missing.")
        }
        if (step5.gpOohSebdoc != "02890796220") {
            messages.add("Rule 10: SEBDOC number is incorrect or missing.")
        }

        return SafetyPlanOutput(
            patientName = patient.fullName,
            hcNumber = patient.hcNumber,
            assessmentDate = schema.assessmentDate,
            assessorName = schema.referral.assessorName,

            step1WarningSignals = warningSignals,
            step2InternalCopingStrategies = copingStrategies,
            step3DistractionPeoplePlaces = distractions,
            step4Supporters = supporters,

            step5ProfessionalName = step5.otherContacts.firstOrNull(),
            step5ProfessionalPhone = null, // filled by nurse review
            step5GpName = step5.gpName,
            step5GpPhone = step5.gpPhone,
            step5GpOohBeldoc = "02890744447",   // ALWAYS pre-printed
            step5GpOohSebdoc = "02890796220",   // ALWAYS pre-printed
            step5Lifeline247 = "0808 808 8000", // ALWAYS pre-printed
            step5LifelineAccessibilityNote = LIFELINE_ACCESSIBILITY_NOTE,

            step6MakingEnvironmentSafe = environmentSafe,
            mostImportantThingWorthLivingFor = sp.mostImportantReasonToLive,
            followUpCallConsent = sp.followUpCallAgreed,
            followUpCallDate = sp.followUpCallDatetime,

            nurseSignatureCaptured = sp.nurseSignatureCaptured,
            nurseSignatureTimestamp = sp.nurseSignatureTimestamp,

            validationPassed = messages.isEmpty(),
            validationMessages = messages
        )
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Split a free-text field into numbered lines for the paper form.
     * If the nurse typed comma/semicolon/newline delimited items, split on those.
     * If not, wrap the whole string as a single item and pad with empty strings.
     */
    internal fun splitToNumberedLines(text: String, maxLines: Int): List<String> {
        val items = text
            .split(Regex("[,;\n]"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        return if (items.isEmpty()) {
            List(maxLines) { "" }
        } else {
            (items + List(maxLines) { "" }).take(maxLines)
        }
    }

    /**
     * Parse "Name: X, Place: Y; Name: Z, Place: W" style free-text into
     * structured distraction entries. Falls back to best-effort splitting.
     */
    internal fun parseSocialSettings(text: String): List<DistractionEntry> {
        // Try structured "Name: ... Place: ..." format first
        val namePattern = Regex("(?i)name\\s*[:=]\\s*([^,;\\n]+)")
        val placePattern = Regex("(?i)place\\s*[:=]\\s*([^,;\\n]+)")

        val names = namePattern.findAll(text).map { it.groupValues[1].trim() }.toList()
        val places = placePattern.findAll(text).map { it.groupValues[1].trim() }.toList()

        if (names.isNotEmpty()) {
            return names.mapIndexed { i, name ->
                DistractionEntry(name, places.getOrElse(i) { "" })
            }
        }

        // Fallback: split by semicolons or newlines, each becomes a name with blank place
        val items = text.split(Regex("[;\n]")).map { it.trim() }.filter { it.isNotEmpty() }
        return items.map { DistractionEntry(it, "") }
    }
}
