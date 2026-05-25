# Diagnostic and Resolution Report: `:androidApp` Build Fixes

This report outlines the errors encountered during the compilation and packaging of the `:androidApp` module and documents how each was successfully resolved.

---

## 1. Built-in Kotlin Support vs. KAPT Compatibility
### The Error
```text
An exception occurred applying plugin request [id: 'org.jetbrains.kotlin.kapt']
> Failed to apply plugin 'org.jetbrains.kotlin.kapt'.
   > The 'org.jetbrains.kotlin.kapt' plugin is not compatible with built-in Kotlin support.
```
When attempting to bypass this by setting `android.builtInKotlin=false` in `gradle.properties`, the compiler subsequently failed to resolve the `kotlin` configuration block:
```text
build.gradle.kts:130:1: None of the following candidates is applicable: ...
Unresolved reference 'compilerOptions'
```

### Root Cause
1. Android Gradle Plugin (AGP) 9.0+ enables **built-in Kotlin support** by default, rendering the standard `kotlin-kapt` plugin incompatible.
2. ObjectBox 4.0.3 does not yet natively support Kotlin Symbol Processing (KSP) and requires KAPT code generation to compile its database entities (`PatientEntity_`, `VisitEntity_`, etc.).
3. Bypassing built-in Kotlin support (`builtInKotlin=false`) disabled automatic Kotlin injection, which broke the `kotlin { compilerOptions { ... } }` DSL block since the standalone Kotlin plugin wasn't applied manually.

