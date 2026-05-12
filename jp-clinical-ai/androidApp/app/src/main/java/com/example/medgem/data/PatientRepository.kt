package com.example.medgem.data

import io.objectbox.Box
import io.objectbox.kotlin.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepository @Inject constructor(
    private val patientBox: Box<PatientEntity>,
    private val visitBox: Box<VisitEntity>
) {
    // ==================== Patient Operations ====================

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllPatients(): Flow<List<PatientEntity>> {
        return patientBox
            .query()
            .orderDesc(PatientEntity_.createdAt)
            .build()
            .flow()
            .map { it.toList() }
            .flowOn(Dispatchers.IO)
    }

    suspend fun getPatient(id: Long): PatientEntity? {
        return withContext(Dispatchers.IO) {
            patientBox.get(id)
        }
    }

    suspend fun createPatient(
        name: String,
        age: Int,
        gender: String,
        allergies: List<String> = emptyList(),
        chronicConditions: List<String> = emptyList(),
        currentMedications: List<String> = emptyList()
    ): Long {
        return withContext(Dispatchers.IO) {
            val patient =
                PatientEntity(
                    name = name,
                    age = age,
                    gender = gender,
                    allergies = allergies,
                    chronicConditions = chronicConditions,
                    currentMedications = currentMedications
                )
            patientBox.put(patient)
        }
    }

    suspend fun updatePatient(patient: PatientEntity) {
        withContext(Dispatchers.IO) { patientBox.put(patient) }
    }

    suspend fun deletePatient(patientId: Long) {
        withContext(Dispatchers.IO) {
            val visits = visitBox.query().equal(VisitEntity_.patientId, patientId).build().find()
            // Clean up audio and images from disk before removing DB records
            for (visit in visits) {
                visit.audioFilePath?.let { path ->
                    if (path.isNotEmpty()) File(path).delete()
                }
                visit.imagePaths.forEach { path ->
                    File(path).delete()
                }
            }
            visitBox.remove(visits)
            patientBox.remove(patientId)
        }
    }

    // ==================== Visit Operations ====================

    suspend fun getVisit(id: Long): VisitEntity? {
        return withContext(Dispatchers.IO) {
            visitBox.get(id)
        }
    }

    suspend fun createVisit(patientId: Long): Long {
        return withContext(Dispatchers.IO) {
            val visit = VisitEntity()
            try {
                visit.patient.targetId = patientId
            } catch (e: UninitializedPropertyAccessException) {
                visit.patient = io.objectbox.relation.ToOne(visit, VisitEntity_.patient)
                visit.patient.targetId = patientId
            }
            visitBox.put(visit)
        }
    }

    suspend fun deleteVisit(visitId: Long) {
        withContext(Dispatchers.IO) {
            val visit = visitBox.get(visitId)
            // Clean up audio file from disk before removing DB record
            visit?.audioFilePath?.let { path ->
                if (path.isNotEmpty()) File(path).delete()
            }
            // Clean up images
            visit?.imagePaths?.forEach { path ->
                File(path).delete()
            }
            visitBox.remove(visitId)
        }
    }

    suspend fun updateVisitTranscript(visitId: Long, transcript: String) {
        withContext(Dispatchers.IO) {
            val visit = visitBox.get(visitId)
            if (visit != null) {
                visit.rawTranscript = transcript
                if (transcript.isNotBlank()) {
                    visit.setStatus(VisitStatus.TRANSCRIBED)
                } else if (visit.generatedNote.isBlank()) {
                    visit.setStatus(VisitStatus.RECORDING)
                }
                visitBox.put(visit)
            }
        }
    }

    suspend fun updateVisitNote(visitId: Long, note: String) {
        withContext(Dispatchers.IO) {
            val visit = visitBox.get(visitId)
            if (visit != null) {
                visit.generatedNote = note
                if (note.isEmpty()) {
                    // Revert status if note is cleared
                    visit.setStatus(if (visit.rawTranscript.isNotEmpty()) VisitStatus.TRANSCRIBED else VisitStatus.RECORDING)
                } else {
                    visit.setStatus(VisitStatus.PROCESSED)
                }
                visitBox.put(visit)
            }
        }
    }

    suspend fun updateVisitAudioPath(visitId: Long, path: String) {
        withContext(Dispatchers.IO) {
            val visit = visitBox.get(visitId)
            if (visit != null) {
                visit.audioFilePath = path.ifEmpty { null }
                visitBox.put(visit)
            }
        }
    }

    suspend fun updateVisitImagePaths(visitId: Long, paths: List<String>) {
        withContext(Dispatchers.IO) {
            val visit = visitBox.get(visitId)
            if (visit != null) {
                visit.imagePaths = paths
                visitBox.put(visit)
            }
        }
    }

    suspend fun updateVisitDate(visitId: Long, date: Long) {
        withContext(Dispatchers.IO) {
            val visit = visitBox.get(visitId)
            if (visit != null) {
                visit.date = date
                visitBox.put(visit)
            }
        }
    }

    /** Atomically clears transcript, note, audio path, and images for a visit. */
    suspend fun clearVisit(visitId: Long) {
        withContext(Dispatchers.IO) {
            val visit = visitBox.get(visitId)
            if (visit != null) {
                // Delete image files
                visit.imagePaths.forEach { path ->
                    File(path).delete()
                }
                visit.imagePaths = emptyList()

                visit.rawTranscript = ""
                visit.generatedNote = ""
                visit.audioFilePath = null
                visit.setStatus(VisitStatus.RECORDING)
                visitBox.put(visit)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPatientVisits(patientId: Long): Flow<List<VisitEntity>> {
        return visitBox
            .query()
            .equal(VisitEntity_.patientId, patientId)
            .orderDesc(VisitEntity_.date)
            .build()
            .flow()
            .map { it.toList() }
            .flowOn(Dispatchers.IO)
    }
}
