package com.example.medgem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun DisclaimerScreen(
    onDisclaimerAccepted: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Disclaimer",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(24.dp))

            val linkStyles = TextLinkStyles(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )

            val annotatedString = buildAnnotatedString {
                append("This is a demo app built for the ")

                withLink(
                    LinkAnnotation.Url(
                        url = "https://www.kaggle.com/competitions/med-gemma-impact-challenge",
                        styles = linkStyles
                    )
                ) {
                    append("Med Gemma Impact Challenge")
                }

                append(".\n\nIt is ")
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                ) {
                    append("NOT validated")
                }
                append(" for clinical use and should not be used for medical decision making.")

                append("\n\nThis app uses ")
                withLink(
                    LinkAnnotation.Url(
                        url = "https://developers.google.com/health-ai-developer-foundations/terms",
                        styles = linkStyles
                    )
                ) {
                    append("MedGemma and MedASR")
                }
                append(" under the Health AI Developer Foundations terms.")

                append("\n\nIt also uses ")
                withLink(
                    LinkAnnotation.Url(
                        url = "https://ai.google.dev/gemma/terms",
                        styles = linkStyles
                    )
                ) {
                    append("Embedding Gemma")
                }
                append(" under its license terms.")
            }

            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onDisclaimerAccepted,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("I Understand")
            }
        }
    }
}
