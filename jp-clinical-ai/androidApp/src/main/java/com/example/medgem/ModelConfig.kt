package com.example.medgem

object ModelConfig {

    // Paths are initialized in MedGemApplication.onCreate() to use internal storage
    object Llm {
        lateinit var MODEL_PATH: String
        lateinit var TOKENIZER_PATH: String
        const val DEFAULT_TEMPERATURE = 0.0f
        const val DEFAULT_TOP_P = 0.9f
        const val MODEL_CATEGORY = 2 // VISION_MODEL for Gemma 3
        const val DEFAULT_PREFILL_CHUNK_SIZE = 1024
        const val PREFILL_CHUNK_SIZE_WITH_BOS = 1024
        const val PREFILL_CHUNK_SIZE_WITH_BOS_ADJUSTED = 1023
        const val IMAGE_SIZE = 896
        const val IMAGE_CHANNELS = 3
        const val MAX_SEQ_LEN = 131072
    }

    object Embedding {
        lateinit var MODEL_PATH: String
        lateinit var TOKENIZER_PATH: String
        const val USE_GPU = false
    }

    object MedAsr {
        lateinit var MODEL_PATH: String
        lateinit var TOKENS_PATH: String
        const val SAMPLE_RATE = 16000
    }
}
