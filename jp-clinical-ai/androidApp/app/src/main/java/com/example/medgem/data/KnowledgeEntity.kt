package com.example.medgem.data

import io.objectbox.annotation.Entity
import io.objectbox.annotation.HnswIndex
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.VectorDistanceType

/**
 * Entity representing a unit of knowledge (e.g., a medical article, guideline, or snippet)
 * with a vector embedding for semantic search.
 */
@Entity
data class KnowledgeEntity(
    @Id var id: Long = 0,

    @Index
    var title: String = "",

    var content: String = "",

    // Optional metadata (e.g., source, category) stored as JSON or string
    var metadata: String = "",

    // Vector embedding: 768 dimensions for Gemma
    // dimensions must be specified for HnswIndex
    @HnswIndex(dimensions = 768, distanceType = VectorDistanceType.DOT_PRODUCT)
    var embedding: FloatArray? = null,

    var createdAt: Long = System.currentTimeMillis()
)
