package com.belfasttrust.jpclinical

import com.belfasttrust.jpclinical.domain.extraction.ExtractionPromptBuilder
import kotlin.test.Test
import kotlin.test.assertTrue

class ExtractionPromptBuilderTest {

    @Test
    fun testCluster1PromptContainsAbbreviationsGlossary() {
        val prompt = ExtractionPromptBuilder.buildCluster1Prompt("Fictional note")
        assertTrue(prompt.contains("BELFAST TRUST CLINICAL ABBREVIATIONS GLOSSARY:"), "Glossary title missing")
        assertTrue(prompt.contains("- TLNWL: Thoughts of Life Not Worth Living"), "TLNWL abbreviation missing")
        assertTrue(prompt.contains("- PISANI: Risk assessment framework (8 domains)"), "PISANI abbreviation missing")
    }

    @Test
    fun testCluster2PromptContainsNegationRule() {
        val prompt = ExtractionPromptBuilder.buildCluster2Prompt("Fictional note", "{}")
        assertTrue(prompt.contains("Rule 4 CRITICAL: Negation means absence. If notes say \"no suicidal ideation\" or \"denied TLNWL\" the suicidality fields must reflect ABSENT. Discussing a symptom is not the same as its presence."), "Negation rule missing")
    }

    @Test
    fun testCluster3PromptContainsPisaniJudgmentRequiredRule() {
        val prompt = ExtractionPromptBuilder.buildCluster3Prompt("Fictional note", "{}", "{}")
        assertTrue(prompt.contains("Rule 5: PISANI fields for impulsivity_and_self_control and engagement_and_reliability must ALWAYS include _judgment_required: true regardless of confidence. These always go to the clarification queue."), "PISANI judgment rule missing")
    }

    @Test
    fun testCluster3PromptContainsStep5ExclusionRule() {
        val prompt = ExtractionPromptBuilder.buildCluster3Prompt("Fictional note", "{}", "{}")
        assertTrue(prompt.contains("Rule 6: NEVER populate safety_plan.step5_professionals from the notes. Leave all fields in that object null. They are pre-filled by the system with pre-printed crisis numbers."), "Step 5 exclusion rule missing")
    }
}
