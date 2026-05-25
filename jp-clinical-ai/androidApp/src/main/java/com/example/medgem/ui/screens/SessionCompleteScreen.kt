package com.example.medgem.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medgem.ui.theme.MedGemTheme
import kotlinx.coroutines.launch

@Composable
fun SessionCompleteScreen(
    approvedFormCount: Int = 20,
    onStartNewSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberSessionCompleteColors()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
            Button(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Preparing FHIR bundle for upload...")
                    }
                },
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
        background = if (dark) Color(0xFF121212) else Color(0xFFFAFAFA),
        text = if (dark) Color(0xFFE8E8E8) else Color(0xFF1A1A1A),
        mutedText = if (dark) Color(0xFFB8B8B8) else Color(0xFF666666)
    )
}

private data class SessionCompleteColors(
    val primary: Color,
    val success: Color,
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
