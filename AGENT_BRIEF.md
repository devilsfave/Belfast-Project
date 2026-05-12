# JP CLINICAL DOCUMENTATION AI — COMPLETE AGENT BRIEF
**Read every section before writing a single line of code.**
**This document is your sole source of truth for this project.**

---

## SECTION 1: WHO YOU ARE WORKING WITH AND HOW

You are working with Herbert Kwame Yeboah (Devlin), based in Ghana. Herbert is the architect and human in the loop. He does not hand-code. He directs agents. You build under his instruction. He will report your actions and outputs to a senior AI supervisor who holds the full project context. When you are uncertain about a clinical or architectural decision, stop and report rather than guess. Never make clinical assumptions. Never invent form fields. If a field is not in this document, flag it and wait.

Herbert uses Claude as his strategic supervisor in the browser. Decisions made by that supervisor are passed to you through Herbert. Do not deviate from this brief without Herbert's explicit instruction.

---

## SECTION 2: THE PROBLEM THIS PROJECT SOLVES

JP is a mental health nurse at Belfast Trust Home Treatment Team (HTT) in Northern Ireland. He visits roughly five patients per day at their homes or at clinic. After every shift he spends four hours, typically 5pm to 9pm, converting rough handwritten or mental notes into more than twenty complex clinical forms. Missing even one mandatory field is a legal and professional risk under NMC standards, UK GDPR, and Belfast Trust Information Governance policy.

JP and his colleagues carry smartphones during shifts. They do not carry laptops to patient visits. The documentation session happens after the shift, on phone or base PC.

The goal of this application is to reduce four hours of documentation to approximately twenty minutes by letting the nurse speak or type rough notes, having the AI extract all relevant clinical data into a structured master schema, and then automatically populating all required forms. The nurse reviews and approves every output before anything is submitted. The system is never autonomous. The nurse is always the decision maker. This framing is legally required.

---

## SECTION 3: FIRST ACTIONS — GIT SETUP

When Herbert opens a new folder and gives you control, execute the following in order:

```bash
# Initialise a new git repository in the current folder
git init

# Clone the MedGEM reference project into a subfolder called medgem-reference
git clone https://github.com/kamalkraj/MedGem.git medgem-reference

# Create the main project directory structure
mkdir -p jp-clinical-ai/app/src/main/kotlin/com/belfasttrust/jpclinical
mkdir -p jp-clinical-ai/app/src/main/res
mkdir -p jp-clinical-ai/docs
mkdir -p jp-clinical-ai/schemas
mkdir -p jp-clinical-ai/synthetic-data

# Copy the MedGEM Android app structure as the project foundation
cp -r medgem-reference/app jp-clinical-ai/
cp medgem-reference/build.gradle.kts jp-clinical-ai/
cp medgem-reference/settings.gradle.kts jp-clinical-ai/
cp medgem-reference/gradle.properties jp-clinical-ai/
cp -r medgem-reference/gradle jp-clinical-ai/
cp medgem-reference/gradlew jp-clinical-ai/
cp medgem-reference/gradlew.bat jp-clinical-ai/

# Initialise git tracking for the main project
cd jp-clinical-ai
git init
git add .
git commit -m "Initial: Foundation from MedGEM reference implementation"
```

After these commands complete, report back to Herbert with the directory structure and confirm the MedGEM files are in place before proceeding.

---

## SECTION 4: WHAT MEDGEM IS — READ THIS BEFORE TOUCHING ANYTHING

MedGEM is a fully working offline Android clinical AI application built by kamalkraj. It already solves the hardest engineering problems in this project. You must understand what it does before modifying anything.

**What MedGEM already contains and runs successfully:**

- MedGemma 1.5 4B running via ExecuTorch (`8da4w` format), tested and confirmed clinically equivalent to the original HuggingFace weights. Pre-converted model at `kamalkraj/medgemma-1.5-4b-it-executorch`.
- MedASR running via ONNX int8 through sherpa-onnx, confirmed 0.00% word error rate difference from original. Pre-converted model at `kamalkraj/medasr-onnx`.
- EmbeddingGemma 300M running via LiteRT int8, confirmed 0.9987 cosine similarity. Pre-converted model at `kamalkraj/embeddinggemma-300m-litert`.
- ObjectBox as the embedded database for crash-proof state management.
- Jetpack Compose UI.
- Patient management screens.
- SOAP note generation from visit data.
- RAG (Retrieval-Augmented Generation) pipeline with a medical knowledge base.
- Audio recording pipeline feeding directly into MedASR.

**Device requirements from MedGEM:**
- Android 14 (API 34) or higher
- 8GB RAM minimum with vision encoder disabled (vision encoder is irrelevant for this project, disable it on day one)
- 12GB RAM for full multimodal (not needed here)
- Approximately 4GB free storage for model files

**What MedGEM does not have that you will build:**
- Belfast Trust specific form schemas
- Master JSON schema extraction pipeline
- Clarification Queue for judgment-required fields
- Policy Validator with ten mandatory rules
- FHIR R4 serialisation for Epic EHR
- Epic EHR sync gateway
- Per-session audit log with timestamps and nurse approvals
- The specific scoring logic for LDQ, AUDIT, and UNOCINI trigger

---

## SECTION 5: THE INFERENCE STACK — DO NOT TOUCH

These components from MedGEM must not be modified. They work. Touching them risks breaking the only thing that is already proven.

