package com.example.medgem.data

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

/**
 * Entity representing a single message in a conversation.
 * Messages belong to a conversation and can contain text and/or images.
 * Images are stored as a JSON array of file paths.
 */
@Entity
data class MessageEntity(
    @Id var id: Long = 0,
    var content: String = "",
    var isUser: Boolean = false,
    var imagePathsJson: String? = null, // JSON array of file paths
    var referencesJson: String? = null, // JSON array of serialized RagReference
    var createdAt: Long = System.currentTimeMillis()
) {
    lateinit var conversation: ToOne<ConversationEntity>

    // Convenience property for conversationId
    val conversationId: Long
        get() = conversation.targetId
}