### The Solution
*   Removed the bypass property `android.builtInKotlin=false` from `gradle.properties` to keep AGP 9.0's built-in Kotlin support active.
*   Declared and applied the new **`com.android.legacy-kapt`** plugin in [libs.versions.toml](file:///c:/Users/GASMILA/Desktop/BELFAST%20PROJECT/jp-clinical-ai/gradle/libs.versions.toml) and applied it as `alias(libs.plugins.legacy.kapt)` in [build.gradle.kts](file:///c:/Users/GASMILA/Desktop/BELFAST%20PROJECT/jp-clinical-ai/androidApp/build.gradle.kts) instead of `kotlin("kapt")`. This enabled compatibility with built-in Kotlin support while still running KAPT code generation for ObjectBox.

---

## 2. Unresolved Android Resources (`R`)
### The Error
```text
OnboardingScreen.kt:73:52 Unresolved reference 'R'.
```

### Root Cause
The build configuration namespace in `build.gradle.kts` is set to `com.belfasttrust.jpclinical.android`. However, the source code package structure is located in `com.example.medgem`. When compiling `OnboardingScreen.kt` (which resides in `com.example.medgem`), the generated resource mappings (`R`) were created under the namespace package (`com.belfasttrust.jpclinical.android.R`) and were not visible within `com.example.medgem` without an explicit import.

### The Solution
Added the import statement to [OnboardingScreen.kt](file:///c:/Users/GASMILA/Desktop/BELFAST%20PROJECT/jp-clinical-ai/androidApp/src/main/java/com/example/medgem/OnboardingScreen.kt):
```kotlin
import com.belfasttrust.jpclinical.android.R
```

---

## 3. Unresolved ObjectBox Helper (`MyObjectBox`)
### The Error
```text
ObjectBox.kt:32:27 Unresolved reference 'MyObjectBox'.
```

### Root Cause
ObjectBox compiles and generates the Java entry point helper `MyObjectBox.java` under the namespace package `com.belfasttrust.jpclinical.android.data.db`. The Kotlin initialization class `ObjectBox.kt` resides in `com.example.medgem.data`. Since `MyObjectBox` was generated outside the `com.example.medgem` hierarchy, it could not be resolved without an import.

### The Solution
Added the import statement to [ObjectBox.kt](file:///c:/Users/GASMILA/Desktop/BELFAST%20PROJECT/jp-clinical-ai/androidApp/src/main/java/com/example/medgem/data/ObjectBox.kt):
```kotlin
import com.belfasttrust.jpclinical.android.data.db.MyObjectBox
```

---

## 4. HAPI FHIR Cardinality Unresolved References
### The Errors
```text
FhirSerializer.kt:51:13 Unresolved reference 'addAuthor'.
FhirSerializer.kt:170:13 Unresolved reference 'addPerformer'.
```

### Root Cause
In the FHIR R4 schema:
*   `CarePlan.author` has a cardinality of `0..1` (representing a single responsible party).
*   `RiskAssessment.performer` has a cardinality of `0..1` (representing a single assessor).

Because these properties are single references rather than collections, the HAPI FHIR Java library does not generate `addAuthor()` or `addPerformer()` list helper methods. The code was incorrectly attempting to append references to a collection instead of using single-value assignment.

### The Solution
Updated [FhirSerializer.kt](file:///c:/Users/GASMILA/Desktop/BELFAST%20PROJECT/jp-clinical-ai/androidApp/src/main/java/com/example/medgem/fhir/FhirSerializer.kt) to use standard Kotlin property setters:
```kotlin
// CarePlan Author
author = Reference().apply {
    reference = "urn:uuid:$nurseId"
    display = plan.assessorName
}

// RiskAssessment Performer
performer = Reference().apply {
    reference = "urn:uuid:$nurseId"
    display = assessment.assessorName
}
```

---

## 5. Duplicate Resource Packaging Conflict
### The Error
```text
Execution failed for task ':androidApp:mergeDebugJavaResource'.
> A failure occurred while executing com.android.build.gradle.internal.tasks.MergeJavaResWorkAction
   > 2 files found with path 'META-INF/NOTICE.md' from inputs:
      - jakarta.annotation:jakarta.annotation-api:2.1.1/jakarta.annotation-api-2.1.1.jar
      - jakarta.inject:jakarta.inject-api:2.0.1/jakarta.inject-api-2.0.1.jar
```

### Root Cause
Both `jakarta.annotation-api` and `jakarta.inject-api` libraries bundle identical license and notice metadata files (`META-INF/NOTICE.md`) in their JAR packages. During final APK compilation, the Gradle resource merger tool fails when it encounters duplicate non-class resources with the same path, unless instructed on how to handle them.

### The Solution
Added a `packaging` block inside the `android` block of [build.gradle.kts (androidApp)](file:///c:/Users/GASMILA/Desktop/BELFAST%20PROJECT/jp-clinical-ai/androidApp/build.gradle.kts) to instruct the compiler to ignore duplicates of standard license/notice files:
```kotlin
    packaging {
        resources {
            excludes.add("/META-INF/AL2.0")
            excludes.add("/META-INF/LGPL2.1")
            excludes.add("/META-INF/LICENSE.md")
            excludes.add("/META-INF/NOTICE.md")
            excludes.add("/META-INF/LICENSE.txt")
            excludes.add("/META-INF/NOTICE.txt")
            excludes.add("/META-INF/LICENSE")
            excludes.add("/META-INF/NOTICE")
        }
    }
```

---

## Conclusion
Following these modifications, the build task was successfully run:
```powershell
.\gradlew :androidApp:assembleDebug
```
**Result:** **`BUILD SUCCESSFUL in 1m 59s`** with zero remaining compiler errors.

---

## 6. Duplicate Android Source Tree / Wrong Path Risk
### The Error Risk
The project contains two Android source trees:
```text
androidApp/src/main/...      # compiled module path
androidApp/app/src/main/...  # unused duplicate path
```
Several prompts and generated files referenced `androidApp/app/src/main/...`, which would be ignored by Gradle.

### Root Cause
The MedGEM bootstrap left a legacy nested `app` structure inside `androidApp`. The active Gradle module evaluates sources from `androidApp/src/main/...`, not the nested `androidApp/app/src/main/...` directory.

### The Solution
All working Belfast UI, navigation, and pipeline wiring was implemented under the compiled path:
```text
androidApp/src/main/java/com/example/medgem/...
```
Debug APK builds then succeeded. Future agents must not add Kotlin files under `androidApp/app/src/main/...`; that tree should be treated as dead code unless explicitly cleaning it up.
