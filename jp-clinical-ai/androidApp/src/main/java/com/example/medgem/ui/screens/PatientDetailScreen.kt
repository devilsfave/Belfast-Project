package com.example.medgem.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.medgem.data.ConversationEntity
import com.example.medgem.data.ConversationType
import com.example.medgem.data.PatientEntity
import com.example.medgem.data.VisitEntity
import com.example.medgem.ui.components.PatientFormDialog
import com.example.medgem.ui.components.MedGemTopBar
import com.example.medgem.ui.viewmodel.PatientViewModel
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText

import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    patientId: Long,
    onBack: () -> Unit,
    onNavigateToVisit: (Long, Long) -> Unit,
    onNavigateToChat: (Long?, Long?, String?) -> Unit,
    onNavigateToRagChat: (Long?, Long?) -> Unit
) {
    val viewModel: PatientViewModel = hiltViewModel()
    val visits by viewModel.getPatientVisits(patientId).collectAsState()
    val conversations by viewModel.getPatientConversations(patientId).collectAsState()
    var patient by remember { mutableStateOf<PatientEntity?>(null) }
    var showDeletePatientDialog by remember { mutableStateOf(false) }
    var visitToDelete by remember { mutableStateOf<VisitEntity?>(null) }
    var conversationToDelete by remember { mutableStateOf<ConversationEntity?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedVisitIds by remember { mutableStateOf(setOf<Long>()) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var activeTab by rememberSaveable { mutableStateOf(0) } // 0: Visits, 1: AI Chats
    var showVisitSelectionDialog by remember { mutableStateOf(false) }

    var showEditPatientDialog by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) { patient = viewModel.getPatient(patientId) }

    if (showEditPatientDialog && patient != null) {
        PatientFormDialog(
            initialPatient = patient,
            onDismiss = { showEditPatientDialog = false },
            onConfirm = { name, age, gender, allergies, conditions, medications ->
                val updatedPatient = patient!!.copy(
                    name = name,
                    age = age,
                    gender = gender,
                    allergies = allergies,
                    chronicConditions = conditions,
                    currentMedications = medications
                )
                viewModel.updatePatient(updatedPatient)
                patient = updatedPatient
                showEditPatientDialog = false
            }
        )
    }

    if (showVisitSelectionDialog) {
        VisitSelectionDialog(
            visits = visits,
            onDismiss = { showVisitSelectionDialog = false },
            onConfirm = { ids ->
                showVisitSelectionDialog = false
                if (ids.isNotEmpty()) {
                    val vIds = visits.filter { it.id in ids }
                        .sortedBy { it.date }
                        .joinToString(",") { it.id.toString() }
                    onNavigateToChat(null, patientId, vIds)
                }
            }
        )
    }

    if (showDeletePatientDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePatientDialog = false },
            title = { Text(text = "Delete Patient") },
            text = {
                Text(
                    text =
                        "Are you sure you want to delete this patient? All associated visits and data will be permanently removed."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeletePatientDialog = false
                        scope.launch {
                            try {
                                viewModel.deletePatient(patientId)
                                onBack()
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Failed to delete patient")
                            }
                        }
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePatientDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (visitToDelete != null) {
        AlertDialog(
            onDismissRequest = { visitToDelete = null },
            title = { Text(text = "Delete Visit") },
            text = { Text(text = "Are you sure you want to delete this visit?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val v = visitToDelete
                        visitToDelete = null
                        if (v != null) {
                            scope.launch {
                                try {
                                    viewModel.deleteVisit(v.id)
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Failed to delete visit")
                                }
                            }
                        }
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { visitToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (conversationToDelete != null) {
        AlertDialog(
            onDismissRequest = { conversationToDelete = null },
            title = { Text(text = "Delete Conversation") },
            text = { Text(text = "Are you sure you want to delete this conversation?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val c = conversationToDelete
                        conversationToDelete = null
                        if (c != null) {
                            scope.launch {
                                try {
                                    viewModel.deleteConversation(c.id)
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Failed to delete conversation")
                                }
                            }
                        }
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { conversationToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            MedGemTopBar(
                title =
                    if (isSelectionMode) "${selectedVisitIds.size} Selected"
                    else (patient?.name ?: "Patient Details"),
                onBack =
                    if (isSelectionMode) {
                        {
                            isSelectionMode = false
                            selectedVisitIds = emptySet()
                        }
                    } else onBack,
                centerTitle = false,
                navigationIcon = if (isSelectionMode) Icons.Default.Close else null,
                actions = {
                    if (isSelectionMode) {
                        IconButton(
                            onClick = {
                                val vIds = visits.filter { it.id in selectedVisitIds }
                                    .sortedBy { it.date }
                                    .joinToString(",") { it.id.toString() }
                                onNavigateToChat(null, patientId, vIds)
                                isSelectionMode = false
                                selectedVisitIds = emptySet()
                            }
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Analyze Selected"
                            )
                        }
                    } else {
                        IconButton(onClick = { showEditPatientDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Patient",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showDeletePatientDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Patient",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!isSelectionMode) {
                if (activeTab == 0) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                val visitId = viewModel.createVisit(patientId)
                                onNavigateToVisit(visitId, patientId)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) { Icon(Icons.Default.Add, contentDescription = "New Visit") }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FloatingActionButton(
                            onClick = { showVisitSelectionDialog = true },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = "Chat with Context"
                            )
                        }
                        FloatingActionButton(
                            onClick = { onNavigateToRagChat(null, patientId) },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "RAG Chat"
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            patient?.let { PatientInfoSection(it) }

            PrimaryTabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = {
                        activeTab = 0
                        isSelectionMode = false
                        selectedVisitIds = emptySet()
                    },
                    text = { Text("Visits") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = {
                        activeTab = 1
                        isSelectionMode = false
                        selectedVisitIds = emptySet()
                    },
                    text = { Text("AI Chats") }
                )
            }

            if (activeTab == 0) {
                if (visits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(bottom = 16.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant
                            )
                            Text(
                                "No visits recorded yet.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Tap + to start a new visit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(visits) { visit ->
                            val isSelected = selectedVisitIds.contains(visit.id)
                            VisitCard(
                                visit = visit,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedVisitIds =
                                            if (isSelected) {
                                                selectedVisitIds - visit.id
                                            } else {
                                                selectedVisitIds + visit.id
                                            }
                                        if (selectedVisitIds.isEmpty()) isSelectionMode = false
                                    } else {
                                        onNavigateToVisit(visit.id, patientId)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedVisitIds = setOf(visit.id)
                                    }
                                },
                                onDelete = { visitToDelete = visit }
                            )
                        }
                    }
                }
            } else {
                if (conversations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(bottom = 16.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant
                            )
                            Text(
                                "No AI conversations yet.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Start a chat to discuss patient data",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(conversations) { conversation ->
                            PatientConversationCard(
                                conversation = conversation,
                                onClick = {
                                    if (conversation.getType() == ConversationType.RAG) {
                                        onNavigateToRagChat(conversation.id, patientId)
                                    } else {
                                        onNavigateToChat(conversation.id, patientId, null)
                                    }
                                },
                                onDelete = { conversationToDelete = conversation }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatientInfoSection(patient: PatientEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.padding(bottom = 8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Age",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = "${patient.age} years", style = MaterialTheme.typography.bodyLarge)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gender",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = patient.gender, style = MaterialTheme.typography.bodyLarge)
                }
            }

            if (patient.allergies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Allergies",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = patient.allergies.joinToString(", "),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (patient.chronicConditions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chronic Conditions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = patient.chronicConditions.joinToString(", "),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (patient.currentMedications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Current Medications",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = patient.currentMedications.joinToString(", "),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val dateStr =
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(Date(patient.createdAt))

            Text(
                text = "Registered: $dateStr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VisitCard(
    visit: VisitEntity,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = 0.5f
                        )
                    else MaterialTheme.colorScheme.surfaceContainer
            ),
        elevation =
            CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 12.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                val dateStr =
                    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        .format(Date(visit.date))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateStr,
                        style =
                            MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                        modifier = Modifier.weight(1f)
                    )
                    StatusChip(status = visit.getStatus())
                    if (!isSelectionMode) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(start = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Visit",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val summary =
                    if (visit.generatedNote.isNotBlank()) {
                        "**SOAP Note Generated**"
                    } else if (visit.rawTranscript.isNotBlank()) {
                        "**Transcript Generated**"
                    } else {
                        if (visit.getStatus() ==
                            com.example.medgem.data.VisitStatus.RECORDING
                        ) {
                            "Tap to start recording"
                        } else {
                            "Status: ${visit.getStatus()}"
                        }
                    }

                RichText {
                    Markdown(content = summary)
                }
            }
        }
    }
}

@Composable
fun PatientConversationCard(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable(onClick = onClick),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val icon =
                if (conversation.getType() == ConversationType.RAG) {
                    Icons.Default.AutoAwesome
                } else {
                    Icons.AutoMirrored.Filled.Chat
                }
            val tint =
                if (conversation.getType() == ConversationType.RAG) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary
                }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.title.ifBlank { "Untitled Conversation" },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
                val dateStr =
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(conversation.updatedAt))
                Text(
                    text = "Last active: $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Chat",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: com.example.medgem.data.VisitStatus) {
    val (color, text) =
        when (status) {
            com.example.medgem.data.VisitStatus.RECORDING ->
                MaterialTheme.colorScheme.error to "Ready to Record"

            com.example.medgem.data.VisitStatus.TRANSCRIBED ->
                MaterialTheme.colorScheme.primary to "Transcribed"

            com.example.medgem.data.VisitStatus.PROCESSED ->
                MaterialTheme.colorScheme.tertiary to "Processed"
        }

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun VisitSelectionDialog(
    visits: List<VisitEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Long>) -> Unit
) {
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    val visitsWithContent =
        remember(visits) {
            visits.filter { it.generatedNote.isNotBlank() || it.rawTranscript.isNotBlank() }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Select Visits for Context") },
        text = {
            if (visitsWithContent.isEmpty()) {
                Text("No visits with transcripts or notes available.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(visitsWithContent) { visit ->
                        val isSelected = selectedIds.contains(visit.id)
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedIds =
                                            if (isSelected) {
                                                selectedIds - visit.id
                                            } else {
                                                selectedIds + visit.id
                                            }
                                    }
                                    .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    selectedIds =
                                        if (isSelected) {
                                            selectedIds - visit.id
                                        } else {
                                            selectedIds + visit.id
                                        }
                                }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                val dateStr =
                                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                        .format(Date(visit.date))
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (visit.generatedNote.isNotBlank()) {
                                    Text(
                                        text =
                                            visit.generatedNote
                                                .take(50)
                                                .replace("\n", " ") + "...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedIds) },
                enabled = visitsWithContent.isNotEmpty() && selectedIds.isNotEmpty()
            ) { Text("Analyze") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
