package com.example.medgem.fhir

import com.belfasttrust.jpclinical.domain.mappers.SafetyPlanOutput
import com.belfasttrust.jpclinical.domain.mappers.PisaniOutput
import org.hl7.fhir.r4.model.*
import java.util.Date
import java.util.UUID

object FhirSerializer {

    fun safetyPlanToFhir(
        plan: SafetyPlanOutput,
        patientId: String,
        nurseId: String
    ): Bundle {
        val bundle = Bundle().apply {
            id = UUID.randomUUID().toString()
            type = Bundle.BundleType.DOCUMENT
            timestamp = Date()
            identifier = Identifier().apply {
                system = "urn:belfasttrust:fhir:bundle"
                value = "safety-plan-${plan.hcNumber}-${System.currentTimeMillis()}"
            }
        }

        // Add Patient
        val fhirPatient = Patient().apply {
            id = patientId
            addIdentifier().apply {
                system = "urn:belfasttrust:hc-number"
                value = plan.hcNumber
            }
            addName().apply {
                family = plan.patientName.split(" ").lastOrNull() ?: plan.patientName
                addGiven(plan.patientName.split(" ").firstOrNull() ?: plan.patientName)
            }
        }
        bundle.addEntry().apply {
            fullUrl = "urn:uuid:${fhirPatient.id}"
            resource = fhirPatient
        }

        // Add CarePlan
        val carePlan = CarePlan().apply {
            id = UUID.randomUUID().toString()
            status = CarePlan.CarePlanStatus.ACTIVE
            intent = CarePlan.CarePlanIntent.PLAN
            subject = Reference("urn:uuid:${fhirPatient.id}")
            
            // Set author (nurse)
            author = Reference().apply {
                reference = "urn:uuid:$nurseId"
                display = plan.assessorName
            }
            
            title = "Belfast Trust HTT Safety Plan"
            description = "Stanley-Brown Safety Plan for suicide prevention"
            
            // Step 1: Warning Signals
            addActivity().apply {
                detail = CarePlan.CarePlanActivityDetailComponent().apply {
                    status = CarePlan.CarePlanActivityStatus.INPROGRESS
                    description = "Step 1: Warning Signals: " + plan.step1WarningSignals.filter { it.isNotBlank() }.joinToString("; ")
                }
            }

            // Step 2: Internal Coping Strategies
            addActivity().apply {
                detail = CarePlan.CarePlanActivityDetailComponent().apply {
                    status = CarePlan.CarePlanActivityStatus.INPROGRESS
                    description = "Step 2: Internal Coping Strategies: " + plan.step2InternalCopingStrategies.filter { it.isNotBlank() }.joinToString("; ")
                }
            }

            // Step 3: Social Settings and People for Distraction
            addActivity().apply {
                detail = CarePlan.CarePlanActivityDetailComponent().apply {
                    status = CarePlan.CarePlanActivityStatus.INPROGRESS
                    description = "Step 3: Distraction settings/people: " + plan.step3DistractionPeoplePlaces.joinToString("; ") { "${it.name} at ${it.place}" }
                }
            }

            // Step 4: Supporters
            addActivity().apply {
                detail = CarePlan.CarePlanActivityDetailComponent().apply {
                    status = CarePlan.CarePlanActivityStatus.INPROGRESS
                    description = "Step 4: Supporters to contact: " + plan.step4Supporters.joinToString("; ") { "${it.name} (${it.phone})" }
                }
            }

            // Step 5: Professionals and Agencies
            addActivity().apply {
                detail = CarePlan.CarePlanActivityDetailComponent().apply {
                    status = CarePlan.CarePlanActivityStatus.INPROGRESS
                    description = "Step 5: Professionals: GP ${plan.step5GpName} (${plan.step5GpPhone}), BelDOC ${plan.step5GpOohBeldoc}, SEBDOC ${plan.step5GpOohSebdoc}, Lifeline ${plan.step5Lifeline247}"
                }
            }

            // Step 6: Making the Environment Safe
            addActivity().apply {
                detail = CarePlan.CarePlanActivityDetailComponent().apply {
                    status = CarePlan.CarePlanActivityStatus.INPROGRESS
                    description = "Step 6: Making environment safe: " + plan.step6MakingEnvironmentSafe.filter { it.isNotBlank() }.joinToString("; ")
                }
            }

            // Reason to live
            addActivity().apply {
                detail = CarePlan.CarePlanActivityDetailComponent().apply {
                    status = CarePlan.CarePlanActivityStatus.INPROGRESS
                    description = "Reason to live: " + plan.mostImportantThingWorthLivingFor
                }
            }
            
            // Signature verification info
            if (plan.nurseSignatureCaptured) {
                val annotation = Annotation().apply {
                    text = "Digitally signed by ${plan.assessorName} at ${plan.nurseSignatureTimestamp}"
                }
                addNote(annotation)
            }
        }
        bundle.addEntry().apply {
            fullUrl = "urn:uuid:${carePlan.id}"
            resource = carePlan
        }

        return bundle
    }