- ExecuTorch inference engine and model loading for MedGemma
- sherpa-onnx integration and audio pipeline for MedASR
- LiteRT delegate and model loading for EmbeddingGemma
- ObjectBox database initialisation and core entity definitions
- Gradle build configuration for native libraries (JNI, AAR dependencies)
- Model download and file management utilities

If you need to extend ObjectBox with new entities, add new entity classes. Do not modify existing ones.

---

## SECTION 6: COMPLETE TECHNICAL STACK

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| LLM inference | ExecuTorch (`8da4w`) via MedGEM integration |
| ASR | sherpa-onnx (MedASR ONNX int8) |
| Embedding | LiteRT int8 (EmbeddingGemma 300M) |
| Database | ObjectBox |
| State management | Kotlin StateFlow + Coroutines |
| JSON handling | kotlinx.serialization |
| FHIR serialisation | HAPI FHIR R4 library (add to Gradle) |
| Encryption | Android Keystore + AES-256 |
| Target OS | Android 14+ (API 34) |
| Min RAM | 8GB (vision encoder disabled) |
| Build system | Gradle Kotlin DSL |

---

## SECTION 7: THE PIPELINE ARCHITECTURE

This is the core processing pipeline. Every patient session follows this exact sequence. Each step must checkpoint to ObjectBox before the next step begins.

```
STEP 1: INPUT
Nurse speaks or types rough clinical notes after patient visit.
Voice path: Audio recorded by phone mic → MedASR (sherpa-onnx) → text transcript
Text path: Nurse types directly into text field
Both paths produce the same output: a raw text string.

STEP 2: MASTER SCHEMA EXTRACTION
Raw text is passed to MedGemma 1.5 4B via ExecuTorch.
System prompt instructs the model to populate the master JSON schema
(defined in Section 9 of this document).
Output: a partially or fully populated master JSON object.
Every field in the schema has a confidence score (0.0 to 1.0).
Fields with confidence below 0.80 are flagged as JUDGMENT_REQUIRED.
This step runs in THREE CLUSTERS to prevent OOM crashes:
  Cluster 1: Patient demographics, confidentiality, collateral, history of presenting complaint
  Cluster 2: Mental State Examination, medication section
  Cluster 3: Risk assessment (PISANI), substance misuse, social circumstances, safety plan
After each cluster completes, write results to ObjectBox before starting the next cluster.

STEP 3: POLICY VALIDATION (FIRST PASS)
Run the Policy Validator against the partially extracted schema.
Hard blocks are fields that MUST be present. If any hard block field is null,
add it to the Clarification Queue regardless of the confidence score.
See Section 11 for all ten policy rules.

STEP 4: CLARIFICATION QUEUE
Collect all JUDGMENT_REQUIRED fields and all hard block failures into a single list.
Present this list to the nurse as a targeted question screen.
The nurse answers each question via text or voice.
Re-run affected schema fields with the nurse's answers appended to context.
Do not show the nurse more than twenty questions at once.

STEP 5: POLICY VALIDATION (FINAL PASS)
Run all ten policy rules against the finalised schema.
Hard blocks that still fail must halt the pipeline and display a blocking error.
Soft flags display a warning the nurse must acknowledge by typing a confirmation note.
Every validation result is written to the audit log.

STEP 6: FORM GENERATION
For each of the 20+ Belfast Trust forms, a form mapper reads the finalised
master schema and populates the form's fields through mechanical lookups.
No AI involvement at this stage. Pure data mapping.
Each generated form is displayed to the nurse for review and correction.

STEP 7: NURSE APPROVAL AND DIGITAL SIGNATURE
The nurse reviews each generated form screen by screen.
The nurse taps an approve button for each form.
The Safety Plan requires a digital signature captured through the device's
touch/gesture layer before it can be marked approved.
Every approval is written to the audit log with a timestamp.

STEP 8: FHIR SERIALISATION
Approved forms are serialised into FHIR R4 format using HAPI FHIR.
FHIR bundles are encrypted with AES-256 and stored in the device sandbox.
Nothing is transmitted yet.

STEP 9: EPIC SYNC (MANUAL TRIGGER ONLY)
When the nurse taps the sync button and WiFi is available, the app sends
encrypted FHIR bundles to the Belfast Trust Epic EHR gateway over HTTPS TLS 1.3.
The nurse must be authenticated with NHS credentials via SMART on FHIR.
No background sync. No automatic transmission. Ever.
The audit log records every sync attempt and its outcome.
```

---

## SECTION 8: THE FIVE SYSTEM COMPONENTS YOU ARE BUILDING

### Component 1: Clarification Agent

Handles all cases where MedGemma could not extract a field with sufficient confidence or where a mandatory field is missing.

Fields fall into two categories:
- **EXTRACTABLE**: Fields the AI can pull directly from notes (name, date, medication names, etc.)
- **JUDGMENT_REQUIRED**: Fields requiring clinical interpretation (Impulsivity, Insight, risk level, etc.)

For any field where confidence is below 0.80, generate a specific targeted question. Batch all questions into a single Clarification Queue screen shown after the extraction clusters complete. The nurse answers once. The schema is finalised. Nothing is guessed.

Example question format:
```
"Your notes mention low mood but do not specify whether this is subjective or objective.
How would you rate JP's objective affect during the assessment? [euthymic / depressed / elated / labile]"
```

### Component 2: Policy Validator

Ten mandatory rules defined below in Section 11. Implement each as a pure Kotlin function that takes the master schema as input and returns a `ValidationResult` object containing:
- `ruleId: Int`
- `passed: Boolean`
- `severity: Severity` (HARD_BLOCK or SOFT_FLAG)
- `message: String`
- `affectedFields: List<String>`

