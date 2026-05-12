package com.example.medgem

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.medgem.data.ContextItem
import com.example.medgem.utils.ImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.pytorch.executorch.extension.llm.LlmCallback
import org.pytorch.executorch.extension.llm.LlmModule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmInferenceService @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    // Track conversation state internally if needed, or rely on LlmModule state (which is stateful)

    suspend fun getModule(): LlmModule? {
        if (!LlmModuleProvider.isLoaded()) {
            val result = LlmModuleProvider.initialize()
            if (result.isFailure) return null
        }
        return LlmModuleProvider.getModule()
    }

    sealed interface LlmGenerationEvent {
        data class Content(val text: String) : LlmGenerationEvent
        data class Stats(val generatedTokens: Int, val promptTokens: Int) : LlmGenerationEvent
        data object Done : LlmGenerationEvent
    }

    fun generateResponseFlow(
        prompt: String,
        preImagePrompt: String = "",
        images: List<String>,
        maxTokens: Int,
        numBos: Int,
        temperature: Float = 0.8f,
        topP: Float = 0.9f
    ): kotlinx.coroutines.flow.Flow<LlmGenerationEvent> = kotlinx.coroutines.flow.callbackFlow {
        generateResponse(
            prompt = prompt,
            preImagePrompt = preImagePrompt,
            images = images,
            maxTokens = maxTokens,
            temperature = temperature,
            topP = topP,
            numBos = numBos,
            onPartialResult = { trySend(LlmGenerationEvent.Content(it)) },
            onStats = { generated, prompt -> trySend(LlmGenerationEvent.Stats(generated, prompt)) }
        )
        trySend(LlmGenerationEvent.Done)
        close()
    }

    suspend fun generateResponse(
        prompt: String,
        preImagePrompt: String = "",
        images: List<String>, // Paths or URIs
        maxTokens: Int,
        temperature: Float,
        topP: Float,
        numBos: Int, // 1 for start of conversation, 0 otherwise
        onPartialResult: (String) -> Unit,
        onStats: (generated: Int, prompt: Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        val module = getModule() ?: throw IllegalStateException("Model not initialized")

        // 0. Prefill Pre-Image Prompt
        if (preImagePrompt.isNotEmpty()) {
            // Use numBos here if it's the start
            module.prefillPrompt(preImagePrompt, numBos, 0)
        }

        // 1. Prefill Images
        if (images.isNotEmpty()) {
            val imageBitmaps = loadImages(images)
            val ordinals = listOf("First", "Second", "Third", "Fourth")

            imageBitmaps.forEachIndexed { index, bitmap ->
                val label = "${ordinals.getOrElse(index) { "Image ${index + 1}" }} image: "
                module.prefillPrompt(label, 0, 0)

                val floats = ImageUtils.prepareImage(bitmap)
                module.prefillImages(
                    floats,
                    ModelConfig.Llm.IMAGE_SIZE,
                    ModelConfig.Llm.IMAGE_SIZE,
                    ModelConfig.Llm.IMAGE_CHANNELS
                )

                // Explicitly recycle since these were loaded specifically for inference
                bitmap.recycle()

                // Add newline after each image
                module.prefillPrompt("\n", 0, 0)
            }
        }

        // 2. Encode Prompt
        // If we prefilled something (preImagePrompt or images), numBos for this prompt should be 0.
        // If we didn't prefill anything, then this prompt gets the original numBos.
        val promptNumBos = if (preImagePrompt.isNotEmpty() || images.isNotEmpty()) 0 else numBos

        // 3. Generate
        val callback = object : LlmCallback {
            override fun onResult(result: String) {
                if (result == "<end_of_turn>") {
                    module.stop()
                    return
                }
                onPartialResult(result)
            }

            override fun onStats(stats: String) {
                try {
                    val json = JSONObject(stats)
                    val generatedTokens = json.optInt("generated_tokens", 0)
                    val promptTokensFromStats = json.optInt("prompt_tokens", 0)

                    onStats(generatedTokens, promptTokensFromStats)
                } catch (e: Exception) {
                    Log.e("LlmInferenceService", "Failed to parse stats", e)
                }
            }
        }

        module.generate(
            prompt,
            ModelConfig.Llm.MAX_SEQ_LEN, // seqLen
            maxTokens,   // maxNewTokens
            callback,
            false,  // echo
            temperature,
            topP,
            promptNumBos,
            0 // numEos
        )
    }

    suspend fun prefillPrompt(prompt: String, numBos: Int) = withContext(Dispatchers.IO) {
        val module = getModule() ?: throw IllegalStateException("Model not initialized")
        module.prefillPrompt(prompt, numBos, 0)
    }

    suspend fun prefillImages(images: List<String>) = withContext(Dispatchers.IO) {
        val module = getModule() ?: throw IllegalStateException("Model not initialized")
        val imageBitmaps = loadImages(images)
        val ordinals = listOf("First", "Second", "Third", "Fourth")

        imageBitmaps.forEachIndexed { index, bitmap ->
            val label = "${ordinals.getOrElse(index) { "Image ${index + 1}" }} image: "
            module.prefillPrompt(label, 0, 0)

            val floats = ImageUtils.prepareImage(bitmap)
            module.prefillImages(
                floats,
                ModelConfig.Llm.IMAGE_SIZE,
                ModelConfig.Llm.IMAGE_SIZE,
                ModelConfig.Llm.IMAGE_CHANNELS
            )

            // Explicitly recycle since these were loaded specifically for inference
            bitmap.recycle()

            // Add newline after each image
            module.prefillPrompt("\n", 0, 0)
        }
    }

    suspend fun prefillContext(items: List<ContextItem>) = withContext(Dispatchers.IO) {
        val module = getModule() ?: throw IllegalStateException("Model not initialized")

        items.forEach { item ->
            when (item) {
                is ContextItem.Text -> {
                    module.prefillPrompt(item.content, 0, 0)
                }

                is ContextItem.Image -> {
                    try {
                        val bitmaps = loadImages(listOf(item.path))
                        if (bitmaps.isNotEmpty()) {
                            val bitmap = bitmaps.first()
                            val floats = ImageUtils.prepareImage(bitmap)
                            module.prefillImages(
                                floats,
                                ModelConfig.Llm.IMAGE_SIZE,
                                ModelConfig.Llm.IMAGE_SIZE,
                                ModelConfig.Llm.IMAGE_CHANNELS
                            )
                            bitmap.recycle()
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "LlmInferenceService",
                            "Failed to prefill image context: ${item.path}",
                            e
                        )
                    }
                }
            }
        }
    }

    suspend fun stop() {
        LlmModuleProvider.getModule()?.stop()
    }

    suspend fun reset() {
        LlmModuleProvider.getModule()?.resetContext()
    }

    private suspend fun loadImages(paths: List<String>): List<Bitmap> =
        withContext(Dispatchers.IO) {
            val loader = ImageLoader(context)
            val bitmaps = mutableListOf<Bitmap>()

            paths.forEach { path ->
                try {
                    val request = ImageRequest.Builder(context)
                        .data(path)
                        .allowHardware(false) // Must be software bitmap for pixel access
                        .build()
                    val result = loader.execute(request)
                    if (result is SuccessResult) {
                        (result.drawable as? BitmapDrawable)?.bitmap?.let { bitmaps.add(it) }
                    }
                } catch (e: Exception) {
                    Log.e("LlmInferenceService", "Failed to load image: $path", e)
                }
            }
            bitmaps
        }
}
