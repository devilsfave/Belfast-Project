package com.example.medgem.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medgem.ui.theme.MedGemTheme

data class ClarificationQueueItem(
    val id: String,
    val formName: String,
    val question: String,
    val mandatory: Boolean
)

@Composable
fun ClarificationQueueScreen(
    items: List<ClarificationQueueItem> = clarificationQueuePreviewItems(),
    onSubmit: (answers: Map<String, String>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = rememberQueueColors()
    val sortedItems = remember(items) {
        items.sortedWith(
            compareBy<ClarificationQueueItem> { !it.mandatory }
                .thenBy { it.formName }
                .thenBy { it.id }
        )
    }
    val pages = remember(sortedItems) { sortedItems.chunked(20).ifEmpty { listOf(emptyList()) } }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val answers = remember(items) {
        mutableStateMapOf<String, String>().apply {
            items.forEach { put(it.id, "") }
        }
    }
    val mandatoryComplete = sortedItems
        .filter { it.mandatory }
        .all { answers[it.id].orEmpty().isNotBlank() }
    val buttonContainer by animateColorAsState(
        targetValue = if (mandatoryComplete) colors.primary else colors.disabledButton,
        animationSpec = tween(durationMillis = 150),
        label = "clarification-submit-container"
    )
    val buttonContent by animateColorAsState(
        targetValue = if (mandatoryComplete) Color.White else colors.disabledButtonContent,
        animationSpec = tween(durationMillis = 150),
        label = "clarification-submit-content"
    )

    Scaffold(
        modifier = modifier.background(colors.background),
        containerColor = colors.background,
        topBar = {
            ReviewRequiredTopBar(
                fieldCount = sortedItems.size,
                colors = colors
            )
        },
        bottomBar = {
            Surface(
                color = colors.surface,
                shadowElevation = 2.dp
            ) {
                Button(
                    onClick = { onSubmit(answers.toMap()) },
                    enabled = mandatoryComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonContainer,
                        contentColor = buttonContent,
                        disabledContainerColor = buttonContainer,
                        disabledContentColor = buttonContent
                    )
                ) {
                    Text(
                        text = "CONFIRM AND CONTINUE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = pages[page],
                        key = { it.id }
                    ) { item ->
                        ClarificationCard(
                            item = item,
                            answer = answers[item.id].orEmpty(),
                            onAnswerChange = { answers[item.id] = it },
                            colors = colors
                        )
                    }
                }
            }

            if (pages.size > 1) {
                QueuePageIndicator(
                    pageCount = pages.size,
                    currentPage = pagerState.currentPage,
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.background)
                        .padding(bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ReviewRequiredTopBar(
    fieldCount: Int,
    colors: QueueColors
) {
    Surface(
        color = colors.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Review Required",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = colors.text
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$fieldCount fields need your input before this session can be completed",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal,
                    color = colors.mutedText
                )
            )
        }
    }
}

@Composable
private fun ClarificationCard(
    item: ClarificationQueueItem,
    answer: String,
    onAnswerChange: (String) -> Unit,
    colors: QueueColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.formName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.primary,
                        fontWeight = FontWeight.Medium
                    )
                )
                if (item.mandatory) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = "MANDATORY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = colors.error.copy(alpha = 0.10f),
                            labelColor = colors.error
                        ),
                        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.35f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.question,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.text
                )
            )
            Spacer(modifier = Modifier.height(14.dp))
            TextField(
                value = answer,
                onValueChange = onAnswerChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Your clinical assessment...",
                        fontStyle = FontStyle.Normal
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal,
                    color = colors.text
                ),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.fieldBackground,
                    unfocusedContainerColor = colors.fieldBackground,
                    disabledContainerColor = colors.fieldBackground,
                    focusedIndicatorColor = colors.primary,
                    unfocusedIndicatorColor = colors.outline,
                    cursorColor = colors.primary,
                    focusedPlaceholderColor = colors.mutedText,
                    unfocusedPlaceholderColor = colors.mutedText
                )
            )
        }
    }
}

@Composable
private fun QueuePageIndicator(
    pageCount: Int,
    currentPage: Int,
    colors: QueueColors,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (index == currentPage) 9.dp else 7.dp)
                    .background(
                        color = if (index == currentPage) colors.primary else colors.outline,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun rememberQueueColors(): QueueColors {
    val dark = isSystemInDarkTheme()
    return QueueColors(
        primary = Color(0xFF00695C),
        background = if (dark) Color(0xFF121212) else Color(0xFFFAFAFA),
        surface = if (dark) Color(0xFF1E1E1E) else Color.White,
        text = if (dark) Color(0xFFE8E8E8) else Color(0xFF1A1A1A),
        mutedText = if (dark) Color(0xFFB8B8B8) else Color(0xFF666666),
        outline = if (dark) Color(0xFF555555) else Color(0xFFD6D6D6),
        fieldBackground = if (dark) Color(0xFF242424) else Color(0xFFFFFFFF),
        disabledButton = if (dark) Color(0xFF3A3A3A) else Color(0xFFE0E0E0),
        disabledButtonContent = if (dark) Color(0xFF9E9E9E) else Color(0xFF777777),
        error = Color(0xFFB71C1C)
    )
}

private data class QueueColors(
    val primary: Color,
    val background: Color,
    val surface: Color,
    val text: Color,
    val mutedText: Color,
    val outline: Color,
    val fieldBackground: Color,
    val disabledButton: Color,
    val disabledButtonContent: Color,
    val error: Color
)

private fun clarificationQueuePreviewItems() = listOf(
    ClarificationQueueItem(
        id = "mental-state-tlnwl-tsh",
        mandatory = true,
        formName = "Mental State",
        question = "Describe the patient's response when directly asked about TLNWL and TSH. Were these addressed separately in your assessment?"
    ),
    ClarificationQueueItem(
        id = "pisani-impulsivity",
        mandatory = true,
        formName = "PISANI",
        question = "Rate this patient's level of impulsivity and self-control based on your clinical observation during this visit."
    ),
    ClarificationQueueItem(
        id = "collateral-declined-reason",
        mandatory = false,
        formName = "Collateral",
        question = "What was the documented reason collateral information was not obtained for this patient visit?"
    )
)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ClarificationQueueScreenPreview() {
    MedGemTheme(dynamicColor = false) {
        ClarificationQueueScreen(
            items = clarificationQueuePreviewItems(),
            onSubmit = {}
        )
    }
}