Hard blocks halt the pipeline. Soft flags display a warning. Every result is logged to the audit log regardless of pass or fail.

### Component 3: Audit Log

Extend ObjectBox with an `AuditEntry` entity containing:
- `id: Long`
- `sessionId: String`
- `timestamp: Long` (epoch milliseconds)
- `eventType: AuditEventType` (EXTRACTION_START, EXTRACTION_COMPLETE, VALIDATION_RUN, VALIDATION_RESULT, CLARIFICATION_ASKED, CLARIFICATION_ANSWERED, FORM_GENERATED, NURSE_APPROVED, NURSE_OVERRIDE, SYNC_TRIGGERED, SYNC_COMPLETE, SYNC_FAILED)
- `details: String` (JSON string with event-specific data)
- `nurseId: String`

The audit log is never deleted from the device. If there is ever a legal challenge, this log proves the nurse reviewed and confirmed every document as an active informed decision.

### Component 4: Data Factory (Synthetic Notes Generator)

A utility screen accessible from a developer settings menu. Not visible in production build.

Generates fictional Belfast Trust flavoured clinical notes for testing. Uses profile injection with these variables:
- 10 clinical diagnoses (depression, psychosis, bipolar, PTSD, anxiety, personality disorder, substance misuse, OCD, eating disorder, schizophrenia)
- 4 risk levels (low, medium, high, very high)
- 5 complicating factors (children in home, history of violence, substance use, no fixed abode, recent bereavement)
- 3 documentation quality levels (detailed, average, minimal/rushed)

Each generated note must come with a pre-labelled ground truth master schema JSON showing what the correct extraction output should be. This is used for testing extraction accuracy.

### Component 5: Form Generator Engine

A pure Kotlin mapping layer. For each Belfast Trust form, define a mapper class that reads specific fields from the master schema and returns a populated form data class. No AI at this stage. These are mechanical lookups.

Example structure:
```kotlin
class SafetyPlanMapper {
    fun map(schema: MasterSchema): SafetyPlanForm {
        return SafetyPlanForm(
            step1WarningSign = schema.safetyPlan.warningSignInternal,
            step2CopingStrategies = schema.safetyPlan.internalCopingStrategies,
            // ... etc
            lifeline247 = "0808 808 8000", // Pre-printed, never extracted from notes
            gpOohBelDoc = "02890744447", // Pre-printed
            gpOohSebDoc = "02890796220" // Pre-printed
        )
    }
}
```

---

## SECTION 9: MASTER JSON SCHEMA

This is the load-bearing data structure. Every form is populated from this schema. MedGemma populates it. You never re-read the raw notes after this schema is built.

