package com.example.medgem.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.sp
import com.example.medgem.ui.theme.MedGemTheme
import kotlinx.coroutines.launch

@Composable
fun NoteInputScreen(
    onProcessNotes: (hcNumber: String, noteText: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = rememberClinicalInputColors()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var hcNumber by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Surface(
                color = colors.surface,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = "New Clinical Session",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = colors.text,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            val notesFieldHeight = maxHeight * 0.60f

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.Top
            ) {
                OutlinedTextField(
                    value = hcNumber,
                    onValueChange = { hcNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("H&C Number (optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline,
                        focusedLabelColor = colors.primary,
                        unfocusedLabelColor = colors.mutedText,
                        cursorColor = colors.primary,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedContainerColor = colors.fieldBackground,
                        unfocusedContainerColor = colors.fieldBackground
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(notesFieldHeight),
                    placeholder = {
                        Text(
                            text = "Type or speak your patient notes here...\nUse abbreviations as you normally would.\nTLNWL, TSH, MSE, PISANI — the system understands them.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.mutedText,
                                lineHeight = 20.sp
                            )
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = colors.text,
                        lineHeight = 22.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline,
                        cursorColor = colors.primary,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text,
                        focusedContainerColor = colors.fieldBackground,
                        unfocusedContainerColor = colors.fieldBackground
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Voice input coming soon — please type your notes"
                                )
                            }
                        },
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, colors.primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.primary),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Speak notes",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Button(
                        onClick = { onProcessNotes(hcNumber.trim(), noteText.trim()) },
                        enabled = noteText.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = Color.White,
                            disabledContainerColor = colors.disabledButton,
                            disabledContentColor = colors.disabledButtonContent
                        )
                    ) {
                        Text(
                            text = "PROCESS NOTES",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Your notes are processed entirely on this device. Nothing is sent to any server until you approve and manually sync completed forms.",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.mutedText,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

@Composable
private fun rememberClinicalInputColors(): ClinicalInputColors {
    val dark = isSystemInDarkTheme()
    return ClinicalInputColors(
        primary = Color(0xFF00695C),
        background = if (dark) Color(0xFF121212) else Color(0xFFFAFAFA),
        surface = if (dark) Color(0xFF1E1E1E) else Color.White,
        fieldBackground = if (dark) Color(0xFF1E1E1E) else Color.White,
        text = if (dark) Color(0xFFE8E8E8) else Color(0xFF1A1A1A),
        mutedText = if (dark) Color(0xFFB8B8B8) else Color(0xFF666666),
        outline = if (dark) Color(0xFF555555) else Color(0xFFD6D6D6),
        disabledButton = if (dark) Color(0xFF3A3A3A) else Color(0xFFE0E0E0),
        disabledButtonContent = if (dark) Color(0xFF9E9E9E) else Color(0xFF777777)
    )
}

private data class ClinicalInputColors(
    val primary: Color,
    val background: Color,
    val surface: Color,
    val fieldBackground: Color,
    val text: Color,
    val mutedText: Color,
    val outline: Color,
    val disabledButton: Color,
    val disabledButtonContent: Color
)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun NoteInputScreenPreview() {
    MedGemTheme(dynamicColor = false) {
        NoteInputScreen(onProcessNotes = { _, _ -> })
    }
}
