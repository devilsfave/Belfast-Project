package com.example.medgem

import android.app.Application
import com.example.medgem.data.ObjectBox
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MedGemApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Model Paths to internal storage
        val filesDir = filesDir.absolutePath
        ModelConfig.Llm.MODEL_PATH = "$filesDir/model.pte"
        ModelConfig.Llm.TOKENIZER_PATH = "$filesDir/tokenizer.model"
        ModelConfig.Embedding.MODEL_PATH = "$filesDir/embedding_gemma_no_normalize_q8.tflite"
        // Ensure Embedding uses the updated LLM tokenizer path if it was dependent, 
        // though typically it might keep its reference if it was a direct assignment.
        // In this case `var TOKENIZER_PATH = Llm.TOKENIZER_PATH` init happens at class load,
        // so we likely need to update it explicitly if we want it to follow.
        ModelConfig.Embedding.TOKENIZER_PATH = ModelConfig.Llm.TOKENIZER_PATH

        ModelConfig.MedAsr.MODEL_PATH = "$filesDir/model.int8.onnx"
        ModelConfig.MedAsr.TOKENS_PATH = "$filesDir/tokens.txt"

        // Initialize ObjectBox database
        ObjectBox.init(this)
    }
}