```json
{
  "session_id": "string",
  "nurse_id": "string",
  "assessment_date": "ISO8601 date",
  "assessment_time": "HH:mm",
  "visit_type": "new_assessment | review | crisis",

  "patient": {
    "full_name": "string",
    "hc_number": "string",
    "date_of_birth": "ISO8601 date",
    "address": "string",
    "gp_name": "string",
    "gp_address": "string",
    "gp_phone": "string",
    "next_of_kin_name": "string",
    "next_of_kin_relationship": "string",
    "next_of_kin_phone": "string",
    "marital_status": "string",
    "ethnicity": "string",
    "gender": "string",
    "place_of_assessment": "string"
  },

  "referral": {
    "referral_agent": "string",
    "referral_source": "string",
    "referral_date": "ISO8601 date",
    "assessor_name": "string",
    "assessor_designation": "string"
  },

  "confidentiality": {
    "confidentiality_explained": "boolean",
    "capacity_to_consent": "boolean | null",
    "consent_to_seek_information": "boolean | null",
    "consent_to_share_information": "boolean | null",
    "information_shareable_with": ["string"],
    "family_carer_consent": "boolean | null",
    "consent_to_phone_contact": "boolean | null",
    "third_party_information_restrictions": "string | null"
  },

  "collateral": {
    "collateral_obtained": "boolean",
    "collateral_declined_reason": "string | null",
    "collateral_sources": [
      {
        "name": "string",
        "relationship": "string",
        "information_provided": "string"
      }
    ]
  },

  "history_of_presenting_complaint": {
    "reason_for_presentation": "string",
    "precipitating_factors": "string",
    "course_and_duration": "string",
    "features_of_mental_illness": "string",
    "management_to_date": "string",
    "presenting_suicide_events_48hrs": {
      "occurred": "boolean",
      "description": "string | null",
      "method": "string | null",
      "intent_level": "string | null"
    }
  },

  "mental_state_examination": {
    "appearance_behaviour": {
      "clothing_appropriateness": "string",
      "self_care": "string",
      "motor_activity": "string",
      "rapport": "string",
      "eye_contact": "string",
      "overall_narrative": "string"
    },
    "speech_thought_form": {
      "spontaneity": "string",
      "coherence": "string",
      "rate": "string",
      "tone": "string",
      "volume": "string",
      "thought_disorder_present": "boolean",
      "thought_disorder_description": "string | null",
      "overall_narrative": "string"
    },
    "mood_affect": {
      "subjective_mood": "string",
      "objective_affect": "string",
      "affect_fluctuation": "string",
      "mood_classification": "depressed | euthymic | elated | mixed",
      "anxiety_present": "boolean",
      "anxiety_description": "string | null",
      "reactivity": "string",
      "overall_narrative": "string"
    },
    "suicidality": {
      "tlnwl": "string",
      "suicidal_ideation_present": "boolean",
      "suicidal_ideation_description": "string | null",
      "plan_present": "boolean",
      "plan_description": "string | null",
      "intent_present": "boolean",
      "intent_description": "string | null",
      "tsh": "string",
      "self_harm_plan": "string | null",
      "suicide_attempt_this_presentation": "boolean",
      "advice_given_on_reattempt_risk": "string",
      "overall_narrative": "string"
    },
    "thought_content": {
      "preoccupations": "string | null",
      "obsessions": "string | null",
      "delusions_present": "boolean",
      "delusions_description": "string | null",
      "paranoid_thoughts": "string | null",
      "thought_interference": "boolean",
      "passivity_phenomena": "boolean",
      "violent_thoughts": "boolean",
      "violent_thoughts_description": "string | null",
      "overall_narrative": "string"
    },
    "perceptual_disturbances": {
      "auditory_hallucinations": "boolean",
      "auditory_description": "string | null",
      "visual_hallucinations": "boolean",
      "visual_description": "string | null",
      "gustatory_hallucinations": "boolean",
      "olfactory_hallucinations": "boolean",
      "tactile_hallucinations": "boolean",
      "overall_narrative": "string"
    },
    "cognition": {
      "orientation_time": "boolean",
      "orientation_place": "boolean",
      "orientation_person": "boolean",
      "attention_intact": "boolean",
      "memory_intact": "boolean",
      "mmse_indicated": "boolean",
      "mmse_score": "integer | null",
      "overall_narrative": "string"
    },
    "insight": {
      "awareness_of_illness": "string",
      "willingness_to_engage": "string",
      "insight_level": "full | partial | none",
      "overall_narrative": "string"
    }
  },

  "history": {
    "mental_health_history": {
      "diagnosis": "string | null",
      "previous_service_contact": "string | null",
      "previous_admissions": "string | null",
      "mho_use": "boolean",
      "mho_details": "string | null",
      "previous_self_harm": "string | null",
      "recent_suicide_events_last_2_months": "string | null",
      "recent_suicide_events_before_2_months": "string | null"
    },
    "personal_history": {
      "early_childhood": "string | null",
      "developmental_milestones": "string | null",
      "schooling": "string | null",
      "psychosexual_history": "string | null",
      "trauma_history": "string | null",
      "relationship_history": "string | null",
      "employment_history": "string | null",
      "spiritual_cultural_needs": "string | null"
    },
    "family_history": {
      "living_arrangements": "string",
      "family_dynamics": "string | null",
      "accommodation_type": "string",
      "ability_to_manage_independently": "string",
      "carers_involved": "boolean",
      "carer_details": "string | null",
      "access_to_lethal_means": "boolean",
      "lethal_means_description": "string | null",
      "fcc_response_to_treatment": "string | null",
      "parents_history": "string | null",
      "siblings_history": "string | null",
      "family_history_suicide": "boolean",
      "family_history_addictions": "boolean",
      "family_history_mental_illness": "boolean"
    }
  },

  "abuse": {
    "abuse_issues_identified": "boolean",
    "vulnerability_identified": "boolean",
    "abuse_details": "string | null"
  },

  "offending_history": {
    "forensic_history": "string | null",
    "current_charges": "string | null",
    "pending_charges": "string | null",
    "custodial_history": "string | null",
    "access_to_weapons": "boolean",
    "gun_licence": "boolean | null",
    "gun_licence_details": "string | null"
  },

  "social_circumstances": {
    "housing": "string",
    "finances": "string | null",
    "debts": "string | null",
    "relationships": "string | null",
    "friendships": "string | null",
    "support_network": "string | null",
    "socialising": "string | null",
    "hobbies": "string | null",
    "strengths": "string | null",
    "occupational_needs": "string | null"
  },

  "medications": {
    "current_medications": [
      {
        "name": "string",
        "dosage": "string",
        "frequency": "string",
        "side_effects": "string | null",
        "allergies": "string | null",
        "compliance_issues": "string | null"
      }
    ],
    "medication_autonomy": {
      "removing_from_packaging": "independent | assisted | dependent",
      "reading_labels": "independent | assisted | dependent",
      "taking_right_dose_right_time": "independent | assisted | dependent",
      "swallowing_tablets": "independent | assisted | dependent",
      "using_equipment_aids": "independent | assisted | dependent | not_applicable",
      "storing_safely": "independent | assisted | dependent",
      "disposing_safely": "independent | assisted | dependent",
      "ordering": "independent | assisted | dependent",
      "collecting": "independent | assisted | dependent"
    }
  },

  "substance_misuse": {
    "current_alcohol_use": {
      "present": "boolean",
      "frequency": "string | null",
      "amount": "string | null",
      "duration": "string | null",
      "withdrawal_symptoms": "boolean",
      "cravings": "boolean"
    },
    "current_drug_use": {
      "present": "boolean",
      "substances": ["string"],
      "frequency": "string | null",
      "duration": "string | null",
      "withdrawal_symptoms": "boolean",
      "cravings": "boolean",
      "polysubstance": "boolean"
    },
    "impact_on_life": "string | null",
    "previous_substance_use": "string | null",
    "abstinence_history": "string | null",
    "complex_factors": {
      "pregnancy": "boolean",
      "injecting_history": "boolean",
      "hiv": "boolean | null",
      "hep_b": "boolean | null",
      "hep_c": "boolean | null"
    },
    "audit_score": {
      "q1": "integer | null",
      "q2": "integer | null",
      "q3": "integer | null",
      "q4": "integer | null",
      "q5": "integer | null",
      "q6": "integer | null",
      "q7": "integer | null",
      "q8": "integer | null",
      "q9": "integer | null",
      "q10": "integer | null",
      "total": "integer | null"
    },
    "ldq_score": {
      "q1": "integer | null",
      "q2": "integer | null",
      "q3": "integer | null",
      "q4": "integer | null",
      "q5": "integer | null",
      "q6": "integer | null",
      "q7": "integer | null",
      "q8": "integer | null",
      "q9": "integer | null",
      "q10": "integer | null",
      "total": "integer | null",
      "dependence_level": "low | medium | high | null",
      "cat_referral_indicated": "boolean"
    }
  },

  "child_protection": {
    "children_in_regular_contact": "boolean",
    "children": [
      {
        "name": "string",
        "sex": "string",
        "date_of_birth": "ISO8601 date",
        "relationship_to_patient": "string",
        "relationship_to_partner": "string | null",
        "other_parent_details": "string | null"
      }
    ],
    "partner_details": "string | null",
    "fcc_social_services_involved": "boolean",
    "unocini_referral_required": "boolean",
    "unocini_trigger_reason": "string | null"
  },

  "pisani_risk_assessment": {
    "strengths_and_protective_factors": "string",
    "long_term_risk_factors": "string",
    "impulsivity_and_self_control": "string",
    "past_suicidal_behaviours": "string",
    "recent_and_present_suicidal_behaviours": "string",
    "stressors_and_precipitants": "string",
    "symptoms_suffering_recent_changes": "string",
    "engagement_and_reliability": "string",
    "overall_risk_level": "low | medium | high | very_high"
  },

  "safety_plan": {
    "step1_warning_signs": "string",
    "step2_internal_coping_strategies": "string",
    "step3_social_settings_for_distraction": "string",
    "step4_people_to_ask_for_help": [
      {
        "name": "string",
        "phone": "string"
      }
    ],
    "step5_professionals_and_agencies": {
      "gp_name": "string",
      "gp_phone": "string",
      "gp_ooh_beldoc": "02890744447",
      "gp_ooh_sebdoc": "02890796220",
      "lifeline_247": "0808 808 8000",
      "other_contacts": ["string"]
    },
    "step6_making_environment_safe": "string",
    "most_important_reason_to_live": "string",
    "follow_up_call_agreed": "boolean",
    "follow_up_call_datetime": "ISO8601 datetime | null",
    "nurse_signature_captured": "boolean",
    "nurse_signature_timestamp": "ISO8601 datetime | null"
  },

  "epic_contact_note": {
    "type_of_contact": "string",
    "purpose_of_contact": "string",
    "contact_details_overview": "string",
    "interventions_used": ["string"],
    "risk_summary": "string",
    "plan": "string"
  },

  "field_confidence_scores": {},
  "extraction_cluster": "integer",
  "schema_finalised": "boolean",
  "schema_version": "1.0"
}
```

