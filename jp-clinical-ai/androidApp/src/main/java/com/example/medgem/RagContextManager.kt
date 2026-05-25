package com.example.medgem

import android.content.Context
import android.util.Log
import com.example.medgem.data.KnowledgeEntity
import com.example.medgem.data.KnowledgeEntity_
import com.example.medgem.data.ObjectBox
import com.example.medgem.data.RagSourceReference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RagContextManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    suspend fun performSearch(
        query: String,
        maxDocs: Int,
        minScore: Float,
        maxContextTokens: Int
    ): List<Pair<KnowledgeEntity, Double>> {
        return withContext(Dispatchers.Default) {
            try {
                if (!EmbeddingModuleProvider.isLoaded()) {
                    val result = EmbeddingModuleProvider.initialize()
                    if (result.isFailure) {
                        Log.e(
                            "RagContextManager",
                            "Failed to load embedding model: ${result.exceptionOrNull()?.message}"
                        )
                        return@withContext emptyList()
                    }
                }

                MemoryLogger.logMemoryUsage(context, "RagSearch")
                val embedModel =
                    EmbeddingModuleProvider.getModel() ?: return@withContext emptyList()

                val queryVector = embedModel.encode(query, taskType = "Retrieval-query")
                val box = ObjectBox.store.boxFor(KnowledgeEntity::class.java)
                val queryObj =
                    box.query(KnowledgeEntity_.embedding.nearestNeighbors(queryVector, maxDocs))
                        .build()
                val results = queryObj.findWithScores()
                queryObj.close()

                results
                    .map { score ->
                        val original = score.get()
                        val truncatedContent =
                            embedModel.truncateToTokens(original.content, maxContextTokens)
                        Pair(original.copy(content = truncatedContent), 1.0 - score.score)
                    }
                    .filter { it.second >= minScore }
            } catch (e: Exception) {
                Log.e("RagContextManager", "Search failed", e)
                emptyList()
            } finally {
                EmbeddingModuleProvider.destroy()
            }
        }
    }

    fun buildToolResponse(
        results: List<Pair<KnowledgeEntity, Double>>
    ): String {
        if (results.isEmpty()) {
            return "<TOOL_RESPONSE>\nNo relevant documents found.\n</TOOL_RESPONSE>"
        }

        val sb = StringBuilder("<TOOL_RESPONSE>")
        for ((index, pair) in results.withIndex()) {
            val (doc, score) = pair
            val content = doc.content
            sb.append(
                "\n[Source ${index + 1}: ${doc.title}] (relevance: ${
                    String.format(
                        java.util.Locale.US,
                        "%.2f",
                        score
                    )
                })\n$content"
            )
        }
        sb.append("\n</TOOL_RESPONSE>")
        return sb.toString()
    }

    fun extractReferences(
        results: List<Pair<KnowledgeEntity, Double>>
    ): List<RagSourceReference> {
        return results.map { (entity, _) ->
            try {
                val json = JSONObject(entity.metadata)
                val chapter = json.optString("chapter", "")
                val startPage = json.optInt("start_page", 0)
                if (chapter.isNotEmpty()) {
                    val pdfPath =
                        if (chapter.endsWith(".pdf")) {
                            chapter
                        } else if (chapter.contains("/")) {
                            "$chapter.pdf"
                        } else {
                            "en_wtnd_2025/$chapter.pdf"
                        }
                    val pdfPage = if (startPage > 0) startPage - 1 else 0
                    RagSourceReference(title = entity.title, pdfFile = pdfPath, page = pdfPage)
                } else {
                    RagSourceReference(title = entity.title, pdfFile = null)
                }
            } catch (e: Exception) {
                Log.w("RagContextManager", "Failed to parse reference metadata", e)
                RagSourceReference(title = entity.title, pdfFile = null)
            }
        }
    }
}
