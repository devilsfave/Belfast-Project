package com.example.medgem.data

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

/** Status of a medical visit/note processing */
enum class VisitStatus {
    RECORDING,
    TRANSCRIBED,
    PROCESSED
}

/**
 * Entity representing a patient visit and the associated medical notes.
 */
@Entity
data class VisitEntity(
    @Id var id: Long = 0,
    var date: Long = System.currentTimeMillis(),
    var audioFilePath: String? = null,
    var rawTranscript: String = "",
    var generatedNote: String = "",
    var statusValue: String = VisitStatus.RECORDING.name,
    var imagePathsJson: String? = null
) {
    lateinit var patient: ToOne<PatientEntity>

    // Helper to get/set images list
    var imagePaths: List<String>
        get() = ChatImageConverter.jsonArrayToPaths(imagePathsJson)
        set(value) {
            imagePathsJson = ChatImageConverter.pathsToJsonArray(value)
        }

    // Helper methods to get/set status as enum
    fun getStatus(): VisitStatus {
        return try {
            VisitStatus.valueOf(statusValue)
        } catch (e: IllegalArgumentException) {
            VisitStatus.RECORDING
        }
    }

    fun setStatus(status: VisitStatus) {
        statusValue = status.name
    }
}
