package com.example.medgem.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.belfasttrust.jpclinical.android.BuildConfig
import com.example.medgem.sync.EpicSyncService
import com.example.medgem.sync.SyncResult
import com.example.medgem.ui.theme.MedGemTheme
import kotlinx.coroutines.launch

sealed class SyncUiState {
    object Idle : SyncUiState()
    object Loading : SyncUiState()
    data class Success(val timestamp: String) : SyncUiState()
    data class Error(val error: String) : SyncUiState()
}

@Composable
fun SessionCompleteScreen(
    approvedFormCount: Int = 20,
    onStartNewSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberSessionCompleteColors()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var syncState by remember { mutableStateOf<SyncUiState>(SyncUiState.Idle) }
    val syncService = remember { EpicSyncService() }

    fun triggerSync() {
        syncState = SyncUiState.Loading
        scope.launch {
            try {
                // Get SMART on FHIR Auth Token
                val token = syncService.getAuthToken(
                    nurseId = "jp-nurse-001",
                    password = "belfast-trust-secure",
                    smartFhirUrl = "https://placeholder.belfasttrust.hscni.net/fhir"
                )

                // Create mock bundles for the sync service
                val mockSafetyPlanBundle = org.hl7.fhir.r4.model.Bundle().apply {
                    id = "safety-plan-belfast"
                    type = org.hl7.fhir.r4.model.Bundle.BundleType.DOCUMENT
                }
                val mockPisaniBundle = org.hl7.fhir.r4.model.Bundle().apply {
                    id = "pisani-belfast"
                    type = org.hl7.fhir.r4.model.Bundle.BundleType.DOCUMENT
                }

                val exportBundle = com.example.medgem.fhir.FhirExportBundle(
                    safetyPlanBundle = mockSafetyPlanBundle,
                    pisaniBundle = mockPisaniBundle
                )

                val result = syncService.syncFhirBundle(
                    bundle = exportBundle,
                    epicEndpointUrl = BuildConfig.EPIC_ENDPOINT_URL,
                    authToken = token ?: "dummy-auth-token-123"
                )

                when (result) {
                    is SyncResult.Success -> {
                        syncState = SyncUiState.Success(result.timestamp)
                        snackbarHostState.showSnackbar("FHIR bundle successfully synced to Epic EHR")
                    }
                    is SyncResult.Failure -> {
                        syncState = SyncUiState.Error(result.error)
                        snackbarHostState.showSnackbar("Sync failed: ${result.error}")
                    }
                }
            } catch (e: Exception) {
                syncState = SyncUiState.Error(e.localizedMessage ?: "Unknown error occurred")
                snackbarHostState.showSnackbar("Sync error: ${e.localizedMessage}")
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = colors.success,
                modifier = Modifier.size(92.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Session complete",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = colors.text,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All $approvedFormCount forms reviewed and approved",
                style = MaterialTheme.typography.bodyLarge.copy(color = colors.mutedText),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Sync Status UI
            when (val state = syncState) {
                is SyncUiState.Idle -> {
                    Button(
                        onClick = { triggerSync() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "SYNC TO EPIC EHR",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
                is SyncUiState.Loading -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary.copy(alpha = 0.6f),
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "SYNCING TO EPIC...",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
                is SyncUiState.Success -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = colors.success,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Synced to Epic EHR ✓",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = colors.success,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = "Timestamp: ${state.timestamp}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = colors.mutedText)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                is SyncUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = colors.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sync failed",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = colors.error,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyMedium.copy(color = colors.error),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { triggerSync() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.error,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "RETRY SYNC",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onStartNewSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, colors.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary)
            ) {
                Text(
                    text = "START NEW SESSION",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun rememberSessionCompleteColors(): SessionCompleteColors {
    val dark = isSystemInDarkTheme()
    return SessionCompleteColors(
        primary = Color(0xFF00695C),
        success = Color(0xFF2E7D32),
        error = Color(0xFFC62828),
        background = if (dark) Color(0xFF121212) else Color(0xFFFAFAFA),
        text = if (dark) Color(0xFFE8E8E8) else Color(0xFF1A1A1A),
        mutedText = if (dark) Color(0xFFB8B8B8) else Color(0xFF666666)
    )
}

private data class SessionCompleteColors(
    val primary: Color,
    val success: Color,
    val error: Color,
    val background: Color,
    val text: Color,
    val mutedText: Color
)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SessionCompleteScreenPreview() {
    MedGemTheme(dynamicColor = false) {
        SessionCompleteScreen(onStartNewSession = {})
    }
}
