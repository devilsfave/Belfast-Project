# Handover Documentation: Belfast Clinical AI (Next Agent Instructions)

> [!IMPORTANT]
> **CRITICAL WARNING ON PROJECT PATHS (MUST READ BEFORE WRITING CODE)**
> Due to legacy setup files copied during initial project bootstrap, the project contains a duplicate folder structure:
> 1. `androidApp/src/main/java/com/example/medgem/...` (**COMPILED PATH — USE THIS ONE**)
> 2. `androidApp/app/src/main/java/com/example/medgem/...` (**UNUSED PATH — DO NOT USE**)
> 
> The Gradle module `:androidApp` evaluates from the root `androidApp/src/` folder. **Any new files you create or modify must be placed in `androidApp/src/`**, NOT `androidApp/app/src/`. If you place them in `androidApp/app/src/`, the compiler will completely ignore them.

---

## 1. Project Status Summary
* **Status**: **BUILD SUCCESSFUL** (assembleDebug runs successfully in under 2 minutes).
* **Completed Foundation**: Shared clinical logic, 74 desktop tests passing, form mappers, policy validator, FHIR serialisation proof of concept (for `SafetyPlan` and `Pisani`), local models status verified, and APK builds successfully.
* **FHIR Serialisation**: Created under `androidApp/src/main/java/com/example/medgem/fhir/FhirSerializer.kt`.
* **Last Commit Hash**: `3f1e51ee6c43744b041fc4f6aed699d5d4ad1f77` on branch `master`.

---

