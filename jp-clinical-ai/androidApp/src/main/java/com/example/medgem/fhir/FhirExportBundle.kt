package com.example.medgem.fhir

import org.hl7.fhir.r4.model.Bundle

data class FhirExportBundle(
    val safetyPlanBundle: Bundle,
    val pisaniBundle: Bundle
)
