# Belfast Clinical AI — Handover for Next Agent

Read this before making any changes.

## Non-Negotiable First Reads

1. `C:\Users\GASMILA\Desktop\BELFAST PROJECT\AGENT_BRIEF.md`
2. `C:\Users\GASMILA\Desktop\BELFAST PROJECT\jp-clinical-ai\docs\build_resolution_report.md`
3. `C:\Users\GASMILA\Desktop\BELFAST PROJECT\jp-clinical-ai\docs\handover_to_next_agent.md`
4. `C:\Users\GASMILA\Desktop\BELFAST PROJECT\JP_REVIEW_PACKAGE.md`

Treat `JP_REVIEW_PACKAGE.md` as synthetic QA / review context only. It has useful examples, but it is not final clinical specification until JP reviews it.

## Critical Path Correction

The repo has duplicate Android source trees:

- `jp-clinical-ai/androidApp/src/main/...` is the compiled Android module path. Use this.
- `jp-clinical-ai/androidApp/app/src/main/...` is a dead/unused duplicate path. Do not add new files here.

The overseer prompt mentioned `androidApp/app/src/...`, but that is wrong for this project. All successful recent work was done under `androidApp/src/main/...`.

## Current Build / Git State

Last pushed commits on `origin/master`:

- `d8ef4bd` — `feat: Belfast navigation flow, NoteInputScreen, ProcessingScreen, SessionCompleteScreen`
- `2ac41a0` — `feat: wire Belfast MedGemma extraction pipeline`

Verified by Herbert from PowerShell:

- `.\gradlew :androidApp:compileDebugKotlin --no-daemon` — BUILD SUCCESSFUL
- `.\gradlew :androidApp:assembleDebug --no-daemon` — BUILD SUCCESSFUL

Important git note: the Git repository root is `C:\Users\GASMILA\Desktop\BELFAST PROJECT`, not only `jp-clinical-ai`. A sibling folder `../ui-ux-pro-max-skill` appears as untracked when running status inside `jp-clinical-ai`; ignore it and stage only from `jp-clinical-ai`.

## What Is Already Done

The app is now functionally wired end to end for the Belfast debug path:

- Debug builds show Belfast UI via `BuildConfig.SHOW_BELFAST_UI`.
- `NoteInputScreen` collects optional H&C number and rough notes.
- `BelfastNavGraph` routes note input -> processing -> clarification queue or form review -> session complete.
- `BelfastPipelineViewModel` calls MedGemma through `LlmInferenceService`.
- Existing `ExtractionPromptBuilder` now includes a strict final `MasterSchema` prompt.
- Output is parsed into `MasterSchema` using kotlinx serialization.
- `PolicyValidator` runs and creates hard-block/soft-flag UI issues.
- Clarification items are generated from failed rules and low confidence fields.
- All form review pages are generated from existing pure mapper outputs.
- Safety Plan form still requires signature in review.
- `FormReviewScreen` final sync handoff is disabled unless all forms are approved and hard blocks are clear.
- `LlmModuleProvider` defaults to `BuildConfig.ENABLE_VISION_ENCODER` so Belfast text inference does not accidentally load vision.

## Key Files To Inspect Before Next Work

### Belfast flow

- `androidApp/src/main/java/com/example/medgem/MainActivity.kt`
- `androidApp/src/main/java/com/example/medgem/navigation/BelfastNavGraph.kt`
- `androidApp/src/main/java/com/example/medgem/ui/screens/NoteInputScreen.kt`
- `androidApp/src/main/java/com/example/medgem/ui/screens/ProcessingScreen.kt`
- `androidApp/src/main/java/com/example/medgem/ui/screens/ClarificationQueueScreen.kt`
- `androidApp/src/main/java/com/example/medgem/ui/screens/FormReviewScreen.kt`
- `androidApp/src/main/java/com/example/medgem/ui/screens/SessionCompleteScreen.kt`
- `androidApp/src/main/java/com/example/medgem/ui/viewmodel/BelfastPipelineViewModel.kt`

### Model paths and download flow

- `androidApp/src/main/java/com/example/medgem/ModelConfig.kt`
- `androidApp/src/main/java/com/example/medgem/MedGemApplication.kt`
- `androidApp/src/main/java/com/example/medgem/LlmModuleProvider.kt`
- `androidApp/src/main/java/com/example/medgem/EmbeddingModuleProvider.kt`
- `androidApp/src/main/java/com/example/medgem/MedAsrProvider.kt`
- `androidApp/src/main/java/com/example/medgem/ui/screens/ModelDownloadScreen.kt`
- `androidApp/src/main/java/com/example/medgem/ui/screens/ModelDownloadViewModel.kt`

### Shared clinical logic

