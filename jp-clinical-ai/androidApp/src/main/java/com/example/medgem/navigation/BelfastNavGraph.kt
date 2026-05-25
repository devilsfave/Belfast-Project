package com.example.medgem.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.medgem.ui.screens.ClarificationQueueScreen
import com.example.medgem.ui.screens.FormReviewScreen
import com.example.medgem.ui.screens.ModelDownloadScreen
import com.example.medgem.ui.screens.NoteInputScreen
import com.example.medgem.ui.screens.ProcessingScreen
import com.example.medgem.ui.screens.SessionCompleteScreen
import com.example.medgem.ui.viewmodel.BelfastPipelineStage
import com.example.medgem.ui.viewmodel.BelfastPipelineViewModel
import java.io.File

object BelfastRoute {
    const val NOTE_INPUT = "note_input"
    const val PROCESSING = "processing"
    const val CLARIFICATION_QUEUE = "clarification_queue"
    const val FORM_REVIEW = "form_review"
    const val SESSION_COMPLETE = "session_complete"
    const val MODEL_DOWNLOAD = "model_download"
}

@Composable
fun BelfastNavGraph() {
    val navController = rememberNavController()
    val viewModel: BelfastPipelineViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val modelsDownloaded = remember { areModelsDownloaded(context) }
    val startDestination = if (modelsDownloaded) BelfastRoute.NOTE_INPUT else BelfastRoute.MODEL_DOWNLOAD

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(BelfastRoute.NOTE_INPUT) {
            NoteInputScreen(
                onProcessNotes = { hcNumber, noteText ->
                    viewModel.startSession(hcNumber, noteText)
                    navController.navigate(BelfastRoute.PROCESSING)
                }
            )
        }

        composable(BelfastRoute.MODEL_DOWNLOAD) {
            ModelDownloadScreen(
                onDownloadCompleted = {
                    navController.navigate(BelfastRoute.NOTE_INPUT) {
                        popUpTo(BelfastRoute.MODEL_DOWNLOAD) { inclusive = true }
                    }
                }
            )
        }

        composable(BelfastRoute.PROCESSING) {
            LaunchedEffect(uiState.stage) {
                when (uiState.stage) {
                    BelfastPipelineStage.NEEDS_CLARIFICATION -> {
                        navController.navigate(BelfastRoute.CLARIFICATION_QUEUE) {
                            popUpTo(BelfastRoute.PROCESSING) { inclusive = true }
                        }
                    }
                    BelfastPipelineStage.READY_FOR_REVIEW -> {
                        navController.navigate(BelfastRoute.FORM_REVIEW) {
                            popUpTo(BelfastRoute.PROCESSING) { inclusive = true }
                        }
                    }
                    else -> Unit
                }
            }

            ProcessingScreen(
                hasClarificationItems = uiState.clarificationItems.isNotEmpty(),
                onComplete = {},
                statusMessage = uiState.statusMessage,
                errorMessage = uiState.errorMessage,
                autoComplete = false,
                onBackToNotes = {
                    navController.navigate(BelfastRoute.NOTE_INPUT) {
                        popUpTo(BelfastRoute.NOTE_INPUT) { inclusive = true }
                    }
                    viewModel.resetForNewSession()
                }
            )
        }

        composable(BelfastRoute.CLARIFICATION_QUEUE) {
            ClarificationQueueScreen(
                items = uiState.clarificationItems,
                onSubmit = { answers ->
                    viewModel.submitClarifications(answers)
                    navController.navigate(BelfastRoute.PROCESSING) {
                        popUpTo(BelfastRoute.CLARIFICATION_QUEUE) { inclusive = true }
                    }
                }
            )
        }

        composable(BelfastRoute.FORM_REVIEW) {
            FormReviewScreen(
                forms = uiState.reviewForms,
                hardBlocks = uiState.hardBlocks,
                softFlags = uiState.softFlags,
                onSubmitForEpicSync = {
                    navController.navigate(BelfastRoute.SESSION_COMPLETE)
                }
            )
        }

        composable(BelfastRoute.SESSION_COMPLETE) {
            SessionCompleteScreen(
                approvedFormCount = uiState.reviewForms.size,
                onStartNewSession = {
                    viewModel.resetForNewSession()
                    navController.navigate(BelfastRoute.NOTE_INPUT) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

private fun areModelsDownloaded(context: android.content.Context): Boolean {
    val filesDir = context.filesDir
    val requiredFiles = listOf(
        "model.pte",
        "tokenizer.model",
        "embedding_gemma_no_normalize_q8.tflite",
        "model.int8.onnx",
        "tokens.txt"
    )
    return requiredFiles.all { path ->
        val destinationFile = File(filesDir, path)
        val markerFile = File(destinationFile.absolutePath + ".complete")
        destinationFile.exists() && destinationFile.length() > 0 && markerFile.exists()
    }
}
