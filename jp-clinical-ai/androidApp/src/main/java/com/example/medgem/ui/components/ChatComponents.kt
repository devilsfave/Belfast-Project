package com.example.medgem.ui.components

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val TAG = "ChatComponents"
private const val MAX_INPUT_LINES = 3

// --- Data class for image picker state ---

data class ImagePickerState(
    val pickImages: () -> Unit,
    val takePhoto: () -> Unit
)

/**
 * Creates and remembers image picker and camera launcher state.
 * Returns an [ImagePickerState] with lambdas to trigger gallery picker and camera.
 */
@Composable
fun rememberImagePickers(
    selectedImagesState: MutableStateFlow<List<String>>,
    imageLoadErrorState: MutableStateFlow<String?>
): ImagePickerState {
    val context = LocalContext.current
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentCameraPath by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentCameraPath != null) {
            try {
                val sourceFile = File(currentCameraPath!!)
                if (sourceFile.exists()) {
                    // Images are initially saved to cache (staging).
                    // They will be moved to permanent storage by the Repository when the message is sent.
                    val storageDir = File(context.cacheDir, "chat_staging_images")
                    if (!storageDir.exists()) storageDir.mkdirs()

                    val targetFile = File(storageDir, "IMG_${System.currentTimeMillis()}.jpg")
                    sourceFile.copyTo(targetFile, overwrite = true)
                    sourceFile.delete() // Clean up original capture file

                    selectedImagesState.value = selectedImagesState.value + targetFile.absolutePath
                } else {
                    imageLoadErrorState.value = "Failed to capture image"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing camera image", e)
                imageLoadErrorState.value = "Error processing: ${e.message}"
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                val photoFile = createImageFile(context)
                currentCameraPath = photoFile.absolutePath
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating camera file", e)
                imageLoadErrorState.value = "Error starting camera: ${e.message}"
            }
        } else {
            imageLoadErrorState.value = "Camera permission is required to take photos"
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newPaths = mutableListOf<String>()
            var hasError = false
            // Images are initially saved to cache (staging).
            // They will be moved to permanent storage by the Repository when the message is sent.
            val storageDir = File(context.cacheDir, "chat_staging_images")
            if (!storageDir.exists()) storageDir.mkdirs()

            for (uri in uris) {
                try {
                    val targetFile = File(
                        storageDir,
                        "IMG_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
                    )
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    newPaths.add(targetFile.absolutePath)
                } catch (e: Exception) {
                    hasError = true
                    Log.e(TAG, "Error saving image from URI: $uri", e)
                }
            }
            if (newPaths.isNotEmpty()) {
                selectedImagesState.value = selectedImagesState.value + newPaths
                imageLoadErrorState.value = if (hasError) "Some images failed to load" else null
            } else if (hasError) {
                imageLoadErrorState.value = "Failed to load images"
            }
        }
    }

    val takePhoto = {
        val permissionCheckResult = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CAMERA
        )
        if (permissionCheckResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                val photoFile = createImageFile(context)
                currentCameraPath = photoFile.absolutePath
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating camera file", e)
                imageLoadErrorState.value = "Error starting camera: ${e.message}"
            }
        } else {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    val pickImages = {
        galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    return ImagePickerState(pickImages = pickImages, takePhoto = takePhoto)
}

// --- Top Bar ---

@Composable
fun ChatTopBar(
    title: String,
    onBack: () -> Unit,
    onNewChat: () -> Unit,
    newChatEnabled: Boolean
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
        Box(modifier = Modifier.weight(2f), contentAlignment = Alignment.Center) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    ),
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            TextButton(
                onClick = onNewChat,
                modifier = Modifier.semantics {
                    contentDescription = "Start new conversation"
                },
                enabled = newChatEnabled
            ) { Text(text = "New Chat", style = MaterialTheme.typography.labelLarge) }
        }
    }
}

// --- Clear Conversation Dialog ---

