package com.example.medgem.data

import android.util.Log
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import org.pytorch.executorch.extension.llm.LlmTokenizer
import kotlin.math.sqrt

/**
 * Gemma Embedding Model using LiteRT CompiledModel API.
 * Uses LlmTokenizer for tokenization and supports task-specific prompts.
 */
class GemmaEmbeddingModel(
    modelPath: String,
    tokenizerPath: String,
    useGpu: Boolean = false
) : AutoCloseable {

    private var model: CompiledModel
    private var inputBuffers: List<TensorBuffer>
    private var outputBuffers: List<TensorBuffer>
    private val tokenizer: LlmTokenizer
    private val maxSeqLength: Int
    private val outputDim: Int

    companion object {
        private const val TAG = "GemmaEmbeddingModel"
        private const val DEFAULT_MAX_SEQ_LENGTH = 2048
        private const val DEFAULT_OUTPUT_DIM = 768

        val PROMPT_TEMPLATES = mapOf(
            "query" to "task: search result | query: ",
            "document" to "title: {title} | text: ",
            "BitextMining" to "task: search result | query: ",
            "Clustering" to "task: clustering | query: ",
            "Classification" to "task: classification | query: ",
            "InstructionRetrieval" to "task: code retrieval | query: ",
            "MultilabelClassification" to "task: classification | query: ",
            "PairClassification" to "task: sentence similarity | query: ",
            "Reranking" to "task: search result | query: ",
            "Retrieval" to "task: search result | query: ",
            "Retrieval-query" to "task: search result | query: ",
            "Retrieval-document" to "title: {title} | text: ",
            "STS" to "task: sentence similarity | query: ",
            "Summarization" to "task: summarization | query: "
        )
    }

    init {
        val accelerator = if (useGpu) Accelerator.GPU else Accelerator.CPU
        model = CompiledModel.create(
            modelPath,
            CompiledModel.Options(accelerator)
        )

        tokenizer = LlmTokenizer(tokenizerPath)

        // Create buffers
        inputBuffers = model.createInputBuffers()
        outputBuffers = model.createOutputBuffers()

        // Get dimensions from model - use defaults if inspection fails
        maxSeqLength = DEFAULT_MAX_SEQ_LENGTH
        outputDim = DEFAULT_OUTPUT_DIM

        Log.d(
            TAG,
            "Initialized GemmaEmbeddingModel with CompiledModel: maxSeqLength=$maxSeqLength, outputDim=$outputDim, inputBuffers=${inputBuffers.size}, outputBuffers=${outputBuffers.size}"
        )

        // Log buffer info
        inputBuffers.forEachIndexed { index, buffer ->
            Log.d(TAG, "Input buffer[$index]: $buffer")
        }
        outputBuffers.forEachIndexed { index, buffer ->
            Log.d(TAG, "Output buffer[$index]: $buffer")
        }
    }

    /**
     * Encodes text into an embedding vector.
     */
    fun encode(
        text: String,
        taskType: String? = null,
        title: String = "none",
        embeddingDim: Int? = null
    ): FloatArray {
        var fullText = text
        if (taskType != null) {
            val template = PROMPT_TEMPLATES[taskType]
                ?: throw IllegalArgumentException("Unknown task type: $taskType")
            fullText = if (template.contains("{title}")) {
                template.replace("{title}", title) + text
            } else {
                template + text
            }
        }

        // Tokenize with BOS=1, EOS=1
        val tokens = tokenizer.encode(fullText, 1, 1)

        // Prepare fixed-size inputs - pad to exactly 2048 with zeros
        val count = minOf(tokens.size, maxSeqLength)

        // Create padded input arrays of exactly 2048 elements as Long (INT64)
        val inputIds = LongArray(2048)  // Default is 0L
        val attentionMask = LongArray(2048)  // Default is 0L

        // Copy token values
        for (i in 0 until count) {
            inputIds[i] = tokens[i]  // tokens are already Long
            attentionMask[i] = 1L
        }

        synchronized(this) {
            // Write input data to buffers as Long (INT64)
            inputBuffers[0].writeLong(inputIds)
            if (inputBuffers.size >= 2) {
                inputBuffers[1].writeLong(attentionMask)
            }

            // Run model inference
            model.run(inputBuffers, outputBuffers)

            // Read output data
            val outputFloatArray = outputBuffers[0].readFloat()

            // Truncate to requested embedding dimension if specified
            var finalResult = outputFloatArray
            if (embeddingDim != null && embeddingDim > 0 && embeddingDim < outputFloatArray.size) {
                finalResult = outputFloatArray.copyOfRange(0, embeddingDim)
            }

            val normalized = normalize(finalResult)

            return normalized
        }
    }

    private fun normalize(v: FloatArray): FloatArray {
        var sum = 0f
        for (x in v) sum += x * x
        val norm = sqrt(sum)
        if (norm > 1e-9f) {
            for (i in v.indices) v[i] /= norm
        }
        return v
    }

    fun truncateToTokens(text: String, maxTokens: Int): String {
        val tokens = tokenizer.encode(text, 0, 0)
        if (tokens.size <= maxTokens) return text

        val sb = StringBuilder()
        var prevToken = 0L
        for (i in 0 until maxTokens) {
            val token = tokens[i]
            val piece = tokenizer.decode(prevToken, token)
            sb.append(piece)
            prevToken = token
        }
        return sb.toString() + "..."
    }

    override fun close() {
        synchronized(this) {
            inputBuffers.forEach { it.close() }
            outputBuffers.forEach { it.close() }
            model.close()
        }
    }
}