- `shared/src/commonMain/kotlin/com/belfasttrust/jpclinical/schema/MasterSchema.kt`
- `shared/src/commonMain/kotlin/com/belfasttrust/jpclinical/domain/extraction/ExtractionPromptBuilder.kt`
- `shared/src/commonMain/kotlin/com/belfasttrust/jpclinical/domain/validator/PolicyValidator.kt`
- `shared/src/commonMain/kotlin/com/belfasttrust/jpclinical/domain/mappers/SafetyPlanMapper.kt`
- `shared/src/commonMain/kotlin/com/belfasttrust/jpclinical/domain/mappers/PisaniMapper.kt`
- `shared/src/commonMain/kotlin/com/belfasttrust/jpclinical/domain/mappers/`
- `shared/src/commonMain/kotlin/com/belfasttrust/jpclinical/domain/testdata/SyntheticNoteFactory.kt`

### Build files

- `androidApp/build.gradle.kts`
- `gradle/libs.versions.toml`
- `gradle.properties`

## Next Task From Overseer

### Task 1: Clean Dead Code From Wrong Path

Confirm and delete only the requested dead files under `androidApp/app/src/...` if present. Do not delete anything under `androidApp/src/...`.

Observed before this handover:

- `androidApp/app/src/main/java/com/example/medgem/ui/screens/ClarificationQueueScreen.kt` exists.
- `androidApp/app/src/main/java/com/example/medgem/ui/screens/FormReviewScreen.kt` exists.
- `androidApp/app/src/main/java/com/example/medgem/ui/screens/NoteInputScreen.kt` does not exist.

Recommended action:

```powershell
Remove-Item -LiteralPath "androidApp\app\src\main\java\com\example\medgem\ui\screens\ClarificationQueueScreen.kt"
Remove-Item -LiteralPath "androidApp\app\src\main\java\com\example\medgem\ui\screens\FormReviewScreen.kt"
```

If using tools with safety rules, resolve the paths first and verify they are inside `jp-clinical-ai\androidApp\app\src\...` before deleting.

### Task 2: Adapt Model Download Screen For Belfast Models

Read `ModelDownloadScreen.kt` and `ModelDownloadViewModel.kt` fully first. The existing MedGEM flow already handles model downloads; adapt it rather than replacing it.

Required Belfast models:

- MedGemma 1.5 4B ExecuTorch: `kamalkraj/medgemma-1.5-4b-it-executorch`, about 2.3GB
- MedASR ONNX int8: `kamalkraj/medasr-onnx`, about 121MB
- EmbeddingGemma 300M LiteRT int8: `kamalkraj/embeddinggemma-300m-litert`, about 300MB

UI copy requested:

```text
These AI models run entirely on your device.
Your notes never leave your phone.
Download once — works offline forever.
```

Also show:

- Individual progress bars for all three models.
- Total download size: approximately 2.7GB.
- Warning: `Connect to WiFi before downloading.`

Add a Belfast startup gate in `BelfastNavGraph`:

- If required model files are missing, navigate to `ModelDownloadScreen` before `NoteInputScreen`.
- When downloads complete, navigate to `NoteInputScreen`.

Suggested conservative approach:

- Create a small model readiness helper or ViewModel method that checks the exact local files named by `ModelConfig`.
- Avoid changing inference engine internals.
- Keep existing MedGEM download behavior intact for non-Belfast routes if possible.
- Do not introduce cloud AI calls. Downloads are only for model files.

### Task 3: Tests

Run:

```powershell
.\gradlew :shared:desktopTest --no-daemon
```

Expected historical result: 74 tests passing.

### Task 4: Build

Run:

```powershell
.\gradlew :androidApp:assembleDebug --no-daemon
```

Must be BUILD SUCCESSFUL before committing.

### Task 5: Commit

Requested commit message:

```text
fix: remove dead code at wrong path, adapt model download screen for Belfast models
```

Push to:

```powershell
git push origin master
```

## Practical Lessons Learned

- The local sandbox may fail Gradle Android builds because it cannot access `C:\Users\GASMILA\AppData\Local\Android\Sdk\platforms\android-36-ext19\package.xml`. Herbert can run Gradle successfully from normal PowerShell. If sandbox build fails on SDK access, ask Herbert to run the command and paste output.
- `git add .` from inside `jp-clinical-ai` stages only project files even though the actual repo root is the parent folder.
- Gradle may create a `.kotlin/` cache directory during failed/sandboxed builds. Remove generated cache files before staging.
- The app can compile and assemble without model files. Runtime inference testing still needs model files on an emulator/device.
- Physical-device testing with 8GB+ RAM remains the critical path for proving real MedGemma extraction.

## Report Template For Next Agent

```text
PHASE: Cleanup and Belfast model download flow
STATUS: COMPLETE / IN PROGRESS / BLOCKED
DEAD FILES: [found/not found, deleted/not applicable]
MODEL DOWNLOAD: [adapted or blocked - describe]
SHARED TESTS: [74 passing or actual result]
BUILD: [SUCCESSFUL or FAILED]
COMMIT HASH: [hash if committed]
ISSUES: [technical blockers or clinical questions]
NEXT: [physical device/emulator test, model files, Epic sync, JP corrections]
```