    fun pisaniToFhir(
        assessment: PisaniOutput,
        patientId: String,
        nurseId: String
    ): Bundle {
        val bundle = Bundle().apply {
            id = UUID.randomUUID().toString()
            type = Bundle.BundleType.DOCUMENT
            timestamp = Date()
            identifier = Identifier().apply {
                system = "urn:belfasttrust:fhir:bundle"
                value = "pisani-${assessment.hcNumber}-${System.currentTimeMillis()}"
            }
        }

        // Add Patient
        val fhirPatient = Patient().apply {
            id = patientId
            addIdentifier().apply {
                system = "urn:belfasttrust:hc-number"
                value = assessment.hcNumber
            }
            addName().apply {
                family = assessment.patientName.split(" ").lastOrNull() ?: assessment.patientName
                addGiven(assessment.patientName.split(" ").firstOrNull() ?: assessment.patientName)
            }
        }
        bundle.addEntry().apply {
            fullUrl = "urn:uuid:${fhirPatient.id}"
            resource = fhirPatient
        }

        // Add RiskAssessment
        val riskAssessment = RiskAssessment().apply {
            id = UUID.randomUUID().toString()
            status = RiskAssessment.RiskAssessmentStatus.FINAL
            subject = Reference("urn:uuid:${fhirPatient.id}")
            
            // Assessor (Practitioner)
            performer = Reference().apply {
                reference = "urn:uuid:$nurseId"
                display = assessment.assessorName
            }
            
            occurrence = DateTimeType(assessment.assessmentDate)
            
            // Add PISANI domains as notes/annotations
            addNote(Annotation().apply { text = "Domain 1 (Strengths & Protective): ${assessment.strengthsAndProtectiveFactors}" })
            addNote(Annotation().apply { text = "Domain 2 (Long Term Risk): ${assessment.longTermRiskFactors}" })
            addNote(Annotation().apply { text = "Domain 3 (Impulsivity): ${assessment.impulsivityAndSelfControl}" })
            addNote(Annotation().apply { text = "Domain 4 (Past Suicidal): ${assessment.pastSuicidalBehaviours}" })
            addNote(Annotation().apply { text = "Domain 5 (Recent/Present Suicidal): ${assessment.recentAndPresentSuicidalBehaviours}" })
            addNote(Annotation().apply { text = "Domain 6 (Stressors): ${assessment.stressorsAndPrecipitants}" })
            addNote(Annotation().apply { text = "Domain 7 (Symptoms & Suffering): ${assessment.symptomsSufferingRecentChanges}" })
            addNote(Annotation().apply { text = "Domain 8 (Engagement): ${assessment.engagementAndReliability}" })
            
            // Add Risk level prediction
            addPrediction().apply {
                qualitativeRisk = CodeableConcept().apply {
                    addCoding().apply {
                        system = "urn:belfasttrust:risk-level"
                        code = assessment.overallRiskLevel.name.lowercase()
                        display = assessment.overallRiskLevel.name
                    }
                    text = assessment.overallRiskLevel.name
                }
            }
        }
        bundle.addEntry().apply {
            fullUrl = "urn:uuid:${riskAssessment.id}"
            resource = riskAssessment
        }

        return bundle
    }
}