---

## SECTION 10: BELFAST TRUST FORMS — COMPLETE FIELD REGISTRY

These are the 20+ forms the system must populate. For each form, build a dedicated mapper class in Kotlin under `forms/mappers/`.

### Form 1: Emergency Mental Health Assessment Proforma V4
Fields: patient full name, H&C number, date of birth, assessor name, assessor designation, referral agent, GP name, GP address, place of assessment, date of assessment, time of assessment, next of kin name, next of kin relationship, next of kin phone number. All map directly from `patient` and `referral` schema sections.

### Form 2: Confidentiality Section
Fields: confidentiality explained (Y/N), patient capacity to consent (Y/N/Not assessed), consent to seek information (Y/N), consent to share information (Y/N), who information can be shared with (list), family or carer consent (Y/N), consent to contact by phone (Y/N), restrictions on third party information (free text). All map from `confidentiality` schema section.

### Form 3: Collateral Section
Fields: collateral obtained (Y/N), if not obtained — reason, for each source: name, relationship, information provided. Maps from `collateral` schema section. Policy rule 9 applies.

### Form 4: History of Presenting Complaint
Fields: reason for presentation (free text), precipitating factors (free text), course and duration (free text), features of mental illness (free text), management to date (free text), continuation page flag, presenting suicide events 48hrs subsection (free text, mandatory if any suicidal ideation). Maps from `history_of_presenting_complaint`. Policy rule 8 applies.

### Form 5: Mental State Examination
All MSE subdomains as listed in the master schema. Each subdomain has italicised prompt questions on the paper form — the AI fills the free text narrative for each. All fields map from `mental_state_examination`. Policy rules 1 and 2 apply to the suicidality subsection.

### Form 6: History — Mental Health History
Fields: diagnosis, previous service contact, admissions, MHO use, MHO details, previous self-harm, recent suicide events last 2 months, recent suicide events before 2 months. Maps from `history.mental_health_history`.

### Form 7: History — Mental Health Personal History
Fields: early childhood, developmental milestones, schooling, psychosexual history, trauma history, relationship history, employment history, spiritual and cultural needs. Maps from `history.personal_history`. All fields nullable.

### Form 8: Mental Health Family History
Fields: living arrangements, family dynamics, accommodation type, ability to manage independently, carers involved, carer details, access to lethal means, lethal means description, FCC response to treatment, parents history, siblings history, family history of suicide, family history of addictions, family history of mental illness. Maps from `history.family_history`.

