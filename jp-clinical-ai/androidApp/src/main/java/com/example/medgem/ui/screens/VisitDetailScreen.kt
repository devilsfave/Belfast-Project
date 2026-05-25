package com.example.medgem.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.medgem.parseThinkingContent
import com.example.medgem.ui.components.MedGemTopBar
import com.example.medgem.ui.components.ThinkingBlock
import com.example.medgem.ui.viewmodel.VisitViewModel
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitDetailScreen(
    visitId: Long,
    patientId: Long,
    onBack: () -> Unit
) {
    val viewModel: VisitViewModel = hiltViewModel()
    val context = LocalContext.current

    val patient by viewModel.patient.collectAsState()
    val visit by viewModel.currentVisit.collectAsState()
    val currentVisit = visit
    val isRecording by viewModel.isRecording.collectAsState()
    val isTranscribing by viewModel.isTranscribing.collectAsState()
    val isGenerating by viewModel.isGeneratingNote.collectAsState()
    val transcript by viewModel.transcriptText.collectAsState()
    val note by viewModel.generatedNote.collectAsState()
    val imagePaths by viewModel.imagePaths.collectAsState()

    // Image Picker State
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.saveCameraImage(visitId)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImagesFromUris(uris)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val uri = viewModel.getTempImageUri()
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                android.util.Log.e("VisitDetailScreen", "Error launching camera", e)
                android.widget.Toast.makeText(
                    context,
                    "Failed to launch camera",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            android.widget.Toast.makeText(
                context,
                "Camera permission needed",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Audio Player State
    var isPlaying by remember { mutableStateOf(false) }
    var isPreparing by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(visitId, patientId) {
        viewModel.loadVisit(visitId, patientId)
    }

    // Date Picker Logic
    val calendar = java.util.Calendar.getInstance()
    currentVisit?.let { calendar.timeInMillis = it.date }
    
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            viewModel.updateDate(calendar.timeInMillis)
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )

    // Release MediaPlayer on dispose
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.startVoiceInput()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.processAudioFile(it) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        topBar = {
            MedGemTopBar(
                title = patient?.name ?: "Visit Details",
                onBack = onBack,
                centerTitle = false
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Date Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateStr = currentVisit?.let {
                    java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault())
                        .format(java.util.Date(it.date))
                } ?: ""
                
                TextButton(onClick = { datePickerDialog.show() }) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Edit Date",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr.ifEmpty { "Set Date" },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Audio Player if file exists
            val audioPath = currentVisit?.audioFilePath
            if (!audioPath.isNullOrEmpty() && java.io.File(audioPath).exists()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    mediaPlayer?.pause()
                                    isPlaying = false
                                } else if (!isPreparing) {
                                    if (mediaPlayer == null) {
                                        isPreparing = true
                                        mediaPlayer =
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                                android.media.MediaPlayer(
                                                    context.createAttributionContext(
                                                        "media_playback"
                                                    )
                                                )
                                            } else {
                                                android.media.MediaPlayer()
                                            }
                                        mediaPlayer!!.apply {
                                            setAudioAttributes(
                                                android.media.AudioAttributes.Builder()
                                                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                                                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                                                    .build()
                                            )
                                            setDataSource(audioPath)
                                            setOnPreparedListener { mp ->
                                                isPreparing = false
                                                mp.start()
                                                isPlaying = true
                                            }
                                            setOnCompletionListener {
                                                isPlaying = false
                                                it.seekTo(0)
                                            }
                                            prepareAsync()
                                        }
                                    } else {
                                        mediaPlayer?.start()
                                        isPlaying = true
                                    }
                                }
                            },
                            enabled = !isPreparing,
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            if (isPreparing) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Play Recording", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Recording Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (isRecording) {
                            viewModel.stopVoiceInput()
                        } else {
                            // Release existing player since audio will be replaced
                            mediaPlayer?.release()
                            mediaPlayer = null
                            isPlaying = false
                            val permissionCheck =
                                androidx.core.content.ContextCompat.checkSelfPermission(
                                    context, android.Manifest.permission.RECORD_AUDIO
                                )
                            if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                viewModel.startVoiceInput()
                            } else {
                                permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .scale(if (isRecording) pulseScale else 1f)
                        .background(
                            if (isRecording) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Record",
                        tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.size(24.dp))

                IconButton(
                    onClick = {
                        // Release existing player since audio will be replaced
                        mediaPlayer?.release()
                        mediaPlayer = null
                        isPlaying = false
                        filePickerLauncher.launch(arrayOf("audio/wav", "audio/x-wav"))
                    },
                    enabled = !isRecording && !isTranscribing && !isGenerating,
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                ) {
                    Icon(
                        Icons.Default.AudioFile,
                        contentDescription = "Upload Audio",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Text(
                text = if (isRecording) "Recording..." else if (isTranscribing) "Transcribing..." else "Tap to record or upload audio",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Images Section
            if (showImageSourceDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showImageSourceDialog = false },
                    title = { Text("Add Image") },
                    text = { Text("Choose an image source") },
                    confirmButton = {
                        TextButton(onClick = {
                            showImageSourceDialog = false
                            val permissionCheck =
                                androidx.core.content.ContextCompat.checkSelfPermission(
                                    context, android.Manifest.permission.CAMERA
                                )
                            if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                try {
                                    val uri = viewModel.getTempImageUri()
                                    cameraLauncher.launch(uri)
                                } catch (e: Exception) {
                                    android.util.Log.e(
                                        "VisitDetailScreen",
                                        "Error launching camera",
                                        e
                                    )
                                    android.widget.Toast.makeText(
                                        context,
                                        "Failed to launch camera",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        }) {
                            Text("Camera")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showImageSourceDialog = false
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }) {
                            Text("Gallery")
                        }
                    }
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Images", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add Image Button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.size(80.dp),
                        onClick = { showImageSourceDialog = true },
                        enabled = !isRecording && !isTranscribing && !isGenerating
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Image",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Image List
                    imagePaths.forEach { path ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.size(80.dp)
                        ) {
                            coil.compose.AsyncImage(
                                model = path,
                                contentDescription = "Medical Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            // Delete Button
                            IconButton(
                                onClick = { viewModel.removeImage(path) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        CircleShape
                                    )
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Transcript Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Live Transcript", style = MaterialTheme.typography.titleSmall)
                        if (transcript.isNotEmpty()) {
                            TextButton(onClick = {
                                mediaPlayer?.release()
                                mediaPlayer = null
                                isPlaying = false
                                viewModel.clearAll()
                            }) {
                                Text("Clear", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    androidx.compose.material3.OutlinedTextField(
                        value = transcript,
                        onValueChange = { viewModel.updateTranscriptText(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 300.dp),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        placeholder = { Text("No transcript yet.") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.generateMedicalNote() },
                enabled = transcript.isNotBlank() && !isGenerating && !isRecording,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Medical Note (SOAP)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Generated Note Section
            if (isGenerating || note.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Structured Medical Note", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isGenerating && note.isEmpty()) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        val (thought, actualContent) = parseThinkingContent(note)
                        if (thought.isNotBlank()) {
                            ThinkingBlock(
                                thought = thought,
                                messageId = visitId,
                                initiallyExpanded = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (actualContent.isNotEmpty()) {
                            RichText {
                                Markdown(actualContent)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

