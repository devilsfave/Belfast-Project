package com.example.medgem.data

import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import io.objectbox.relation.ToOne

/** Types of conversations */
enum class ConversationType {
    CHAT, // Regular Gemma chat
    RAG // RAG-augmented chat
}

/**
 * Entity representing a conversation in the database. Each conversation can contain multiple
 * messages. Supports both regular chat and RAG conversations.
 */
@Entity
data class ConversationEntity(
    @Id var id: Long = 0,
    var title: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var previewImagePath: String? = null,
    var conversationType: String = ConversationType.CHAT.name,
    var contextJson: String? = null
) {
    @Backlink(to = "conversation")
    lateinit var messages: ToMany<MessageEntity>

    lateinit var patient: ToOne<PatientEntity>

    // Helper methods to get/set type as enum
    fun getType(): ConversationType {
        return try {
            ConversationType.valueOf(conversationType)
        } catch (e: IllegalArgumentException) {
            ConversationType.CHAT
        }
    }

    fun setType(type: ConversationType) {
        conversationType = type.name
    }
}