### Form 9: Mental Health Abuse Section
Fields: abuse issues identified (Y/N), vulnerability identified (Y/N), abuse details (free text). Maps from `abuse`.

### Form 10: Mental Health Offending History
Fields: forensic history, current charges, pending charges, custodial history, access to weapons (Y/N), gun licence (Y/N/Unknown), gun licence details. Maps from `offending_history`. Policy rule 4 applies — hard block if null.

### Form 11: Mental Health Social Circumstances
Fields: housing, finances, debts, relationships, friendships, support network, socialising, hobbies, strengths. Maps from `social_circumstances`.

### Form 12: Medication Section
Fields: for each medication — name, dosage, frequency, side effects, allergies, compliance issues. Medication autonomy table: 8 criteria each scored as independent/assisted/dependent. Maps from `medications`.

### Form 13: Mental Health Occupational Needs
Single free text field. Maps from `social_circumstances.occupational_needs`.

### Form 14: Substance Misuse Section
Fields: current alcohol use details, current drug use details (substances list, frequency, duration), withdrawal symptoms, cravings, polysubstance use, impact on life, previous substance use, abstinence history, complex factors (pregnancy, injecting, HIV, Hep B, Hep C). Maps from `substance_misuse`.

### Form 15: AUDIT Alcohol Screening
Ten scored questions, each 0-4. Must be calculated from `substance_misuse.audit_score`. If total exceeds 8 flag for follow-up. Display score and interpretation.

Scoring logic (implement in native Kotlin, not extracted from notes):
- Q1-Q8: score 0-4
- Q9-Q10: score 0,2,4
- Total under 8: lower risk
- Total 8-15: increasing risk
- Total 16-19: higher risk
- Total 20+: possible dependence

### Form 16: Leeds Dependence Questionnaire (LDQ)
Ten questions scored 0 (Never), 1 (Sometimes), 2 (Often), 3 (Nearly Always). Total score calculated in Kotlin.

Scoring logic (implement in native Kotlin):
- Total under 10: low dependence
- Total 10-22: medium dependence
- Total over 22: high dependence
- Score over 20: triggers referral to CAT (Community Addictions Team)
- Score under 20 but positive: consider Addictions NI, Dunlewy, or Daisy (if under 25)

Map from `substance_misuse.ldq_score`. Calculate `cat_referral_indicated` in the mapper based on the total.

### Form 17: Child Protection Details
Fields: patient name, marital status, partner details, for each child — sex, date of birth, relationship to patient, relationship to partner, other parent details. FCC Social Services involvement (Y/N). UNOCINI referral status.

**UNOCINI trigger logic (implement in Kotlin, not AI):**
If `child_protection.children_in_regular_contact == true` AND any of the following are true:
- `pisani_risk_assessment.overall_risk_level` is "high" or "very_high"
- `offending_history.access_to_weapons == true`
- `history.family_history.access_to_lethal_means == true`
- `substance_misuse.complex_factors.injecting_history == true`
Then `unocini_referral_required = true` and policy rule 3 hard block fires.

Policy rule 3 applies: if children are in the home, UNOCINI assessment is mandatory. Hard block.

### Form 18: PISANI Risk Assessment
Eight domains, each as free text plus an overall risk level. Map from `pisani_risk_assessment`.

**Domains:**
1. Strengths and Protective Factors
2. Long Term Risk Factors
3. Impulsivity and Self Control (JUDGMENT_REQUIRED field)
4. Past Suicidal Behaviours
5. Recent and Present Suicidal Behaviours
6. Stressors and Precipitants
7. Symptoms, Suffering, and Recent Changes
8. Engagement and Reliability

Fields 3 and 8 are marked JUDGMENT_REQUIRED — confidence threshold of 0.80 applies. If below threshold, both go to Clarification Queue.

### Form 19: Safety Plan
Six steps of the Stanley-Brown Safety Plan model. Specific crisis phone numbers are pre-printed in the app and never extracted from notes.

**Step 1:** Internal warning signs the patient experiences before a crisis
**Step 2:** Internal coping strategies the patient can use without contacting others
**Step 3:** People and social settings that provide distraction
**Step 4:** People the patient can ask for help (names and phone numbers)
**Step 5:** Professionals and agencies to contact in crisis. Pre-printed values: BelDOC 02890744447, SEBDOC 02890796220, Lifeline 24/7 on 0808 808 8000. These values must be hard-coded in the mapper. Never extracted from notes.
**Step 6:** Making the environment safe (removing means, storing medications safely)

Additional fields: most important reason to live, follow-up call agreement and datetime.

Safety plan requires nurse digital signature before it can be marked approved. Policy rules 7 and 10 apply. Hard blocks.

### Form 20: Epic EHR Contact Note
Fields matching Epic's note entry structure: Type of contact, Purpose of contact, Contact details and general overview, Mental State Examination summary, Suicidality summary, Thought content summary, Perceptual disturbances summary, Cognition summary, Insight summary, Collateral summary, Medications summary, Interventions used (checkbox list), Risk summary, Plan.

**Interventions checkbox list (pre-defined, nurse selects which apply):**
Activity planning, Behavioural activation, Brief intervention, Caffeine intake advice, CBT techniques, Goal setting, Health promotion, Medication concordance monitoring, Medication education, Mental state monitoring, Problem solving, Psychosocial interventions, Reframing thoughts, Recovering well session, Relapse prevention, Risk assessing, Risk management, Sleep hygiene, Social activity, Solution focused approach, Support and reassurance, Symptom management, Symptom recognition.

---

## SECTION 11: POLICY VALIDATOR — ALL TEN RULES

