package com.example.medgem

import android.util.Log
import com.example.medgem.data.GemmaEmbeddingModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** Singleton provider for GemmaEmbeddingModel. */
object EmbeddingModuleProvider {
    private const val TAG = "EmbeddingModuleProvider"

    private var embeddingModel: GemmaEmbeddingModel? = null
    private var loadError: String? = null
    private var isLoading = false

    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** Initialize the embedding model. */
    suspend fun initialize(): Result<GemmaEmbeddingModel> =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                embeddingModel?.let {
                    return@withContext Result.success(it)
                }
                loadError?.let {
                    return@withContext Result.failure(Exception(it))
                }
                if (isLoading) return@withContext Result.failure(Exception("Model is loading"))

                isLoading = true
                try {
                    if (!File(ModelConfig.Embedding.MODEL_PATH).exists()) {
                        val error =
                            "Embedding model not found at ${ModelConfig.Embedding.MODEL_PATH}"
                        loadError = error
                        return@withContext Result.failure(Exception(error))
                    }

                    val model =
                        GemmaEmbeddingModel(
                            ModelConfig.Embedding.MODEL_PATH,
                            ModelConfig.Embedding.TOKENIZER_PATH,
                            ModelConfig.Embedding.USE_GPU
                        )
                    embeddingModel = model
                    Log.d(TAG, "Embedding model loaded successfully")
                    Result.success(model)
                } catch (e: Exception) {
                    val error = "Error loading embedding model: ${e.message}"
                    loadError = error
                    Log.e(TAG, error, e)
                    Result.failure(e)
                } finally {
                    isLoading = false
                }
            }
        }

    fun getModel(): GemmaEmbeddingModel? = embeddingModel
    fun isLoaded(): Boolean = embeddingModel != null

    /** Destroy the embedding model to free memory. */
    fun destroy() {
        val model = embeddingModel
        embeddingModel = null
        if (model != null) {
            scope.launch {
                model.close()
                Log.d(TAG, "Embedding model destroyed")
            }
        }
    }

    /** Reset error state to allow retry. */
    fun reset() {
        loadError = null
    }
}
