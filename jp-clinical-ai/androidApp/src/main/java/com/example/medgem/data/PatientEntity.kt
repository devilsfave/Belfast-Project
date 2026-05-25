package com.example.medgem.data

import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany

/**
 * Entity representing a patient in the database.
 */
@Entity
data class PatientEntity(
    @Id var id: Long = 0,
    var name: String = "",
    var age: Int = 0,
    var gender: String = "",
    @Convert(converter = StringListConverter::class, dbType = String::class)
    var allergies: List<String> = emptyList(),
    @Convert(converter = StringListConverter::class, dbType = String::class)
    var chronicConditions: List<String> = emptyList(),
    @Convert(converter = StringListConverter::class, dbType = String::class)
    var currentMedications: List<String> = emptyList(),
    var createdAt: Long = System.currentTimeMillis()
) {
    @Backlink(to = "patient")
    lateinit var visits: ToMany<VisitEntity>
}