Implement each as a named function in `PolicyValidator.kt`. Hard blocks cannot be overridden. Soft flags can be acknowledged with a typed note that goes into the audit log.

```kotlin
// Rule 1: HARD BLOCK
// Suicidality must be explicitly documented, never implied.
// Fail condition: schema.mental_state_examination.suicidality.tlnwl is null or blank
// OR schema.mental_state_examination.suicidality.suicidal_ideation_present has no narrative

// Rule 2: HARD BLOCK
// Both TLNWL and TSH must be addressed separately as distinct entries.
// Fail condition: either schema.mental_state_examination.suicidality.tlnwl
// OR schema.mental_state_examination.suicidality.tsh is null or blank

// Rule 3: HARD BLOCK
// Children in home triggers mandatory UNOCINI assessment.
// Fail condition: schema.child_protection.children_in_regular_contact == true
// AND schema.child_protection.unocini_referral_required is not resolved

// Rule 4: HARD BLOCK
// Weapons and gun licence must be documented explicitly.
// Fail condition: schema.offending_history.access_to_weapons is null
// OR schema.offending_history.gun_licence is null

// Rule 5: SOFT FLAG
// Protective factors must be identified alongside risk factors.
// Fail condition: schema.pisani_risk_assessment.strengths_and_protective_factors
// is null or blank while schema.pisani_risk_assessment.long_term_risk_factors is not blank

// Rule 6: SOFT FLAG
// Medication concordance must be documented if medications are prescribed.
// Fail condition: schema.medications.current_medications is non-empty
// AND all compliance_issues fields are null

// Rule 7: HARD BLOCK
// Safety plan requires nurse digital signature and timestamp before finalising.
// Fail condition: schema.safety_plan.nurse_signature_captured == false

// Rule 8: HARD BLOCK
// Presenting suicide events in 48 hours must be explicitly addressed.
// Fail condition: schema.history_of_presenting_complaint
//   .presenting_suicide_events_48hrs.occurred is null (not assessed, not documented)

// Rule 9: SOFT FLAG
// Collateral information source must be documented or explicitly declined.
// Fail condition: schema.collateral.collateral_obtained == false
// AND schema.collateral.collateral_declined_reason is null or blank

// Rule 10: HARD BLOCK
// Safety plan must contain the Lifeline number and GP OOH numbers.
// Fail condition: safety plan mapper output does not contain
// "0808 808 8000" AND "02890744447" AND "02890796220"
// (These are pre-printed in the mapper. This rule validates the mapper ran correctly.)
```

---

## SECTION 12: PRIVACY AND LEGAL CONSTRAINTS — NON-NEGOTIABLE

These are not suggestions. Violating any of these makes the application undeployable in an NHS context.

**Data residency:** All patient data stays on the device. Always. No exceptions. The only data that leaves the device is completed, nurse-approved FHIR R4 payloads sent to Belfast Trust's own Epic EHR gateway when the nurse manually triggers a sync.

**No cloud AI:** No patient data or clinical notes may be sent to any external AI service, API, or cloud processor. All inference runs locally via ExecuTorch (MedGemma), sherpa-onnx (MedASR), and LiteRT (EmbeddingGemma).

**No background transmission:** No data is transmitted automatically or in the background. Every sync is a manual nurse-triggered action.

**No consumer AI products:** Real patient data must never touch any consumer AI product including but not limited to OpenAI, Anthropic API (without signed DPA), Google Cloud AI, or any SaaS product.

**Encryption at rest:** All patient data stored in ObjectBox must be encrypted using AES-256 with keys bound to the Android Keystore. Never store keys in code or in shared preferences.

**Audit trail:** Every action taken by the system must be logged to the AuditLog ObjectBox entity with a timestamp, event type, and details. The audit log is the legal defence for the nurse if documentation is ever challenged.

**Framing:** The app is a documentation assistant. The nurse reviews and approves every output. No autonomous clinical decisions. The nurse's approval screen must be explicit, not a rubber stamp.

**NHS standards:**
- DCB0129: Clinical risk management for health software. Every design decision that could affect patient safety must be documented in comments and in a hazard log.
- DCB0160: Clinical risk management during deployment. Testing procedures must be documented.

**Synthetic data only:** During development and testing, use only the fictional patient notes generated by the Data Factory component. Never use real patient data in development or testing environments.

---

## SECTION 13: BUILD SEQUENCE

Work through these phases in order. Do not start a new phase until Herbert confirms the previous phase is working.

### Phase 1: Foundation Verification
Read `docs/ARCHITECTURE.md` and `docs/DATABASE.md` from the MedGEM reference. Map which Kotlin files handle (a) note input and SOAP generation, and (b) ObjectBox schema definitions. Report the file map to Herbert before touching anything.

### Phase 2: Disable Vision Encoder
Locate the vision encoder loading code in MedGEM. Add a build flag `ENABLE_VISION_ENCODER = false`. Verify the app still launches and text inference still works.

### Phase 3: Master Schema Data Classes
Create Kotlin data classes for the full master schema defined in Section 9. Use `@kotlinx.serialization.Serializable` on every class. Create `MasterSchema.kt` in a new package `schema/`. Include all confidence score tracking.

### Phase 4: ObjectBox Entities
Extend ObjectBox with new entities: `ClinicalSession`, `AuditEntry`, `FhirBundle`, `ClarificationItem`. Do not modify existing MedGEM entities.