@Composable
fun ClearConversationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear Conversation") },
        text = { Text("Are you sure you want to clear all messages?") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("New Chat") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// --- Image Error Banner ---

@Composable
fun ImageErrorBanner(
    errorMessage: String?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = errorMessage != null,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss error",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

// --- Image Preview Row ---

@Composable
fun ImagePreviewRow(
    images: List<String>,
    onRemoveImage: (Int) -> Unit
) {
    AnimatedVisibility(
        visible = images.isNotEmpty(),
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(images) { index, path ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(path)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Selected image ${index + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { onRemoveImage(index) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove image ${index + 1}",
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- Chat Input Bar ---

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit,
    onStartVoiceInput: () -> Unit,
    onStopVoiceInput: () -> Unit,
    isVoiceRecording: Boolean,
    isGenerating: Boolean,
    isModelLoaded: Boolean,
    isRestoring: Boolean,
    visionEnabled: Boolean,
    canSend: Boolean,
    hasImages: Boolean = false,
    isReadOnly: Boolean = false,
    placeholder: String = "Type a message..."
) {
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
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleScale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (visionEnabled) {
            IconButton(
                onClick = onPickImage,
                enabled = isModelLoaded && !isGenerating && !isRestoring && !isVoiceRecording,
                modifier = Modifier.semantics { contentDescription = "Pick image" }
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Add photo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (visionEnabled) {
            IconButton(
                onClick = onTakePhoto,
                enabled = isModelLoaded && !isGenerating && !isRestoring && !isVoiceRecording,
                modifier = Modifier.semantics { contentDescription = "Take photo" }
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Take photo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = "Message input field"
                },
            placeholder = {
                Text(if (isReadOnly) "Read-only mode" else if (isVoiceRecording) "Recording..." else placeholder)
            },
            maxLines = MAX_INPUT_LINES,
            enabled = isModelLoaded && !isGenerating && !isReadOnly && !isVoiceRecording
        )
        Spacer(modifier = Modifier.size(8.dp))

        val hasContent = inputText.isNotBlank() || hasImages

        when {
            isGenerating -> {
                IconButton(
                    onClick = onStop,
                    modifier = Modifier.semantics {
                        contentDescription = "Stop generation"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop generation",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            isVoiceRecording -> {
                Box(contentAlignment = Alignment.Center) {
                    // Pulsing Ripple Effect
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .scale(rippleScale)
                            .background(
                                MaterialTheme.colorScheme.error.copy(alpha = rippleAlpha),
                                CircleShape
                            )
                    )
                    IconButton(
                        onClick = onStopVoiceInput,
                        modifier = Modifier.semantics {
                            contentDescription = "Stop voice input"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop recording",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size((24 * pulseScale).dp)
                        )
                    }
                }
            }

            hasContent -> {
                IconButton(
                    onClick = onSend,
                    enabled = canSend && !isReadOnly,
                    modifier = Modifier.semantics { contentDescription = "Send message" }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message",
                        tint = if (canSend && !isReadOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.38f
                        )
                    )
                }
            }

            else -> {
                IconButton(
                    onClick = onStartVoiceInput,
                    enabled = isModelLoaded && !isRestoring && !isReadOnly,
                    modifier = Modifier.semantics { contentDescription = "Start voice input" }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// --- Typing Indicator ---

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .semantics {
                contentDescription = "Assistant is typing"
            },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha by
            infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation =
                            tween(
                                durationMillis = 600,
                                delayMillis = index * 200
                            ),
                        repeatMode = RepeatMode.Reverse
                    ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = CircleShape
                    )
            )
        }
    }
}

// --- Utility ---

fun createImageFile(context: android.content.Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir = File(context.cacheDir, "camera_images")
    if (!storageDir.exists()) {
        storageDir.mkdirs()
    }
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}

// --- Full Screen Image Dialog ---

@Composable
fun FullScreenImageDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imagePath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Full screen image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close full screen image",
                    tint = Color.White
                )
            }
        }
    }
}
