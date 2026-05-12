package com.example.medgem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.medgem.ui.components.MedGemTopBar
import com.example.medgem.ui.viewmodel.SettingsViewModel
import java.util.Locale

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToModelDownload: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    // Global LLM Settings
    val temperature by viewModel.temperature.collectAsState()
    val topP by viewModel.topP.collectAsState()
    val systemPrompt by viewModel.systemPrompt.collectAsState()
    val thinkingEnabled by viewModel.thinkingEnabled.collectAsState()
    val visionEnabled by viewModel.visionEnabled.collectAsState()
    val prefillChunkSize by viewModel.prefillChunkSize.collectAsState()
    val maxSequenceLength by viewModel.maxSequenceLength.collectAsState()

    // RAG Settings
    val ragTemperature by viewModel.ragTemperature.collectAsState()
    val ragTopP by viewModel.ragTopP.collectAsState()
    val maxSearchSteps by viewModel.maxSearchSteps.collectAsState()
    val maxContextTokens by viewModel.maxContextTokens.collectAsState()
    val maxRetrievedDocs by viewModel.maxRetrievedDocs.collectAsState()
    val minRetrievalScore by viewModel.minRetrievalScore.collectAsState()

    // SOAP Settings
    val soapTemperature by viewModel.soapTemperature.collectAsState()
    val soapTopP by viewModel.soapTopP.collectAsState()
    val soapSystemPrompt by viewModel.soapSystemPrompt.collectAsState()
    val soapThinkingEnabled by viewModel.soapThinkingEnabled.collectAsState()

    // Context Chat Settings
    val contextChatTemperature by viewModel.contextChatTemperature.collectAsState()
    val contextChatTopP by viewModel.contextChatTopP.collectAsState()
    val contextChatSystemPrompt by viewModel.contextChatSystemPrompt.collectAsState()
    val contextChatThinkingEnabled by viewModel.contextChatThinkingEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MedGemTopBar(
            title = "Settings",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // --- Common Model Configuration ---
            Text(
                "Common Model Configuration",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Manage Models Button
            Button(
                onClick = onNavigateToModelDownload,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage Downloaded Models")
            }
            Text(
                "View status, re-download, or change context length of models.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Vision Enabled
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Vision Encoder",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = visionEnabled,
                    onCheckedChange = { viewModel.setVisionEnabled(it) }
                )
            }
            Text(
                "Enables image input analysis capabilities. Requires model reload.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Prefill Chunk Size
            Text(
                text = "Prefill Chunk Size: $prefillChunkSize",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = prefillChunkSize.toFloat(),
                onValueChange = { viewModel.setPrefillChunkSize(it.toInt()) },
                valueRange = 32f..1024f,
                steps = 30 // increments of 32
            )
            Text(
                "Adjusts how many tokens are processed at once during prefill. Lower values reduce memory usage but may be slower. Requires model reload.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Max New Tokens
            Text(
                text = "Max New Tokens: $maxSequenceLength",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = maxSequenceLength.toFloat(),
                onValueChange = {
                    val steps = 128
                    val value = it.toInt()
                    val roundedValue = ((value + steps / 2) / steps) * steps
                    val constrainedValue = roundedValue.coerceIn(128, 8192)
                    viewModel.setMaxSequenceLength(constrainedValue)
                },
                valueRange = 128f..8192f,
                steps = 62 // (8192 - 128) / 128 approx 63 steps
            )
            Text(
                "Maximum number of tokens the model can generate.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // --- Global Generation Settings ---
            Text(
                "Gemma Generation Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Temperature
            Text(
                text = String.format(Locale.US, "Temperature: %.2f", temperature),
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = temperature,
                onValueChange = { viewModel.setTemperature(it) },
                valueRange = 0.0f..1.0f,
                steps = 19 // increments of 0.05
            )
            Text(
                "Controls randomness. Lower values are more deterministic.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Top P
            Text(
                text = String.format(Locale.US, "Top-P: %.2f", topP),
                style = MaterialTheme.typography.bodyMedium,
                color = if (temperature > 0f) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            )
            Slider(
                value = topP,
                onValueChange = { viewModel.setTopP(it) },
                valueRange = 0.0f..1.0f,
                steps = 19,
                enabled = temperature > 0f
            )
            Text(
                "Nucleus sampling probability. Lower values filter out less likely tokens.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // System Prompt
            Text(
                "System Prompt (Gemma Only)",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = systemPrompt,
                onValueChange = { viewModel.setSystemPrompt(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter a system prompt...") },
                minLines = 3,
                maxLines = 5
            )
            Text(
                "Instructions given to the model at the start of the chat.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Thinking Enabled
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Thinking",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = thinkingEnabled,
                    onCheckedChange = { viewModel.setThinkingEnabled(it) }
                )
            }
            Text(
                "Allows the model to think before responding.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // --- Context Chat Settings ---
            Text(
                "Context Chat Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Context Chat Temperature
            Text(
                text = String.format(
                    Locale.US,
                    "Context Chat Temperature: %.2f",
                    contextChatTemperature
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = contextChatTemperature,
                onValueChange = { viewModel.setContextChatTemperature(it) },
                valueRange = 0.0f..1.0f,
                steps = 19
            )
            Text(
                "Controls randomness for context-aware chats.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Context Chat Top P
            Text(
                text = String.format(Locale.US, "Context Chat Top-P: %.2f", contextChatTopP),
                style = MaterialTheme.typography.bodyMedium,
                color = if (contextChatTemperature > 0f) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            )
            Slider(
                value = contextChatTopP,
                onValueChange = { viewModel.setContextChatTopP(it) },
                valueRange = 0.0f..1.0f,
                steps = 19,
                enabled = contextChatTemperature > 0f
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Context Chat Thinking Enabled
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Thinking in Context Chat",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = contextChatThinkingEnabled,
                    onCheckedChange = { viewModel.setContextChatThinkingEnabled(it) }
                )
            }
            Text(
                "Allows the model to think before responding in context chats.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Context Chat System Prompt
            Text(
                "Context Chat System Prompt",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = contextChatSystemPrompt,
                onValueChange = { viewModel.setContextChatSystemPrompt(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter a system prompt for context chats...") },
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // --- RAG Settings ---
            Text(
                "RAG Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // RAG Temperature
            Text(
                text = String.format(Locale.US, "RAG Temperature: %.2f", ragTemperature),
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = ragTemperature,
                onValueChange = { viewModel.setRagTemperature(it) },
                valueRange = 0.0f..1.0f,
                steps = 19 // increments of 0.05
            )
            Text(
                "Controls randomness for RAG chats. Lower values are more deterministic.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // RAG Top P
            Text(
                text = String.format(Locale.US, "RAG Top-P: %.2f", ragTopP),
                style = MaterialTheme.typography.bodyMedium,
                color = if (ragTemperature > 0f) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            )
            Slider(
                value = ragTopP,
                onValueChange = { viewModel.setRagTopP(it) },
                valueRange = 0.0f..1.0f,
                steps = 19,
                enabled = ragTemperature > 0f
            )
            Text(
                "Nucleus sampling probability for RAG chats. Lower values filter out less likely tokens.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Max Search Steps
            Text(
                text = "Max Search Steps: $maxSearchSteps",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = maxSearchSteps.toFloat(),
                onValueChange = { viewModel.setMaxSearchSteps(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3
            )
            Text(
                "Maximum number of iterative search steps per query.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Max Retrieved Docs
            Text(
                text = "Max Retrieved Documents: $maxRetrievedDocs",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = maxRetrievedDocs.toFloat(),
                onValueChange = { viewModel.setMaxRetrievedDocs(it.toInt()) },
                valueRange = 1f..10f,
                steps = 8
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Min Retrieval Score
            Text(
                text = String.format(Locale.US, "Min Retrieval Score: %.2f", minRetrievalScore),
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = minRetrievalScore,
                onValueChange = { viewModel.setMinRetrievalScore(it) },
                valueRange = 0.0f..1.0f,
                steps = 19
            )
            Text(
                "Reference quality filter. Higher values require better matches.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Max Context Tokens
            Text(text = "Max Context Tokens", style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf(1024, 2048, 4096).forEach { size ->
                    FilterChip(
                        selected = maxContextTokens == size,
                        onClick = { viewModel.setMaxContextTokens(size) },
                        label = { Text(size.toString()) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            Text(
                "Total size of retrieved content sent to the model (in tokens).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // --- Medical Note (SOAP) Settings ---
            Text(
                "Medical Note (SOAP) Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // SOAP Temperature
            Text(
                text = String.format(Locale.US, "SOAP Temperature: %.2f", soapTemperature),
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = soapTemperature,
                onValueChange = { viewModel.setSoapTemperature(it) },
                valueRange = 0.0f..1.0f,
                steps = 19
            )
            Text(
                "Controls randomness for SOAP note generation.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SOAP Top P
            Text(
                text = String.format(Locale.US, "SOAP Top-P: %.2f", soapTopP),
                style = MaterialTheme.typography.bodyMedium,
                color = if (soapTemperature > 0f) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            )
            Slider(
                value = soapTopP,
                onValueChange = { viewModel.setSoapTopP(it) },
                valueRange = 0.0f..1.0f,
                steps = 19,
                enabled = soapTemperature > 0f
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SOAP Thinking Enabled
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Thinking for SOAP",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = soapThinkingEnabled,
                    onCheckedChange = { viewModel.setSoapThinkingEnabled(it) }
                )
            }
            Text(
                "Allows the model to think before generating the SOAP note. Can improve quality.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SOAP System Prompt
            Text(
                "SOAP System Prompt",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = soapSystemPrompt,
                onValueChange = { viewModel.setSoapSystemPrompt(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter a system prompt for SOAP generation...") },
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Reset Button
            Button(
                onClick = { viewModel.resetToDefaults() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset to Defaults")
            }
        }
    }
}
