package com.example.medgem.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.medgem.ui.components.MedGemTopBar

@Composable
fun ModelDownloadScreen(
    onDownloadCompleted: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: ModelDownloadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    DisposableEffect(uiState.isDownloading) {
        val window = (context as? Activity)?.window
        if (uiState.isDownloading) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Scaffold(
        topBar = { MedGemTopBar(title = "Download Models", onBack = onBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Required Models",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Start)
            )

            Text(
                text = "Please download the following models to use the application. High-speed internet is recommended.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )



            var expanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.selectedContextLength.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Context Length") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Context Length"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isDownloading,
                    singleLine = true
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(enabled = !uiState.isDownloading) { expanded = true }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    uiState.availableContextLengths.forEach { length ->
                        DropdownMenuItem(
                            text = { Text(length.toString()) },
                            onClick = {
                                viewModel.updateContextLength(length)
                                expanded = false
                            }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val groupedFiles = uiState.files.groupBy { it.groupName }

                groupedFiles.forEach { (groupName, files) ->
                    item {
                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(files) { file ->
                        // Find original index in the main list
                        val index = uiState.files.indexOf(file)
                        DownloadItem(
                            file = file,
                            onDelete = { viewModel.deleteFile(index) },
                            isDownloading = uiState.isDownloading
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isAllCompleted) {
                Button(
                    onClick = onDownloadCompleted,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue")
                }
            } else {
                Button(
                    onClick = { viewModel.startDownload() },
                    enabled = !uiState.isDownloading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Downloading...")
                    } else {
                        Text("Start Download")
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadItem(
    file: DownloadableFile,
    onDelete: () -> Unit,
    isDownloading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (file.error != null) {
                    Text(
                        text = "Error: ${file.error}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (file.status == DownloadStatus.DOWNLOADING) {
                    val downloadedMb = file.downloadedBytes / (1024 * 1024)
                    val progressText =
                        if (file.progress >= 0) "${file.progress.toInt()}%" else "Downloaded $downloadedMb MB"
                    Text(
                        text = progressText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            when (file.status) {
                DownloadStatus.COMPLETED -> {
                    IconButton(
                        onClick = onDelete,
                        enabled = !isDownloading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Redownload",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                DownloadStatus.ERROR -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                else -> {
                    // Pending or Downloading - handled by progress bar below or empty
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (file.status == DownloadStatus.DOWNLOADING) {
            if (file.progress >= 0) {
                LinearProgressIndicator(
                    progress = { file.progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else if (file.status == DownloadStatus.PENDING) {
            LinearProgressIndicator(
                progress = { 0f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else if (file.status == DownloadStatus.COMPLETED) {
            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxWidth(),
                color = Color.Green
            )
        }
    }
}
