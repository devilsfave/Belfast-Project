package com.example.medgem

import android.util.Log
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineMedAsrCtcModelConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Singleton provider for Sherpa-Onnx OfflineRecognizer.
 */
object MedAsrProvider {
    private const val TAG = "MedAsrProvider"

    private var recognizer: OfflineRecognizer? = null
    private var loadError: String? = null
    private var isLoading = false

    private val mutex = Mutex()

    suspend fun initialize(): Result<OfflineRecognizer> =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                recognizer?.let { return@withContext Result.success(it) }

                if (isLoading) {
                    return@withContext Result.failure(Exception("ASR model is already loading"))
                }

                isLoading = true
                loadError = null

                try {
                    if (!File(ModelConfig.MedAsr.MODEL_PATH).exists()) {
                        throw Exception("MedAsr model not found at ${ModelConfig.MedAsr.MODEL_PATH}")
                    }
                    if (!File(ModelConfig.MedAsr.TOKENS_PATH).exists()) {
                        throw Exception("MedAsr tokens not found at ${ModelConfig.MedAsr.TOKENS_PATH}")
                    }

                    val config = OfflineRecognizerConfig(
                        featConfig = FeatureConfig(
                            sampleRate = ModelConfig.MedAsr.SAMPLE_RATE,
                            featureDim = 80
                        ),
                        modelConfig = OfflineModelConfig(
                            medasr = OfflineMedAsrCtcModelConfig(
                                model = ModelConfig.MedAsr.MODEL_PATH
                            ),
                            tokens = ModelConfig.MedAsr.TOKENS_PATH,
                            modelType = "medasr",
                            numThreads = 4,
                            debug = true
                        )
                    )

                    Log.d(TAG, "Initializing OfflineRecognizer...")
                    val instance = OfflineRecognizer(config = config)
                    recognizer = instance
                    Log.d(TAG, "OfflineRecognizer initialized successfully")
                    Result.success(instance)
                } catch (e: Exception) {
                    val error = "Error loading MedAsr model: ${e.message}"
                    loadError = error
                    Log.e(TAG, error, e)
                    Result.failure(e)
                } finally {
                    isLoading = false
                }
            }
        }

    fun getRecognizer(): OfflineRecognizer? = recognizer
    fun isLoaded(): Boolean = recognizer != null
}
