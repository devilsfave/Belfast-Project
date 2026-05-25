package com.example.medgem.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medgem.ui.theme.MedGemTheme
import kotlinx.coroutines.delay

@Composable
fun ProcessingScreen(
    hasClarificationItems: Boolean,
    onComplete: (hasClarificationItems: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberProcessingColors()
    val statusMessages = remember {
        listOf(
            "Extracting clinical data...",
            "Checking safety requirements...",
            "Preparing your forms..."
        )
    }
    var statusIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2_000)
            statusIndex = (statusIndex + 1) % statusMessages.size
        }
    }

    LaunchedEffect(hasClarificationItems) {
        delay(3_000)
        onComplete(hasClarificationItems)
    }

    Scaffold(
        modifier = modifier,
        containerColor = colors.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Analysing your notes...",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.text,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = statusMessages[statusIndex],
                    style = MaterialTheme.typography.bodyMedium.copy(color = colors.mutedText),
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "This usually takes 20-40 seconds",
                modifier = Modifier.align(Alignment.BottomCenter),
                style = MaterialTheme.typography.bodySmall.copy(color = colors.mutedText),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun rememberProcessingColors(): ProcessingColors {
    val dark = isSystemInDarkTheme()
    return ProcessingColors(
        primary = Color(0xFF00695C),
        background = if (dark) Color(0xFF121212) else Color(0xFFFAFAFA),
        text = if (dark) Color(0xFFE8E8E8) else Color(0xFF1A1A1A),
        mutedText = if (dark) Color(0xFFB8B8B8) else Color(0xFF666666)
    )
}

private data class ProcessingColors(
    val primary: Color,
    val background: Color,
    val text: Color,
    val mutedText: Color
)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ProcessingScreenPreview() {
    MedGemTheme(dynamicColor = false) {
        ProcessingScreen(
            hasClarificationItems = true,
            onComplete = {}
        )
    }
}