## 2. Compilation and Build Fixes Context
To maintain a green build, you must respect these structural configurations implemented during the previous session (detailed in [docs/build_resolution_report.md](file:///c:/Users/GASMILA/Desktop/BELFAST%20PROJECT/jp-clinical-ai/docs/build_resolution_report.md)):
1. **KAPT Compatibility via `com.android.legacy-kapt`**: ObjectBox 4.0.3 requires KAPT for annotation processing. Since standard `kotlin-kapt` is incompatible with AGP 9.0's default built-in Kotlin support, the plugin `com.android.legacy-kapt` is used.
2. **Exclusion Packaging Block**: Duplicate `NOTICE.md` and `LICENSE.md` files from third-party JARs (specifically `jakarta.annotation` and `jakarta.inject`) are excluded in `androidApp/build.gradle.kts`'s `packaging` block to prevent resource merge collisions.
3. **Resource Namespace (`R`)**: Since the project namespace is `com.belfasttrust.jpclinical.android` but source files are under `com.example.medgem`, you must import `com.belfasttrust.jpclinical.android.R` in any Compose files referencing launcher or layout resources (e.g. as in `OnboardingScreen.kt`).
4. **MyObjectBox Location**: The generated `MyObjectBox` database helper resides in package `com.belfasttrust.jpclinical.android.data.db`. You must import it explicitly when initializing ObjectBox.
5. **FHIR Cardinality**: FHIR R4 `CarePlan.author` and `RiskAssessment.performer` have `0..1` cardinality (single references) rather than collection lists, meaning they are assigned via property setters (e.g., `author = ...` and `performer = ...`), not via `addAuthor()` or `addPerformer()`.

---

## 3. Next Tasks: The Wiring Phase (Belfast Navigation & NoteInputScreen)

Your objective is to build the Belfast Trust navigation flow and the first screen (`NoteInputScreen.kt`) so that debug builds launch into the correct Belfast Trust HTT interface.

### STEP 1: Create `NoteInputScreen.kt`
* **Correct Location**: [NoteInputScreen.kt](file:///c:/Users/GASMILA/Desktop/BELFAST%20PROJECT/jp-clinical-ai/androidApp/src/main/java/com/example/medgem/ui/screens/NoteInputScreen.kt)
* **UI Requirements**:
  * **Top Bar**: Title is `"New Clinical Session"`.
  * **Patient ID Field**: Outlined text field for H&C Number. Label: `"H&C Number (optional)"`.
  * **Notes Area**: Large text field taking ~60% height. Hint: `"Type or speak your patient notes here...\nUse abbreviations as you normally would.\nTLNWL, TSH, MSE, PISANI — the system understands them."`. No character limit, scrollable.
  * **Action Buttons**: Two buttons in a horizontal row:
    * Left: Outlined, teal, microphone icon, labeled `"Speak notes"`. On click, show a Snackbar: `"Voice input coming soon — please type your notes"`.
    * Right: Filled, teal, remaining width, labeled `"PROCESS NOTES"`. Disabled when notes are blank. On click, call `onProcessNotes(hcNumber, noteText)`.
  * **Disclaimer**: Bottom-aligned 11sp muted grey centered text: `"Your notes are processed entirely on this device. Nothing is sent to any server until you approve and manually sync completed forms."`
  * **Theme**: Use colors matching other screens. No emojis or decorative elements.

### STEP 2: Create `BelfastNavGraph.kt`
* **Correct Location**: [BelfastNavGraph.kt](file:///c:/Users/GASMILA/Desktop/BELFAST%20PROJECT/jp-clinical-ai/androidApp/src/main/java/com/example/medgem/navigation/BelfastNavGraph.kt)
* **Routes (Object or Sealed Class)**:
  ```kotlin
  object BelfastRoute {
      const val NOTE_INPUT = "note_input"
      const val PROCESSING = "processing"
      const val CLARIFICATION_QUEUE = "clarification_queue"
      const val FORM_REVIEW = "form_review"
      const val SESSION_COMPLETE = "session_complete"
  }
  ```
* **NavHost Composable Flow**:
  * `NOTE_INPUT` $\rightarrow$ `NoteInputScreen` (navigates to `PROCESSING` on submit).
  * `PROCESSING` $\rightarrow$ `ProcessingScreen` (navigates to `CLARIFICATION_QUEUE` if there are extraction clarifications, else `FORM_REVIEW`).
  * `CLARIFICATION_QUEUE` $\rightarrow$ `ClarificationQueueScreen` (navigates to `FORM_REVIEW` on submit).
  * `FORM_REVIEW` $\rightarrow$ `FormReviewScreen` (navigates to `SESSION_COMPLETE` on approval).
  * `SESSION_COMPLETE` $\rightarrow$ `SessionCompleteScreen` (navigates back to `NOTE_INPUT` while popping the backstack).

### STEP 3: Create `ProcessingScreen.kt`
* **Correct Location**: [ProcessingScreen.kt](file:///c:/Users/GASMILA/Desktop/BELFAST%20PROJECT/jp-clinical-ai/androidApp/src/main/java/com/example/medgem/ui/screens/ProcessingScreen.kt)
* **UI Requirements**:
  * Centered circular progress indicator (Teal).
  * Labeled below with `"Analysing your notes..."`.
  * Three status messages cycling every 2 seconds:
    1. `"Extracting clinical data..."`
    2. `"Checking safety requirements..."`
    3. `"Preparing your forms..."`
  * Footer text: `"This usually takes 20-40 seconds"`.
  * **Simulation logic**: For now, use a `LaunchedEffect` to auto-navigate to the next screen after 3 seconds.

### STEP 4: Create `SessionCompleteScreen.kt`
* **Correct Location**: [SessionCompleteScreen.kt](file:///c:/Users/GASMILA/Desktop/BELFAST%20PROJECT/jp-clinical-ai/androidApp/src/main/java/com/example/medgem/ui/screens/SessionCompleteScreen.kt)
* **UI Requirements**:
  * Large green checkmark icon.
  * Heading: `"Session complete"`.
  * Subtext: `"All X forms reviewed and approved"`.
  * Button 1 (Filled Teal): `"SYNC TO EPIC EHR"`. Shows Snackbar: `"Preparing FHIR bundle for upload..."` on click.
  * Button 2 (Outlined Teal): `"START NEW SESSION"`. Navigates back to `NOTE_INPUT`, clearing the backstack.

### STEP 5: Wire into `MainActivity.kt`
* **Correct Location**: [MainActivity.kt](file:///c:/Users/GASMILA/Desktop/BELFAST%20PROJECT/jp-clinical-ai/androidApp/src/main/java/com/example/medgem/MainActivity.kt)
* **DefaultConfig flag (in `build.gradle.kts`)**: Add `buildConfigField("boolean", "SHOW_BELFAST_UI", "false")`
* **Debug buildType flag (in `build.gradle.kts`)**: Add `buildConfigField("boolean", "SHOW_BELFAST_UI", "true")`
* **MainActivity Code Injection**:
  ```kotlin
  if (BuildConfig.SHOW_BELFAST_UI) {
      BelfastNavGraph()
  } else {
      MedGemApp(...)
  }
  ```

---

## 4. Verification Check
Run the assembly test locally before committing:
```powershell
.\gradlew :androidApp:assembleDebug
```
Must result in **`BUILD SUCCESSFUL`** with zero compiler errors.
