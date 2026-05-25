package com.example.medgem.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medgem.LlmInferenceService
import com.example.medgem.LlmInferenceService.LlmGenerationEvent
import com.example.medgem.MedAsrService
import com.example.medgem.data.PatientEntity
import com.example.medgem.data.PatientRepository
import com.example.medgem.data.VisitEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class VisitViewModel
@Inject
constructor(
    private val patientRepository: PatientRepository,
    private val medAsrService: MedAsrService,
    private val llmInferenceService: LlmInferenceService,
    private val userPreferencesRepository: com.example.medgem.data.UserPreferencesRepository,
    @param:ApplicationContext private val context: Context,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val TAG = "VisitViewModel"

    private val _currentVisit = MutableStateFlow<VisitEntity?>(null)
    val currentVisit: StateFlow<VisitEntity?> = _currentVisit.asStateFlow()
    private val _patient = MutableStateFlow<PatientEntity?>(null)
    val patient: StateFlow<PatientEntity?> = _patient.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    private val _isTranscribing = MutableStateFlow(false)
    val isTranscribing: StateFlow<Boolean> = _isTranscribing.asStateFlow()
    private val _isGeneratingNote = MutableStateFlow(false)
    val isGeneratingNote: StateFlow<Boolean> = _isGeneratingNote.asStateFlow()

    private val _transcriptText = MutableStateFlow("")
    val transcriptText: StateFlow<String> = _transcriptText.asStateFlow()
    private val _generatedNote = MutableStateFlow("")
    val generatedNote: StateFlow<String> = _generatedNote.asStateFlow()

    private val _imagePaths = MutableStateFlow<List<String>>(emptyList())
    val imagePaths: StateFlow<List<String>> = _imagePaths.asStateFlow()

    private var currentPhotoPath: String?
        get() = savedStateHandle["current_photo_path"]
        set(value) {
            savedStateHandle["current_photo_path"] = value
        }

    override fun onCleared() {
        super.onCleared()
        if (_isRecording.value) {
            medAsrService.stopAndRelease()
            _isRecording.value = false
        }
    }

    fun loadVisit(visitId: Long, patientId: Long) {
        viewModelScope.launch {
            val v = patientRepository.getVisit(visitId)
            val p = patientRepository.getPatient(patientId)
            _currentVisit.value = v
            _patient.value = p

            if (v != null) {
                _transcriptText.value = v.rawTranscript
                _generatedNote.value = v.generatedNote
                _imagePaths.value = v.imagePaths
            }
        }
    }

    fun startVoiceInput() {
        if (_isRecording.value) return
        // Clear existing transcript/note before starting new recording (don't delete audio file — it'll be overwritten)
        clearState()
        viewModelScope.launch {
            Log.d(TAG, "Requesting startRecording from MedAsrService")
            val started = medAsrService.startRecording()
            Log.d(TAG, "medAsrService.startRecording() returned: $started")
            withContext(Dispatchers.Main) { _isRecording.value = started }
        }
    }

    fun stopVoiceInput() {
        if (!_isRecording.value) return
        viewModelScope.launch {
            val result = medAsrService.stopRecording()

            // Save the recording to file
            saveRecordingToFile()

            withContext(Dispatchers.Main) {
                if (!result.isNullOrBlank()) {
                    _transcriptText.value = result
                    updateTranscript(result)
                }
                _isRecording.value = false
            }
        }
    }

    private suspend fun saveRecordingToFile() {
        val visitId = _currentVisit.value?.id ?: return
        val audioFile = getAudioFileForVisit(visitId)
        medAsrService.saveRecording(audioFile)
        // Update the DB so the UI knows there is an audio file
        // Must await DB write before refreshing the visit
        patientRepository.updateVisitAudioPath(visitId, audioFile.absolutePath)
        // Refresh current visit to reflect changes immediately in UI
        val v = patientRepository.getVisit(visitId)
        withContext(Dispatchers.Main) {
            _currentVisit.value = v
        }
    }

    private fun getAudioFileForVisit(visitId: Long): File {
        val visitsDir = File(context.filesDir, "visits/$visitId")
        if (!visitsDir.exists()) {
            visitsDir.mkdirs()
        }
        return File(visitsDir, "audio.wav")
    }

    private suspend fun updateAudioFilePath(path: String) {
        val visitId = _currentVisit.value?.id ?: return
        patientRepository.updateVisitAudioPath(visitId, path)
    }

    private fun getImagesDir(visitId: Long): File {
        val imagesDir = File(context.filesDir, "visits/$visitId/images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        return imagesDir
    }

    fun getTempImageUri(): Uri {
        val storageDir = File(context.cacheDir, "camera_images")
        if (!storageDir.exists()) storageDir.mkdirs()
        val file = File.createTempFile("IMG_", ".jpg", storageDir)
        currentPhotoPath = file.absolutePath
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun addImageFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val visitId = _currentVisit.value?.id ?: return@launch
                val imagesDir = getImagesDir(visitId)
                val fileName = "IMG_${System.currentTimeMillis()}.jpg"
                val targetFile = File(imagesDir, fileName)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val newPaths = _imagePaths.value + targetFile.absolutePath
                _imagePaths.value = newPaths
                patientRepository.updateVisitImagePaths(visitId, newPaths)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding image from URI", e)
            }
        }
    }

    fun addImagesFromUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val visitId = _currentVisit.value?.id ?: return@launch
                val imagesDir = getImagesDir(visitId)
                val newAddedPaths = mutableListOf<String>()

                uris.forEach { uri ->
                    val fileName =
                        "IMG_${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}.jpg"
                    val targetFile = File(imagesDir, fileName)

                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    newAddedPaths.add(targetFile.absolutePath)
                }

                val newPaths = _imagePaths.value + newAddedPaths
                _imagePaths.value = newPaths
                patientRepository.updateVisitImagePaths(visitId, newPaths)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding images from URIs", e)
            }
        }
    }

    fun saveCameraImage(visitId: Long) {
        val path = currentPhotoPath ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sourceFile = File(path)
                if (!sourceFile.exists()) return@launch

                val imagesDir = getImagesDir(visitId)
                val fileName = "IMG_${System.currentTimeMillis()}.jpg"
                val targetFile = File(imagesDir, fileName)

                sourceFile.copyTo(targetFile, overwrite = true)
                // Clean up temp file
                sourceFile.delete()

                // Fetch latest visit data to ensure we don't overwrite existing images
                // if the ViewModel hasn't finished loading yet.
                val existingVisit = patientRepository.getVisit(visitId)
                val currentPaths = existingVisit?.imagePaths ?: emptyList()

                val newPaths = currentPaths + targetFile.absolutePath

                // Update state and DB
                _imagePaths.value = newPaths
                patientRepository.updateVisitImagePaths(visitId, newPaths)

                // If _currentVisit was not loaded, update it now so the UI reflects the visit
                if (_currentVisit.value == null) {
                    _currentVisit.value = existingVisit
                    if (existingVisit != null) {
                        _transcriptText.value = existingVisit.rawTranscript
                        _generatedNote.value = existingVisit.generatedNote
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving camera image", e)
            }
        }
    }

    fun removeImage(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }

                val newPaths = _imagePaths.value.filter { it != path }
                _imagePaths.value = newPaths

                val visitId = _currentVisit.value?.id ?: return@launch
                patientRepository.updateVisitImagePaths(visitId, newPaths)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing image", e)
            }
        }
    }

    fun processAudioFile(uri: Uri) {
        // Clear existing transcript/note before processing new file (don't delete audio file — it'll be overwritten)
        clearState()
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { _isTranscribing.value = true }
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val visitId = _currentVisit.value?.id ?: return@launch
                    val targetFile = getAudioFileForVisit(visitId)

                    // Copy to internal storage
                    FileOutputStream(targetFile).use { output -> inputStream.copyTo(output) }

                    // Update DB with new path
                    updateAudioFilePath(targetFile.absolutePath)
                    // Refresh current visit to show play button immediately
                    val updatedVisit = patientRepository.getVisit(visitId)
                    withContext(Dispatchers.Main) {
                        _currentVisit.value = updatedVisit
                    }

                    // Transcribe the *internal* file
                    medAsrService
                        .transcribeFile(targetFile.absolutePath)
                        .onSuccess { text ->
                            withContext(Dispatchers.Main) {
                                _transcriptText.value = text
                                updateTranscript(text)
                            }
                        }
                        .onFailure { e -> Log.e(TAG, "Transcription failed", e) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing audio file", e)
            } finally {
                withContext(Dispatchers.Main) { _isTranscribing.value = false }
            }
        }
    }

    private fun updateTranscript(text: String) {
        val visitId = _currentVisit.value?.id ?: return
        viewModelScope.launch { patientRepository.updateVisitTranscript(visitId, text) }
    }

    fun updateTranscriptText(text: String) {
        _transcriptText.value = text
        updateTranscript(text)
    }

    /** Clears transcript and note state only (no audio file deletion). Safe to call before re-recording/re-uploading. */
    private fun clearState() {
        _transcriptText.value = ""
        _generatedNote.value = ""
        val visitId = _currentVisit.value?.id ?: return
        viewModelScope.launch {
            patientRepository.updateVisitTranscript(visitId, "")
            patientRepository.updateVisitNote(visitId, "")
        }
    }

    /** Clears everything: transcript, note, and audio file. Used by the Clear button. */
    fun clearAll() {
        _transcriptText.value = ""
        _generatedNote.value = ""
        _imagePaths.value = emptyList()
        val visitId = _currentVisit.value?.id ?: return
        viewModelScope.launch {
            // Atomically clear transcript, note, audio path, and reset status
            patientRepository.clearVisit(visitId)
            // Delete audio file from disk
            withContext(Dispatchers.IO) {
                val audioFile = getAudioFileForVisit(visitId)
                if (audioFile.exists()) audioFile.delete()
            }
            // Refresh visit so UI updates (play button hides, etc.)
            val v = patientRepository.getVisit(visitId)
            withContext(Dispatchers.Main) {
                _currentVisit.value = v
            }
        }
    }

    fun generateMedicalNote() {
        if (_transcriptText.value.isBlank()) return

        _isGeneratingNote.value = true
        _generatedNote.value = ""

        viewModelScope.launch {
            try {
                // Reset LLM context to ensure no previous conversation is retained
                llmInferenceService.reset()

                // specific max tokens from settings
                val maxTokens = userPreferencesRepository.maxSequenceLength.first()

                // specific SOAP settings
                val temperature = userPreferencesRepository.soapTemperature.first()
                val topP = userPreferencesRepository.soapTopP.first()
                val systemPrompt = userPreferencesRepository.soapSystemPrompt.first()
                val thinkingEnabled = userPreferencesRepository.soapThinkingEnabled.first()

                val patientContext = StringBuilder()
                _patient.value?.let { p ->
                    if (p.allergies.isNotEmpty()) {
                        patientContext.append("Allergies: ${p.allergies.joinToString(", ")}\n")
                    }
                    if (p.chronicConditions.isNotEmpty()) {
                        patientContext.append(
                            "Chronic Conditions: ${
                                p.chronicConditions.joinToString(
                                    ", "
                                )
                            }\n"
                        )
                    }
                    if (p.currentMedications.isNotEmpty()) {
                        patientContext.append(
                            "Current Medications: ${
                                p.currentMedications.joinToString(
                                    ", "
                                )
                            }\n"
                        )
                    }
                }

                val effectiveSystemPrompt =
                    if (thinkingEnabled) {
                        "SYSTEM INSTRUCTION: think silently if needed. $systemPrompt\n$patientContext"
                    } else {
                        "$systemPrompt\n$patientContext"
                    }

                val preImagePrompt =
                    """<start_of_turn>user
$effectiveSystemPrompt

"""
                val prompt =
                    """TRANSCRIPT:
${_transcriptText.value}<end_of_turn>
<start_of_turn>model
"""

                llmInferenceService.generateResponseFlow(
                    prompt = prompt,
                    preImagePrompt = preImagePrompt,
                    images = _imagePaths.value,
                    maxTokens = maxTokens,
                    numBos = 1,
                    temperature = temperature,
                    topP = topP
                )
                    .collect { event ->
                        when (event) {
                            is LlmGenerationEvent.Content -> {
                                _generatedNote.value += event.text
                            }

                            is LlmGenerationEvent.Done -> {
                                saveGeneratedNote(_generatedNote.value)
                                _isGeneratingNote.value = false
                            }

                            else -> {}
                        }
                    }
            } catch (e: Exception) {
                _isGeneratingNote.value = false
                Log.e(TAG, "Error generating note", e)
            }
        }
    }

    private fun saveGeneratedNote(note: String) {
        val visitId = _currentVisit.value?.id ?: return
        viewModelScope.launch { patientRepository.updateVisitNote(visitId, note) }
    }

    fun updateDate(date: Long) {
        val visitId = _currentVisit.value?.id ?: return
        viewModelScope.launch {
            patientRepository.updateVisitDate(visitId, date)
            // Refresh visit to update UI
            val v = patientRepository.getVisit(visitId)
            _currentVisit.value = v
        }
    }
}
