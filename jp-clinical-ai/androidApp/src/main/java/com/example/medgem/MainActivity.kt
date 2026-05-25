package com.example.medgem

import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.belfasttrust.jpclinical.android.BuildConfig
import com.example.medgem.data.ChatRepository
import com.example.medgem.data.UserPreferencesRepository
import com.example.medgem.navigation.BelfastNavGraph
import com.example.medgem.ui.components.UnifiedConversationListScreen
import com.example.medgem.ui.screens.DisclaimerScreen
import com.example.medgem.ui.screens.ModelDownloadScreen
import com.example.medgem.ui.screens.PatientDetailScreen
import com.example.medgem.ui.screens.PatientListScreen
import com.example.medgem.ui.screens.VisitDetailScreen
import com.example.medgem.ui.theme.MedGemTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.pytorch.executorch.extension.llm.LlmModule
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var chatRepository: ChatRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Model initialization is now handled in MainScreen when onboarding is complete

        setContent {
            MedGemTheme {
                if (BuildConfig.SHOW_BELFAST_UI) {
                    BelfastNavGraph()
                } else {
                    MainScreen(
                        userPreferencesRepository = userPreferencesRepository,
                        chatRepository = chatRepository
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    userPreferencesRepository: UserPreferencesRepository,
    chatRepository: ChatRepository
) {
    val navController = rememberNavController()

    // Observe vision setting
    val visionEnabled by userPreferencesRepository.visionEnabled.collectAsState(initial = true)
    val prefillChunkSize by
    userPreferencesRepository.prefillChunkSize.collectAsState(initial = 1024)

    // Track model loading state at app level
    var isModelLoaded by remember { mutableStateOf(LlmModuleProvider.isLoaded()) }
    var modelStatusMessage by remember { mutableStateOf(LlmModuleProvider.getStatusMessage()) }
    var llmModule by remember { mutableStateOf<LlmModule?>(LlmModuleProvider.getModule()) }

    // Track which conversation is currently loaded in the LLM context
    var lastLoadedConversationId by remember { mutableStateOf<Long?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Use null as initial value to wait for DataStore to load the actual state
    val isOnboardingCompletedState by
    userPreferencesRepository.isOnboardingCompleted.collectAsState(
        initial = null
    )

    // Wait for preference to load before rendering navigation
    if (isOnboardingCompletedState == null) {
        return
    }
    val isOnboardingCompleted = isOnboardingCompletedState!!

    // Show Bottom Bar only on main screens
    val showBottomBar =
        currentRoute in
                listOf(
                    Route.Home.route,
                    Route.ChatConversationList.route,
                    Route.Protocols.route
                )

    // Determines start destination based on onboarding
    val startDestination = if (isOnboardingCompleted) Route.Home.route else Route.Onboarding.route

    // Initial load and reload on setting change - Only if onboarding is complete
    LaunchedEffect(visionEnabled, prefillChunkSize, isOnboardingCompleted) {
        if (isOnboardingCompleted) {
            val result = LlmModuleProvider.updateModelConfig(visionEnabled, prefillChunkSize)
            result
                .onSuccess { module ->
                    llmModule = module
                    isModelLoaded = true
                    modelStatusMessage = LlmModuleProvider.getStatusMessage()
                }
                .onFailure { modelStatusMessage = LlmModuleProvider.getStatusMessage() }
        }
    }

    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                com.example.medgem.ui.components.BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Route.Home.route) {
                DashboardScreen(
                    onNavigateToChat = {
                        // For Quick Action "New Chat", go directly to chat screen with null ID
                        navController.navigate(Route.Chat.createRoute(null, false))
                    },
                    onNavigateToRagChat = {
                        // For Quick Action "RAG Search", go directly to RAG chat screen with
                        // null ID
                        navController.navigate(Route.RagChat.createRoute(null))
                    },
                    onNavigateToKnowledge = {
                        navController.navigate(Route.KnowledgeSearch.route)
                    },
                    onNavigateToProtocols = { navController.navigate(Route.Protocols.route) },
                    onNavigateToSettings = { navController.navigate(Route.Settings.route) },
                    onNavigateToHistory = {
                        navController.navigate(Route.ChatConversationList.route)
                    },
                    onNavigateToPatients = { navController.navigate(Route.PatientList.route) },
                    onConversationSelected = { conversation ->
                        if (conversation.conversationType == "RAG") {
                            navController.navigate(Route.RagChat.createRoute(conversation.id))
                        } else {
                            navController.navigate(
                                Route.Chat.createRoute(conversation.id, false)
                            )
                        }
                    }
                )
            }

            composable(Route.PatientList.route) {
                PatientListScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToPatientDetail = { id ->
                        navController.navigate(Route.PatientDetail.createRoute(id))
                    }
                )
            }

            composable(
                route = Route.PatientDetail.route,
                arguments =
                    listOf(
                        navArgument(Route.PatientDetail.ArgPatientId) {
                            type = NavType.LongType
                        }
                    )
            ) { backStackEntry ->
                val patientId =
                    backStackEntry.arguments?.getLong(Route.PatientDetail.ArgPatientId) ?: 0L
                PatientDetailScreen(
                    patientId = patientId,
                    onBack = { navController.popBackStack() },
                    onNavigateToVisit = { visitId, pId ->
                        navController.navigate(Route.VisitDetail.createRoute(visitId, pId))
                    },
                    onNavigateToChat = { convId, pId, vIds ->
                        navController.navigate(Route.Chat.createRoute(convId, false, pId, vIds))
                    },
                    onNavigateToRagChat = { convId, pId ->
                        navController.navigate(Route.RagChat.createRoute(convId, pId))
                    }
                )
            }

            composable(
                route = Route.VisitDetail.route,
                arguments =
                    listOf(
                        navArgument(Route.VisitDetail.ArgVisitId) {
                            type = NavType.LongType
                        },
                        navArgument(Route.VisitDetail.ArgPatientId) {
                            type = NavType.LongType
                        }
                    )
            ) { backStackEntry ->
                val visitId = backStackEntry.arguments?.getLong(Route.VisitDetail.ArgVisitId) ?: 0L
                val patientId =
                    backStackEntry.arguments?.getLong(Route.VisitDetail.ArgPatientId) ?: 0L
                VisitDetailScreen(
                    visitId = visitId,
                    patientId = patientId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Route.Onboarding.route) {
                OnboardingScreen(
                    onOnboardingCompleted = {
                        navController.navigate(Route.Disclaimer.route)
                    },
                    userPreferencesRepository = userPreferencesRepository
                )
            }

            composable(Route.Disclaimer.route) {
                DisclaimerScreen(
                    onDisclaimerAccepted = {
                        scope.launch {
                            navController.navigate(Route.ModelDownload.route)
                        }
                    }
                )
            }

            composable(Route.ModelDownload.route) {
                ModelDownloadScreen(
                    onDownloadCompleted = {
                        scope.launch {
                            userPreferencesRepository.setOnboardingCompleted(true)
                            // Trigger model load now that files are available
                            LlmModuleProvider.initialize()
                            navController.navigate(Route.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onBack = if (isOnboardingCompleted) {
                        { navController.popBackStack() }
                    } else null
                )
            }

            composable(Route.KnowledgeSearch.route) {
                KnowledgeSearchScreen(
                    onPdfClick = { fileName, page, query ->
                        navController.navigate(
                            Route.PdfViewer.createRoute(fileName, page, query)
                        )
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Route.ChatConversationList.route) {
                UnifiedConversationListScreen(
                    title = "History",
                    emptyIcon = Icons.Default.History,
                    emptyText = "No history yet",
                    emptySubText = "Start a new chat or search to see history",
                    conversations = chatRepository.getGlobalConversations(),
                    visionEnabled = visionEnabled,
                    showBackButton = false,
                    onNewRagConversation = {
                        navController.navigate(Route.RagChat.createRoute(null))
                    },
                    onBack = { navController.popBackStack() },
                    onConversationSelected = { conversation, isReadOnly ->
                        when (conversation.getType()) {
                            com.example.medgem.data.ConversationType.RAG -> {
                                navController.navigate(
                                    Route.RagChat.createRoute(conversation.id)
                                )
                            }

                            else -> {
                                navController.navigate(
                                    Route.Chat.createRoute(conversation.id, isReadOnly)
                                )
                            }
                        }
                    },
                    onNewConversation = {
                        navController.navigate(Route.Chat.createRoute(null, false))
                    },
                    chatRepository = chatRepository
                )
            }

            composable(
                route = Route.Chat.route,
                arguments =
                    listOf(
                        navArgument(Route.Chat.ArgConversationId) {
                            type = NavType.LongType
                            defaultValue = -1L
                        },
                        navArgument(Route.Chat.ArgReadOnly) {
                            type = NavType.BoolType
                            defaultValue = false
                        },
                        navArgument(Route.Chat.ArgPatientId) {
                            type = NavType.LongType
                            defaultValue = 0L
                        },
                        navArgument(Route.Chat.ArgVisitIds) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
            ) { backStackEntry ->
                val conversationId =
                    backStackEntry.arguments?.getLong(Route.Chat.ArgConversationId)?.takeIf {
                        it != -1L
                    }
                val isReadOnly =
                    backStackEntry.arguments?.getBoolean(Route.Chat.ArgReadOnly) ?: false

                ChatScreen(
                    onBack = { navController.popBackStack() },
                    conversationId = conversationId,
                    llmModule = llmModule,
                    isModelLoaded = isModelLoaded,
                    modelStatusMessage = modelStatusMessage,
                    userPreferencesRepository = userPreferencesRepository,
                    isReadOnly = isReadOnly
                )
            }

            composable(
                route = Route.RagChat.route,
                arguments =
                    listOf(
                        navArgument(Route.RagChat.ArgConversationId) {
                            type = NavType.LongType
                            defaultValue = -1L
                        },
                        navArgument(Route.RagChat.ArgPatientId) {
                            type = NavType.LongType
                            defaultValue = 0L
                        }
                    )
            ) { backStackEntry ->
                val conversationId =
                    backStackEntry.arguments?.getLong(Route.RagChat.ArgConversationId)?.takeIf {
                        it != -1L
                    }

                RagChatScreen(
                    onBack = { navController.popBackStack() },
                    onPdfClick = { fileName, page ->
                        navController.navigate(
                            Route.PdfViewer.createRoute(fileName, page, null)
                        )
                    },
                    conversationId = conversationId,
                    llmModule = llmModule,
                    isModelLoaded = isModelLoaded,
                    modelStatusMessage = modelStatusMessage,
                    userPreferencesRepository = userPreferencesRepository,
                    lastLoadedConversationId = lastLoadedConversationId,
                    onConversationLoaded = { id -> lastLoadedConversationId = id }
                )
            }

            composable(
                route = Route.PdfViewer.route,
                arguments =
                    listOf(
                        navArgument(Route.PdfViewer.ArgFileName) {
                            type = NavType.StringType
                        },
                        navArgument(Route.PdfViewer.ArgPageNumber) {
                            type = NavType.IntType
                        },
                        navArgument(Route.PdfViewer.ArgQuery) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
            ) { backStackEntry -> PdfViewerRoute(navController, backStackEntry) }

            composable(Route.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToModelDownload = {
                        navController.navigate(Route.ModelDownload.route)
                    }
                )
            }

            composable(Route.Protocols.route) {
                ProtocolsScreen(
                    onPdfClick = { fileName ->
                        navController.navigate(Route.PdfViewer.createRoute(fileName, 0, null))
                    }
                )
            }
        }
    }
}

@android.annotation.SuppressLint("NewApi")
@Composable
fun PdfViewerRoute(
    navController: androidx.navigation.NavController,
    backStackEntry: androidx.navigation.NavBackStackEntry
) {
    val fileName =
        backStackEntry.arguments?.getString(Route.PdfViewer.ArgFileName)?.let { Uri.decode(it) }
            ?: ""
    val pageNumber = backStackEntry.arguments?.getInt(Route.PdfViewer.ArgPageNumber) ?: 0
    val searchQuery =
        backStackEntry.arguments?.getString(Route.PdfViewer.ArgQuery)?.let { Uri.decode(it) }

    PdfViewerScreen(
        pdfFileName = fileName,
        pageNumber = pageNumber,
        searchQuery = searchQuery,
        onBack = { navController.popBackStack() }
    )
}
