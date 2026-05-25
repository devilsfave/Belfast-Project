package com.example.medgem.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medgem.ui.theme.MedGemTheme

data class ReviewForm(
    val id: String,
    val name: String,
    val fields: List<ReviewField>,
    val requiresSignature: Boolean = false
)

data class ReviewField(
    val id: String,
    val section: String,
    val label: String,
    val value: String?,
    val judgmentRequired: Boolean = false
)

data class ReviewIssue(
    val title: String,
    val detail: String
)

@Composable
fun FormReviewScreen(
    forms: List<ReviewForm> = formReviewPreviewForms(),
    hardBlocks: List<ReviewIssue> = formReviewPreviewHardBlocks(),
    softFlags: List<ReviewIssue> = formReviewPreviewSoftFlags(),
    onSubmitForEpicSync: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = rememberReviewColors()
    val pagerState = rememberPagerState(pageCount = { forms.size + 1 })
    val approvedFormIds = remember(forms) { mutableStateListOf<String>() }
    val fieldValues = remember(forms) {
        mutableStateMapOf<String, String>().apply {
            forms.forEach { form ->
                form.fields.forEach { field ->
                    put(fieldStateKey(form.id, field.id), field.value.orEmpty())
                }
            }
        }
    }
    val signatureStates = remember(forms) { mutableStateMapOf<String, Boolean>() }
    val currentPage = pagerState.currentPage
    val viewingSummary = currentPage == forms.size
    val topBarTitle = if (viewingSummary) "Summary" else forms[currentPage].name
    val topBarCount = if (viewingSummary) "Final review" else "Form ${currentPage + 1} of ${forms.size}"
    val completionProgress = if (forms.isEmpty()) 0f else approvedFormIds.size / forms.size.toFloat()

    Scaffold(
        modifier = modifier.background(colors.background),
        containerColor = colors.background,
        topBar = {
            FormReviewTopBar(
                title = topBarTitle,
                countLabel = topBarCount,
                progress = completionProgress,
                colors = colors
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            val pageAlpha by animateFloatAsState(
                targetValue = if (page == pagerState.currentPage) 1f else 0.88f,
                animationSpec = tween(durationMillis = 200),
                label = "form-page-fade"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(pageAlpha)
            ) {
                if (page < forms.size) {
                    val form = forms[page]
                    ReviewFormPage(
                        form = form,
                        fields = form.fields,
                        fieldValue = { fieldId -> fieldValues[fieldStateKey(form.id, fieldId)].orEmpty() },
                        onFieldValueChange = { fieldId, value ->
                            fieldValues[fieldStateKey(form.id, fieldId)] = value
                        },
                        approved = approvedFormIds.contains(form.id),
                        signatureCaptured = signatureStates[form.id] == true,
                        onSignatureChanged = { signatureStates[form.id] = it },
                        onApprove = {
                            if (!approvedFormIds.contains(form.id)) {
                                approvedFormIds.add(form.id)
                            }
                        },
                        colors = colors
                    )
                } else {
                    SummaryReviewPage(
                        approvedCount = approvedFormIds.size,
                        totalForms = forms.size,
                        hardBlocks = hardBlocks,
                        softFlags = softFlags,
                        onSubmitForEpicSync = onSubmitForEpicSync,
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun FormReviewTopBar(
    title: String,
    countLabel: String,
    progress: Float,
    colors: ReviewColors
) {
    Surface(
        color = colors.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = colors.text
                    )
                )
                Text(
                    text = countLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = colors.mutedText
                    )
                )
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = colors.primary,
                trackColor = colors.outline.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
private fun ReviewFormPage(
    form: ReviewForm,
    fields: List<ReviewField>,
    fieldValue: (String) -> String,
    onFieldValueChange: (String, String) -> Unit,
    approved: Boolean,
    signatureCaptured: Boolean,
    onSignatureChanged: (Boolean) -> Unit,
    onApprove: () -> Unit,
    colors: ReviewColors
) {
    var editing by remember(form.id) { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val sections = fields.groupBy { it.section }
        sections.forEach { (section, sectionFields) ->
            item(key = "${form.id}-$section") {
                Text(
                    text = section,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            sectionFields.forEach { field ->
                item(key = "${form.id}-${field.id}") {
                    ReviewFieldRow(
                        field = field,
                        value = fieldValue(field.id),
                        editing = editing && !approved,
                        onTapMissing = { editing = true },
                        onValueChange = { onFieldValueChange(field.id, it) },
                        colors = colors
                    )
                }
            }
        }

        if (form.requiresSignature) {
            item(key = "${form.id}-signature") {
                SignatureCaptureArea(
                    signatureCaptured = signatureCaptured,
                    onSignatureChanged = onSignatureChanged,
                    colors = colors
                )
            }
        }

        item(key = "${form.id}-actions") {
            FormActionButtons(
                approved = approved,
                editEnabled = !approved,
                approveEnabled = !approved && (!form.requiresSignature || signatureCaptured),
                onEdit = { editing = true },
                onApprove = onApprove,
                colors = colors
            )
        }
    }
}

@Composable
private fun ReviewFieldRow(
    field: ReviewField,
    value: String,
    editing: Boolean,
    onTapMissing: () -> Unit,
    onValueChange: (String) -> Unit,
    colors: ReviewColors
) {
    val missing = value.isBlank()
    val backgroundColor = if (missing) colors.missingBackground else Color.Transparent
    val fieldModifier = Modifier
        .fillMaxWidth()
        .background(backgroundColor, RoundedCornerShape(8.dp))
        .then(
            if (field.judgmentRequired) {
                Modifier.drawBehind {
                    drawRect(
                        color = colors.warning,
                        topLeft = Offset.Zero,
                        size = Size(4.dp.toPx(), size.height)
                    )
                }
            } else {
                Modifier
            }
        )
        .padding(start = if (field.judgmentRequired) 12.dp else 0.dp)
        .padding(horizontal = 10.dp, vertical = 10.dp)

    Column(modifier = fieldModifier) {
        Text(
            text = field.label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.label,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        if (field.judgmentRequired) {
            Text(
                text = "Clinical judgment required — awaiting your input",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = colors.warning,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (editing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.text,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline,
                    cursorColor = colors.primary,
                    focusedTextColor = colors.text,
                    unfocusedTextColor = colors.text
                )
            )
        } else {
            Text(
                text = if (missing) "Not documented — tap to add" else value,
                modifier = if (missing) Modifier.clickable(onClick = onTapMissing) else Modifier,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (missing) colors.error else colors.text,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    fontStyle = if (missing) FontStyle.Italic else FontStyle.Normal
                )
            )
        }
    }
}

@Composable
private fun SignatureCaptureArea(
    signatureCaptured: Boolean,
    onSignatureChanged: (Boolean) -> Unit,
    colors: ReviewColors
) {
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Sign here to confirm you have reviewed and approved this safety plan",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.text,
                    fontWeight = FontWeight.Normal
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(1.dp, colors.outline, RoundedCornerShape(8.dp))
                    .background(colors.signatureBackground, RoundedCornerShape(8.dp))
                    .padding(6.dp)
                    .clickable(enabled = false) {}
                    .then(
                        Modifier.background(Color.Transparent)
                    )
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .clickable(enabled = false) {}
                        .then(
                            Modifier
                        )
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentStroke = listOf(offset)
                                    onSignatureChanged(true)
                                },
                                onDrag = { change, _ ->
                                    currentStroke = currentStroke + change.position
                                },
                                onDragEnd = {
                                    if (currentStroke.isNotEmpty()) {
                                        strokes.add(currentStroke)
                                    }
                                    currentStroke = emptyList()
                                },
                                onDragCancel = {
                                    currentStroke = emptyList()
                                }
                            )
                        }
                ) {
                    val allStrokes = strokes + listOf(currentStroke).filter { it.isNotEmpty() }
                    allStrokes.forEach { stroke ->
                        stroke.zipWithNext { start, end ->
                            drawLine(
                                color = colors.primary,
                                start = start,
                                end = end,
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (signatureCaptured) "Signature captured" else "Signature required",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (signatureCaptured) colors.success else colors.error,
                        fontWeight = FontWeight.Medium
                    )
                )
                TextButton(
                    onClick = {
                        strokes.clear()
                        currentStroke = emptyList()
                        onSignatureChanged(false)
                    }
                ) {
                    Text(
                        text = "CLEAR",
                        color = colors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun FormActionButtons(
    approved: Boolean,
    editEnabled: Boolean,
    approveEnabled: Boolean,
    onEdit: () -> Unit,
    onApprove: () -> Unit,
    colors: ReviewColors
) {
    val approveContainer by animateColorAsState(
        targetValue = when {
            approved -> colors.success
            approveEnabled -> colors.primary
            else -> colors.disabledButton
        },
        animationSpec = tween(durationMillis = 150),
        label = "approve-button-container"
    )
    val approveContent by animateColorAsState(
        targetValue = if (approveEnabled || approved) Color.White else colors.disabledButtonContent,
        animationSpec = tween(durationMillis = 150),
        label = "approve-button-content"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onEdit,
            enabled = editEnabled,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, colors.primary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.primary,
                disabledContentColor = colors.disabledButtonContent
            )
        ) {
            Text(
                text = "EDIT",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
        Button(
            onClick = onApprove,
            enabled = approveEnabled,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = approveContainer,
                contentColor = approveContent,
                disabledContainerColor = approveContainer,
                disabledContentColor = approveContent
            )
        ) {
            if (approved) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "APPROVED",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                )
            } else {
                Text(
                    text = "APPROVE FORM",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

@Composable
private fun SummaryReviewPage(
    approvedCount: Int,
    totalForms: Int,
    hardBlocks: List<ReviewIssue>,
    softFlags: List<ReviewIssue>,
    onSubmitForEpicSync: () -> Unit,
    colors: ReviewColors
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "$approvedCount of $totalForms forms approved",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = colors.text,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { if (totalForms == 0) 0f else approvedCount / totalForms.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.primary,
                        trackColor = colors.outline.copy(alpha = 0.45f)
                    )
                }
            }
        }

        item {
            IssueSection(
                title = "Remaining hard blocks",
                issues = hardBlocks,
                color = colors.error,
                colors = colors
            )
        }

        item {
            IssueSection(
                title = "Soft flags",
                issues = softFlags,
                color = colors.warning,
                colors = colors
            )
        }

        item {
            val submitEnabled = hardBlocks.isEmpty()
            val submitContainer by animateColorAsState(
                targetValue = if (submitEnabled) colors.primary else colors.disabledButton,
                animationSpec = tween(durationMillis = 150),
                label = "epic-sync-container"
            )
            val submitContent by animateColorAsState(
                targetValue = if (submitEnabled) Color.White else colors.disabledButtonContent,
                animationSpec = tween(durationMillis = 150),
                label = "epic-sync-content"
            )
            Button(
                onClick = onSubmitForEpicSync,
                enabled = submitEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = submitContainer,
                    contentColor = submitContent,
                    disabledContainerColor = submitContainer,
                    disabledContentColor = submitContent
                )
            ) {
                Text(
                    text = "SUBMIT FOR EPIC SYNC",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

@Composable
private fun IssueSection(
    title: String,
    issues: List<ReviewIssue>,
    color: Color,
    colors: ReviewColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (issues.isEmpty()) {
                Text(
                    text = "None",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.mutedText,
                        fontWeight = FontWeight.Normal
                    )
                )
            } else {
                issues.forEachIndexed { index, issue ->
                    if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = issue.title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = color,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = issue.detail,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.text,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberReviewColors(): ReviewColors {
    val dark = isSystemInDarkTheme()
    return ReviewColors(
        primary = Color(0xFF00695C),
        background = if (dark) Color(0xFF121212) else Color(0xFFFAFAFA),
        surface = if (dark) Color(0xFF1E1E1E) else Color.White,
        text = if (dark) Color(0xFFE8E8E8) else Color(0xFF1A1A1A),
        label = if (dark) Color(0xFFBDBDBD) else Color(0xFF6B6B6B),
        mutedText = if (dark) Color(0xFFB8B8B8) else Color(0xFF666666),
        outline = if (dark) Color(0xFF555555) else Color(0xFFD6D6D6),
        missingBackground = if (dark) Color(0xFF321B1B) else Color(0xFFFFEBEE),
        signatureBackground = if (dark) Color(0xFF181818) else Color(0xFFFFFFFF),
        disabledButton = if (dark) Color(0xFF3A3A3A) else Color(0xFFE0E0E0),
        disabledButtonContent = if (dark) Color(0xFF9E9E9E) else Color(0xFF777777),
        error = Color(0xFFB71C1C),
        warning = Color(0xFFE65100),
        success = Color(0xFF2E7D32)
    )
}

private data class ReviewColors(
    val primary: Color,
    val background: Color,
    val surface: Color,
    val text: Color,
    val label: Color,
    val mutedText: Color,
    val outline: Color,
    val missingBackground: Color,
    val signatureBackground: Color,
    val disabledButton: Color,
    val disabledButtonContent: Color,
    val error: Color,
    val warning: Color,
    val success: Color
)

private fun fieldStateKey(formId: String, fieldId: String) = "$formId::$fieldId"

private fun formReviewPreviewForms(): List<ReviewForm> = listOf(
    ReviewForm(
        id = "emergency-assessment",
        name = "Emergency Assessment",
        fields = listOf(
            ReviewField("patient-name", "Patient details", "Patient name", "Ciaran O'Donnell"),
            ReviewField("hc-number", "Patient details", "H&C number", "HC200001"),
            ReviewField("assessor", "Assessment", "Assessor", "JP, Mental Health Nurse")
        )
    ),
    ReviewForm(
        id = "confidentiality",
        name = "Confidentiality",
        fields = listOf(
            ReviewField("explained", "Consent", "Confidentiality explained", "Yes"),
            ReviewField("share", "Consent", "Consent to share information", "With wife Nuala")
        )
    ),
    ReviewForm(
        id = "collateral",
        name = "Collateral",
        fields = listOf(
            ReviewField("obtained", "Collateral", "Collateral obtained", "No"),
            ReviewField("reason", "Collateral", "Reason not obtained", "", judgmentRequired = true)
        )
    ),
    ReviewForm(
        id = "hpc",
        name = "Presenting Complaint",
        fields = listOf(
            ReviewField("reason", "History", "Reason for presentation", "Low mood and increased withdrawal"),
            ReviewField("course", "History", "Course and duration", "Three weeks")
        )
    ),
    ReviewForm(
        id = "mse",
        name = "Mental State",
        fields = listOf(
            ReviewField("appearance", "Appearance and behaviour", "Overall narrative", "Kempt, cooperative, maintained eye contact"),
            ReviewField("suicidality", "Suicidality", "TLNWL and TSH", "Denied TLNWL and denied TSH separately")
        )
    ),
    ReviewForm(
        id = "mental-health-history",
        name = "Mental Health History",
        fields = listOf(
            ReviewField("diagnosis", "History", "Diagnosis", "Major depressive disorder"),
            ReviewField("previous-sh", "History", "Previous self-harm", "Superficial lacerations in 2019")
        )
    ),
    ReviewForm(
        id = "personal-history",
        name = "Personal History",
        fields = listOf(
            ReviewField("childhood", "Personal history", "Early childhood", "Normal childhood, no trauma documented"),
            ReviewField("employment", "Personal history", "Employment history", "")
        )
    ),
    ReviewForm(
        id = "family-history",
        name = "Family History",
        fields = listOf(
            ReviewField("living", "Family and home", "Living arrangements", "Lives with wife Nuala"),
            ReviewField("lethal", "Family and home", "Access to lethal means", "No")
        )
    ),
    ReviewForm(
        id = "abuse",
        name = "Abuse Section",
        fields = listOf(
            ReviewField("abuse", "Safeguarding", "Abuse issues identified", "No"),
            ReviewField("vulnerability", "Safeguarding", "Vulnerability identified", "No")
        )
    ),
    ReviewForm(
        id = "offending",
        name = "Offending History",
        fields = listOf(
            ReviewField("weapons", "Weapons", "Access to weapons", "No"),
            ReviewField("licence", "Weapons", "Gun licence", "No")
        )
    ),
    ReviewForm(
        id = "social",
        name = "Social Circumstances",
        fields = listOf(
            ReviewField("housing", "Social", "Housing", "Owner-occupied"),
            ReviewField("support", "Social", "Support network", "Wife Nuala and adult children")
        )
    ),
    ReviewForm(
        id = "medications",
        name = "Medication",
        fields = listOf(
            ReviewField("sertraline", "Current medication", "Sertraline", "100mg once daily"),
            ReviewField("compliance", "Medication autonomy", "Compliance issues", "Good compliance")
        )
    ),
    ReviewForm(
        id = "occupational",
        name = "Occupational Needs",
        fields = listOf(
            ReviewField("needs", "Occupational needs", "Free text", "")
        )
    ),
    ReviewForm(
        id = "substance",
        name = "Substance Misuse",
        fields = listOf(
            ReviewField("alcohol", "Alcohol", "Current alcohol use", "Not currently using alcohol"),
            ReviewField("drugs", "Drugs", "Current drug use", "No current drug use")
        )
    ),
    ReviewForm(
        id = "audit",
        name = "AUDIT",
        fields = listOf(
            ReviewField("score", "Alcohol screening", "Total score", "0"),
            ReviewField("risk", "Alcohol screening", "Risk band", "Lower risk")
        )
    ),
    ReviewForm(
        id = "ldq",
        name = "LDQ",
        fields = listOf(
            ReviewField("score", "Dependence screening", "Total score", "0"),
            ReviewField("cat", "Dependence screening", "CAT referral indicated", "No")
        )
    ),
    ReviewForm(
        id = "child-protection",
        name = "Child Protection",
        fields = listOf(
            ReviewField("children", "Children", "Children in regular contact", "No"),
            ReviewField("unocini", "UNOCINI", "Referral status", "Not indicated")
        )
    ),
    ReviewForm(
        id = "pisani",
        name = "PISANI Risk Assessment",
        fields = listOf(
            ReviewField("strengths", "Risk formulation", "Strengths and protective factors", "Family support and future orientation"),
            ReviewField("impulsivity", "Risk formulation", "Impulsivity and self-control", "", judgmentRequired = true),
            ReviewField("risk-level", "Risk formulation", "Overall risk level", "Low")
        )
    ),
    ReviewForm(
        id = "safety-plan",
        name = "Safety Plan",
        requiresSignature = true,
        fields = listOf(
            ReviewField("step1", "Stanley-Brown plan", "Step 1 warning signs", "Mood dropping in evenings; cancelling family calls; poor sleep"),
            ReviewField("step4", "Stanley-Brown plan", "Step 4 people to ask for help", "Wife Nuala, 07700900101"),
            ReviewField("step6", "Stanley-Brown plan", "Step 6 making environment safe", "Wife Nuala stores medications; no weapons in home")
        )
    ),
    ReviewForm(
        id = "epic-note",
        name = "Epic Contact Note",
        fields = listOf(
            ReviewField("contact", "Epic note", "Contact details and overview", "Patient reviewed at home. Cooperative and engaged."),
            ReviewField("plan", "Epic note", "Plan", "Continue current plan, HTT follow-up call arranged")
        )
    )
)

private fun formReviewPreviewHardBlocks(): List<ReviewIssue> = listOf(
    ReviewIssue(
        title = "Safety Plan signature missing",
        detail = "The Safety Plan must be signed before final submission."
    )
)

private fun formReviewPreviewSoftFlags(): List<ReviewIssue> = listOf(
    ReviewIssue(
        title = "Collateral reason awaiting review",
        detail = "Collateral was not obtained and the documented reason requires nurse confirmation."
    )
)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FormReviewScreenPreview() {
    MedGemTheme(dynamicColor = false) {
        FormReviewScreen()
    }
}