### Phase 5: Policy Validator
Create `PolicyValidator.kt` implementing all ten rules from Section 11. Write unit tests for each rule covering pass, fail, and edge cases. No rule ships without a passing test.

### Phase 6: Schema Extraction Prompt
Write the MedGemma extraction prompt that takes a raw clinical note and outputs the master schema JSON. Structure the prompt to run in three clusters (see Section 7). Implement confidence scoring. Test against synthetic notes before connecting to live inference.

### Phase 7: Clarification Queue
Build the Clarification Queue screen in Jetpack Compose. Show batched questions. Accept text or voice answers. Re-run schema extraction for affected fields with nurse answers appended.

### Phase 8: Form Mappers
Create mapper classes for all 20+ forms (see Section 10). Start with Safety Plan and PISANI as the first proof of concept — Herbert will demonstrate these to JP first. Each mapper is a pure function with no AI involvement.

### Phase 9: Form Display UI
Build a swipeable form review interface in Jetpack Compose. Each form gets its own composable. The nurse can edit any field before approving. Safety plan approval screen requires captured signature.

### Phase 10: FHIR Serialisation
Add HAPI FHIR R4 dependency to Gradle. Write FHIR serialisers for each form type. Store serialised bundles in ObjectBox encrypted with AES-256.

### Phase 11: Data Factory
Build the synthetic note generator behind a developer settings flag. Test extraction accuracy on 10 generated notes before building the full 1000.

### Phase 12: Epic Sync
Build the manual sync trigger UI. Implement HTTPS TLS 1.3 transmission to the Epic gateway endpoint (endpoint URL to be provided by Belfast Trust IG — placeholder for now). Implement SMART on FHIR authentication.

---

## SECTION 14: MODEL FILES — HOW TO DOWNLOAD

The three model files are already converted and published on HuggingFace by kamalkraj. Use these commands when setting up the test device. Do not re-convert anything.

```bash
# Install HuggingFace CLI
uv tool install hf
hf auth login

# Download LLM (MedGemma 1.5 4B via ExecuTorch)
hf download kamalkraj/medgemma-1.5-4b-it-executorch --local-dir models/llm

# Download ASR (MedASR via sherpa-onnx ONNX int8)
hf download kamalkraj/medasr-onnx --local-dir models/asr

# Download Embedding (EmbeddingGemma 300M via LiteRT int8)
hf download kamalkraj/embeddinggemma-300m-litert --local-dir models/embedding

# Push models to Android device
adb push models/llm /sdcard/MedGem/llm/
adb push models/asr /sdcard/MedGem/asr/
adb push models/embedding /sdcard/MedGem/embedding/
```

---

## SECTION 15: REPORTING PROTOCOL

After completing each task, report to Herbert using this format:

```
PHASE: [current phase number and name]
STATUS: [COMPLETE / IN PROGRESS / BLOCKED]
FILES CHANGED: [list of files created or modified]
TESTS: [number passing / number failing]
ISSUES: [any problems encountered or decisions that need Herbert's input]
NEXT: [what you will do next]
```

If you encounter a clinical decision (a form field behaviour you are unsure about, a policy rule edge case, a schema field that is ambiguous), stop. Add it to a file called `CLINICAL_QUERIES.md` in the project root and report it to Herbert. Do not guess on clinical matters.

If you encounter a technical decision that is not covered by this brief, make the most conservative choice, document it in a comment, and flag it in your next report.

---

## SECTION 16: KEY ABBREVIATIONS USED BY BELFAST TRUST NURSES

Your prompts to MedGemma must handle these abbreviations correctly. Include this glossary in the system prompt for schema extraction.

| Abbreviation | Meaning |
|---|---|
| HTT | Home Treatment Team |
| TLNWL | Thoughts of Life Not Worth Living |
| TSH | Thoughts of Self Harm |
| MSE | Mental State Examination |
| PISANI | Risk assessment framework (8 domains) |
| UNOCINI | Understanding the Needs of Children in Northern Ireland (referral form) |
| MHO | Mental Health Officer |
| FCC | Family and Children's Carers |
| AUDIT | Alcohol Use Disorders Identification Test |
| LDQ | Leeds Dependence Questionnaire |
| CAT | Community Addictions Team |
| SOAP | Subjective Objective Assessment Plan (note format) |
| H&C | Health and Care (patient ID number) |
| BelDOC | Belfast Doctor on Call (GP out of hours, 02890744447) |
| SEBDOC | South and East Belfast Doctor on Call (02890796220) |
| OOH | Out of Hours |
| NMC | Nursing and Midwifery Council |
| FHIR | Fast Healthcare Interoperability Resources |
| EHR | Electronic Health Record |
| IG | Information Governance |
| DPA | Data Processing Agreement |

---

## SECTION 17: FIRST PROOF OF CONCEPT TARGET

The first working demo is: a pipeline that takes a fictional patient note as input and outputs a completed Safety Plan and PISANI Risk Assessment as structured JSON.

When Phase 8 (Form Mappers) is complete for Safety Plan and PISANI only, stop and report to Herbert. He will send these outputs to JP for clinical review. JP's feedback on the first ten synthetic notes determines whether the extraction quality is sufficient before continuing.

This is the gate. Nothing moves to Phase 9 until JP has reviewed and confirmed the Safety Plan and PISANI outputs are clinically accurate.

---

*Last updated: May 2026*
*Project supervisor: Claude (Anthropic) via Herbert Kwame Yeboah*
*Clinical domain expert: JP (Belfast Trust HTT)*
*MedGEM reference: github.com/kamalkraj/MedGem*
