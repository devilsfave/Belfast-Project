package com.example.medgem.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medgem.ui.screens.ClarificationQueueScreen
import com.example.medgem.ui.screens.FormReviewScreen
import com.example.medgem.ui.screens.NoteInputScreen
import com.example.medgem.ui.screens.ProcessingScreen
import com.example.medgem.ui.screens.SessionCompleteScreen

object BelfastRoute {
    const val NOTE_INPUT = "note_input"
    const val PROCESSING = "processing"
    const val CLARIFICATION_QUEUE = "clarification_queue"
    const val FORM_REVIEW = "form_review"
    const val SESSION_COMPLETE = "session_complete"
}

@Composable
fun BelfastNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = BelfastRoute.NOTE_INPUT
    ) {
        composable(BelfastRoute.NOTE_INPUT) {
            NoteInputScreen(
                onProcessNotes = { _, _ ->
                    navController.navigate(BelfastRoute.PROCESSING)
                }
            )
        }

        composable(BelfastRoute.PROCESSING) {
            ProcessingScreen(
                hasClarificationItems = true,
                onComplete = { hasClarificationItems ->
                    val nextRoute = if (hasClarificationItems) {
                        BelfastRoute.CLARIFICATION_QUEUE
                    } else {
                        BelfastRoute.FORM_REVIEW
                    }
                    navController.navigate(nextRoute) {
                        popUpTo(BelfastRoute.PROCESSING) { inclusive = true }
                    }
                }
            )
        }

        composable(BelfastRoute.CLARIFICATION_QUEUE) {
            ClarificationQueueScreen(
                onSubmit = {
                    navController.navigate(BelfastRoute.FORM_REVIEW)
                }
            )
        }

        composable(BelfastRoute.FORM_REVIEW) {
            FormReviewScreen(
                hardBlocks = emptyList(),
                softFlags = emptyList(),
                onSubmitForEpicSync = {
                    navController.navigate(BelfastRoute.SESSION_COMPLETE)
                }
            )
        }

        composable(BelfastRoute.SESSION_COMPLETE) {
            SessionCompleteScreen(
                onStartNewSession = {
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
