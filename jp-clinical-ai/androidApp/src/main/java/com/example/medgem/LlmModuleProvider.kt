package com.example.medgem

import android.util.Log
import com.belfasttrust.jpclinical.android.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.pytorch.executorch.extension.llm.LlmModule
import java.io.File

/**
 * Singleton provider for LlmModule to ensure the model is loaded only once. Manages the lifecycle
 * of the model and provides thread-safe access.
 */
object LlmModuleProvider {
    private const val TAG = "LlmModuleProvider"

    private var llmModule: LlmModule? = null
    private var loadError: String? = null
    private var isLoading = false

    private val mutex = Mutex()

    /**
     * Initialize and load the LlmModule. This is thread-safe and will only load once. Subsequent
     * calls return the cached instance unless reinitializing.
     *
     * @param visionEnabled Whether to enable the vision encoder
     * @param prefillChunkSize The number of tokens to process at once during prefill
     * @return Result containing the LlmModule or an error
     */
    suspend fun initialize(
        visionEnabled: Boolean = BuildConfig.ENABLE_VISION_ENCODER,
        prefillChunkSize: Int = ModelConfig.Llm.DEFAULT_PREFILL_CHUNK_SIZE
    ): Result<LlmModule> =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                // Return cached instance if already loaded
                llmModule?.let { cached ->
                    if (currentVisionEnabled == visionEnabled &&
                        currentPrefillChunkSize == prefillChunkSize
                    ) {
                        Log.d(TAG, "Returning cached LlmModule instance")
                        return@withContext Result.success(cached)
                    }

                    Log.d(TAG, "Config changed. Reloading LlmModule instance")
                    cached.stop()
                    llmModule = null
                }

                // Return cached error if already failed
                loadError?.let {
                    // If config changed, we should retry instead of returning old error
                    // But for now, simple logic
                    // return@withContext Result.failure(Exception(it))
                }

                // Prevent concurrent loading attempts (though mutex handles this)
                if (isLoading) {
                    return@withContext Result.failure(Exception("Model is already loading"))
                }

                isLoading = true
                loadError = null

                try {
                    // Check if model files exist
                    if (!File(ModelConfig.Llm.MODEL_PATH).exists()) {
                        val error = "Model file not found at ${ModelConfig.Llm.MODEL_PATH}"
                        loadError = error
                        Log.e(TAG, error)
                        return@withContext Result.failure(Exception(error))
                    }

                    if (!File(ModelConfig.Llm.TOKENIZER_PATH).exists()) {
                        val error =
                            "Tokenizer file not found at ${ModelConfig.Llm.TOKENIZER_PATH}"
                        loadError = error
                        Log.e(TAG, error)
                        return@withContext Result.failure(Exception(error))
                    }

                    // If prefill chunk size is set to multiple of 1024 (e.g. 1024), reduce by 1
                    // to accommodate BOS token
                    // This is a known issue/requirement for some backends to prevent OOM or
                    // assertion errors
                    val effectivePrefillChunkSize =
                        if (prefillChunkSize == ModelConfig.Llm.PREFILL_CHUNK_SIZE_WITH_BOS
                        ) {
                            ModelConfig.Llm.PREFILL_CHUNK_SIZE_WITH_BOS_ADJUSTED
                        } else {
                            prefillChunkSize
                        }

                    Log.d(
                        TAG,
                        "Loading LlmModule with vision=$visionEnabled, prefill=$effectivePrefillChunkSize..."
                    )

                    val module =
                        LlmModule(
                            ModelConfig.Llm.MODEL_CATEGORY,
                            ModelConfig.Llm.MODEL_PATH,
                            ModelConfig.Llm.TOKENIZER_PATH,
                            ModelConfig.Llm.DEFAULT_TEMPERATURE,
                            ModelConfig.Llm.DEFAULT_TOP_P,
                            emptyList<String>(), // dataFiles
                            0, // numBos (Controlled via generate)
                            0, // numEos
                            effectivePrefillChunkSize, // prefillChunkSize
                            visionEnabled, // visionEnabled
                            false // audioEnabled
                        )
                    // module.load() - Removed as it's not available in the current AAR and
                    // generate() triggers load

                    llmModule = module
                    currentVisionEnabled = visionEnabled
                    currentPrefillChunkSize = effectivePrefillChunkSize
                    Log.d(TAG, "Model loaded successfully")

                    Result.success(module)
                } catch (t: Throwable) { // Catch Throwable to handle native errors
                    val error = "Error loading model: ${t.message}"
                    loadError = error
                    Log.e(TAG, error, t)
                    Result.failure(Exception(t))
                } finally {
                    isLoading = false
                }
            }
        }

    // We need to track the current configuration to avoid unnecessary reloads
    private var currentVisionEnabled: Boolean = BuildConfig.ENABLE_VISION_ENCODER
    private var currentPrefillChunkSize: Int = ModelConfig.Llm.DEFAULT_PREFILL_CHUNK_SIZE

    suspend fun updateModelConfig(
        visionEnabled: Boolean,
        prefillChunkSize: Int
    ): Result<LlmModule> =
        withContext(Dispatchers.IO) {
            // We can just call initialize directly since it handles caching and config checking
            // inside the lock
            return@withContext initialize(visionEnabled, prefillChunkSize)
        }

    /** Get the loaded LlmModule instance, or null if not yet loaded. */
    fun getModule(): LlmModule? = llmModule

    /** Check if the model is currently loaded and ready. */
    fun isLoaded(): Boolean = llmModule != null

    /** Get the current loading status message. */
    fun getStatusMessage(): String {
        return when {
            llmModule != null -> "Model Loaded. Ready to chat."
            isLoading -> "Loading model..."
            loadError != null -> loadError!!
            else -> "Initializing..."
        }
    }
}
