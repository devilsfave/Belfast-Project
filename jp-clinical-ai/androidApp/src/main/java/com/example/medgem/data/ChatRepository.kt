package com.example.medgem.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.objectbox.Box
import io.objectbox.kotlin.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

/** Repository for chat data operations. Provides a clean API for database operations. */
class ChatRepository
@Inject
constructor(
    @param:ApplicationContext private val context: Context,
    private val conversationBox: Box<ConversationEntity>,
    private val messageBox: Box<MessageEntity>
) {

    // ==================== Conversation Operations ====================

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllConversations(): Flow<List<ConversationEntity>> {
        return conversationBox
            .query()
            .orderDesc(ConversationEntity_.updatedAt)
            .build()
            .flow()
            .map { it.toList() }
            .flowOn(Dispatchers.IO)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getGlobalConversations(): Flow<List<ConversationEntity>> {
        return conversationBox
            .query()
            .equal(ConversationEntity_.patientId, 0L)
            .orderDesc(ConversationEntity_.updatedAt)
            .build()
            .flow()
            .map { it.toList() }
            .flowOn(Dispatchers.IO)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getChatConversations(): Flow<List<ConversationEntity>> {
        return getGlobalConversations().map { list ->
            list.filter {
                it.conversationType == ConversationType.CHAT.name
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getRagConversations(): Flow<List<ConversationEntity>> {
        return getGlobalConversations().map { list ->
            list.filter { it.conversationType == ConversationType.RAG.name }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPatientConversations(patientId: Long): Flow<List<ConversationEntity>> {
        return conversationBox
            .query()
            .equal(ConversationEntity_.patientId, patientId)
            .orderDesc(ConversationEntity_.updatedAt)
            .build()
            .flow()
            .map { it.toList() }
            .flowOn(Dispatchers.IO)
    }

    suspend fun createConversation(
        title: String = "New Chat",
        type: ConversationType = ConversationType.CHAT,
        patientId: Long = 0L,
        contextItems: List<ContextItem> = emptyList()
    ): Long {
        return withContext(Dispatchers.IO) {
            val contextJson =
                if (contextItems.isNotEmpty()) ContextItem.toJson(contextItems) else null
            val conversation =
                ConversationEntity(
                    title = title,
                    conversationType = type.name,
                    contextJson = contextJson
                )
            if (patientId != 0L) {
                try {
                    conversation.patient.targetId = patientId
                } catch (e: UninitializedPropertyAccessException) {
                    conversation.patient =
                        io.objectbox.relation.ToOne(conversation, ConversationEntity_.patient)
                    conversation.patient.targetId = patientId
                }
            }
            val id = conversationBox.put(conversation)
            Log.d(
                "ChatRepository",
                "Created new conversation with ID: $id, title: $title, type: ${type.name}, patientId: $patientId"
            )
            id
        }
    }

    suspend fun deleteConversation(id: Long) {
        withContext(Dispatchers.IO) {
            val conversation = conversationBox.get(id) ?: return@withContext
            // Clean up context images
            deleteContextImages(conversation.contextJson)
            // Delete all messages for this conversation
            messageBox.query().equal(MessageEntity_.conversationId, id).build().use { query ->
                messageBox.remove(query.find())
            }
            conversationBox.remove(id)
            Log.d("ChatRepository", "Deleted conversation ID: $id")
        }
    }

    suspend fun deleteConversationsForPatient(patientId: Long) {
        withContext(Dispatchers.IO) {
            val conversations =
                conversationBox
                    .query()
                    .equal(ConversationEntity_.patientId, patientId)
                    .build()
                    .use { query -> query.find() }
            for (conversation in conversations) {
                deleteContextImages(conversation.contextJson)
                messageBox
                    .query()
                    .equal(MessageEntity_.conversationId, conversation.id)
                    .build()
                    .use { query -> messageBox.remove(query.find()) }
            }
            conversationBox.remove(conversations)
            Log.d(
                "ChatRepository",
                "Deleted ${conversations.size} conversations for patient $patientId"
            )
        }
    }

    suspend fun updateConversationTitle(conversationId: Long, newTitle: String) {
        withContext(Dispatchers.IO) {
            val conversation = conversationBox.get(conversationId)
            if (conversation != null) {
                conversation.title = newTitle
                conversationBox.put(conversation)
                Log.d(
                    "ChatRepository",
                    "Updated title for conversation ID: $conversationId to '$newTitle'"
                )
            }
        }
    }

    suspend fun getConversation(conversationId: Long): ConversationEntity? {
        return withContext(Dispatchers.IO) {
            conversationBox.get(conversationId)
        }
    }

    // ==================== Message Operations ====================

    suspend fun getMessages(conversationId: Long): List<MessageEntity> {
        return withContext(Dispatchers.IO) {
            messageBox
                .query()
                .equal(MessageEntity_.conversationId, conversationId)
                .orderDesc(MessageEntity_.createdAt)
                .build()
                .use { query -> query.find().reversed() }
        }
    }

    suspend fun saveMessage(
        conversationId: Long,
        content: String,
        isUser: Boolean,
        images: List<Bitmap> = emptyList(),
        imagePaths: List<String> = emptyList(),
        referencesJson: String? = null
    ): Long {
        val finalPaths = imagePaths.toMutableList()

        withContext(Dispatchers.IO) {
            val directory = File(context.filesDir, "chat_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            // Save bitmaps to storage if provided
            images.forEach { bitmap ->
                val filename = "img_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
                val file = File(directory, filename)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                finalPaths.add(file.absolutePath)
            }

            // Move/Copy staged images to permanent storage
            val processedPaths = mutableListOf<String>()
            finalPaths.forEach { path ->
                val file = File(path)
                // If file is in cache/staging (or just not in our target dir), copy it to permanent storage
                if (file.exists() && !file.parentFile!!.absolutePath.equals(directory.absolutePath)) {
                    try {
                        val filename = "img_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
                        val targetFile = File(directory, filename)
                        file.copyTo(targetFile, overwrite = true)
                        processedPaths.add(targetFile.absolutePath)
                        // Optionally delete the source if it was in cache/staging
                        if (file.absolutePath.contains(context.cacheDir.absolutePath)) {
                            file.delete()
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "ChatRepository",
                            "Failed to copy image to permanent storage: $path",
                            e
                        )
                        // Fallback to original path if copy fails
                        processedPaths.add(path)
                    }
                } else {
                    processedPaths.add(path)
                }
            }
            finalPaths.clear()
            finalPaths.addAll(processedPaths)
        }

        // Store paths as JSON in the imagePathsJson field
        val imagesJson =
            if (finalPaths.isNotEmpty()) {
                ChatImageConverter.pathsToJsonArray(finalPaths)
            } else {
                null
            }

        // For preview, we use the first image path if available
        val previewPath = if (isUser && finalPaths.isNotEmpty()) finalPaths.first() else null

        return withContext(Dispatchers.IO) {
            val message =
                MessageEntity(
                    content = content,
                    isUser = isUser,
                    imagePathsJson = imagesJson,
                    referencesJson = referencesJson
                )
            try {
                message.conversation.targetId = conversationId
            } catch (e: UninitializedPropertyAccessException) {
                message.conversation =
                    io.objectbox.relation.ToOne(message, MessageEntity_.conversation)
                message.conversation.targetId = conversationId
            }

            val messageId = messageBox.put(message)
            Log.d(
                "ChatRepository",
                "Saved message ID: $messageId to conversation: $conversationId (isUser: $isUser)"
            )

            // Update conversation timestamp
            val conversation = conversationBox.get(conversationId)
            if (conversation != null) {
                conversation.updatedAt = System.currentTimeMillis()

                // Update preview image path for user messages with images
                if (previewPath != null) {
                    conversation.previewImagePath = previewPath
                }

                // Update title from first user message
                val messageCount =
                    messageBox
                        .query()
                        .equal(MessageEntity_.conversationId, conversationId)
                        .build()
                        .use { query -> query.count() }
                if (isUser && messageCount == 1L) {
                    conversation.title = content.take(50).ifBlank { "New Chat" }
                }

                conversationBox.put(conversation)
            }

            messageId
        }
    }

    private fun deleteContextImages(contextJson: String?) {
        if (contextJson.isNullOrBlank()) return
        val items = ContextItem.fromJson(contextJson)
        for (item in items) {
            if (item is ContextItem.Image) {
                try {
                    val file = File(item.path)
                    if (file.exists()) file.delete()
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Failed to delete context image: ${item.path}", e)
                }
            }
        }
    }
}
