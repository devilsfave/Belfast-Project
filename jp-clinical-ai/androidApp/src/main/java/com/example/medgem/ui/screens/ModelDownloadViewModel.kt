package com.example.medgem.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

data class DownloadableFile(
    val name: String,
    val groupName: String,
    val url: String,
    val destinationPath: String,
    var progress: Float = 0f,
    var downloadedBytes: Long = 0L,
    var status: DownloadStatus = DownloadStatus.PENDING,
    var error: String? = null,
    val contextLength: Int? = null
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    ERROR
}

data class ModelDownloadUiState(
    val files: List<DownloadableFile>,
    val isDownloading: Boolean = false,
    val isAllCompleted: Boolean = false,
    val selectedContextLength: Int = 8192,
    val availableContextLengths: List<Int> = listOf(2048, 4096, 8192, 16384, 32768, 65536)
)

@HiltViewModel
class ModelDownloadViewModel @Inject constructor(
    private val application: Application
) : AndroidViewModel(application) {

    private val TAG = "ModelDownloadViewModel"

    private val _uiState = MutableStateFlow(
        ModelDownloadUiState(
            files = getFilesForContextLength(8192)
        )
    )
    val uiState: StateFlow<ModelDownloadUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            checkExistingFiles()
        }
    }

    private fun checkExistingFiles() {
        val currentFiles = _uiState.value.files
        val updatedFiles = currentFiles.map { file ->
            val destinationFile = File(application.filesDir, file.destinationPath)
            val markerFile = File(destinationFile.absolutePath + ".complete")

            var isCompleted = false
            if (destinationFile.exists() && destinationFile.length() > 0 && markerFile.exists()) {
                if (file.contextLength != null) {
                    // Check if the downloaded model matches the required context length
                    try {
                        val savedLength = markerFile.readText().trim().toIntOrNull()
                        // If savedLength is null (legacy empty marker), we assume it might be wrong if we want strictness.
                        // To be safe and fix the ambiguity, strictly require match if contextLength is specified.
                        if (savedLength == file.contextLength) {
                            isCompleted = true
                        }
                    } catch (e: Exception) {
                        // Error reading marker, assume not completed
                    }
                } else {
                    isCompleted = true
                }
            }

            if (isCompleted) {
                file.copy(
                    status = DownloadStatus.COMPLETED,
                    progress = 100f,
                    downloadedBytes = destinationFile.length()
                )
            } else {
                file
            }
        }

        val allCompleted = updatedFiles.all { it.status == DownloadStatus.COMPLETED }

        _uiState.update {
            it.copy(
                files = updatedFiles,
                isAllCompleted = allCompleted
            )
        }
    }



    fun deleteFile(fileIndex: Int) {
        if (_uiState.value.isDownloading) return

        val file = _uiState.value.files[fileIndex]
        val destinationFile = File(application.filesDir, file.destinationPath)
        val markerFile = File(destinationFile.absolutePath + ".complete")

        if (destinationFile.exists()) destinationFile.delete()
        if (markerFile.exists()) markerFile.delete()

        updateFileStatus(fileIndex, DownloadStatus.PENDING, 0f, 0L)

        // Check overall completion status
        val allCompleted = _uiState.value.files.all { it.status == DownloadStatus.COMPLETED }
        _uiState.update { it.copy(isAllCompleted = allCompleted) }
    }

    fun startDownload() {
        if (_uiState.value.isDownloading) return

        _uiState.update { it.copy(isDownloading = true) }
        viewModelScope.launch {
            val filesToDownload = _uiState.value.files
            var allSuccess = true

            // Sequential download to avoid bandwidth issues
            for (index in filesToDownload.indices) {
                val file = _uiState.value.files[index] // Re-fetch to get latest status
                if (file.status == DownloadStatus.COMPLETED) continue

                val destinationFile = File(application.filesDir, file.destinationPath)
                val markerFile = File(destinationFile.absolutePath + ".complete")

                // Cleanup partials
                if (destinationFile.exists()) destinationFile.delete()
                if (markerFile.exists()) markerFile.delete()

                updateFileStatus(index, DownloadStatus.DOWNLOADING)

                val success = downloadFile(file.url, destinationFile, index)
                if (success) {
                    try {
                        if (file.contextLength != null) {
                            markerFile.writeText(file.contextLength.toString())
                        } else {
                            markerFile.createNewFile()
                        }
                        updateFileStatus(index, DownloadStatus.COMPLETED, 100f)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        updateFileStatus(index, DownloadStatus.ERROR)
                        allSuccess = false
                    }
                } else {
                    updateFileStatus(index, DownloadStatus.ERROR)
                    allSuccess = false
                    break // Stop on error
                }
            }

            _uiState.update {
                it.copy(
                    isDownloading = false,
                    isAllCompleted = allSuccess && _uiState.value.files.all { f -> f.status == DownloadStatus.COMPLETED }
                )
            }
        }
    }

    private fun updateFileStatus(
        index: Int,
        status: DownloadStatus,
        progress: Float = 0f,
        downloadedBytes: Long = -1L
    ) {
        _uiState.update { state ->
            val updatedFiles = state.files.toMutableList()
            updatedFiles[index] = updatedFiles[index].copy(
                status = status,
                progress = progress,
                downloadedBytes = if (downloadedBytes >= 0) downloadedBytes else updatedFiles[index].downloadedBytes
            )
            state.copy(files = updatedFiles)
        }
    }

    fun updateContextLength(length: Int) {
        if (_uiState.value.isDownloading) return

        _uiState.update {
            it.copy(
                selectedContextLength = length,
                files = getFilesForContextLength(length)
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            checkExistingFiles()
        }
    }

    private fun getFilesForContextLength(length: Int): List<DownloadableFile> {
        return listOf(
            DownloadableFile(
                name = "model.pte",
                groupName = "MedGemma 1.5 4B",
                url = "https://huggingface.co/kamalkraj/medgemma-1.5-4b-it-executorch/resolve/main/$length/model.pte?download=true",
                destinationPath = "model.pte",
                contextLength = length
            ),
            DownloadableFile(
                name = "tokenizer.model",
                groupName = "MedGemma 1.5 4B",
                url = "https://huggingface.co/kamalkraj/medgemma-1.5-4b-it-executorch/resolve/main/tokenizer.model?download=true",
                destinationPath = "tokenizer.model"
            ),
            DownloadableFile(
                name = "embedding_gemma.tflite",
                groupName = "Embedding Model",
                url = "https://huggingface.co/kamalkraj/embeddinggemma-300m-litert/resolve/main/embedding_gemma_no_normalize_q8.tflite?download=true",
                destinationPath = "embedding_gemma_no_normalize_q8.tflite"
            ),
            DownloadableFile(
                name = "model.int8.onnx",
                groupName = "MedASR",
                url = "https://huggingface.co/kamalkraj/medasr-onnx/resolve/main/model.int8.onnx?download=true",
                destinationPath = "model.int8.onnx"
            ),
            DownloadableFile(
                name = "tokens.txt",
                groupName = "MedASR",
                url = "https://huggingface.co/kamalkraj/medasr-onnx/resolve/main/tokens.txt?download=true",
                destinationPath = "tokens.txt"
            )
        )
    }

    private suspend fun downloadFile(
        urlString: String,
        destination: File,
        fileIndex: Int
    ): Boolean {
        return withContext(Dispatchers.IO) {
            var input: BufferedInputStream? = null
            var output: FileOutputStream? = null
            var connection: HttpURLConnection? = null
            try {
                Log.d(TAG, "Starting download: $urlString")
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection



                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "Download failed with response code: $responseCode")
                    updateFileStatus(fileIndex, DownloadStatus.ERROR)
                    return@withContext false
                }

                val fileLength = connection.contentLength

                // If content length is unknown, immediately switch to indeterminate state
                if (fileLength <= 0) {
                    viewModelScope.launch {
                        updateFileStatus(fileIndex, DownloadStatus.DOWNLOADING, -1f)
                    }
                }

                input = BufferedInputStream(connection.inputStream)
                output = FileOutputStream(destination)

                val data = ByteArray(1024 * 512) // 512KB buffer for better performance
                var total: Long = 0
                var count: Int

                var lastProgressUpdateBytes = 0L

                while (input.read(data).also { count = it } != -1) {
                    total += count
                    output.write(data, 0, count)

                    // Throttle updates: every 5MB to reduce CPU usage
                    if (total - lastProgressUpdateBytes > 5 * 1024 * 1024) {
                        lastProgressUpdateBytes = total

                        val progress = if (fileLength > 0) (total * 100 / fileLength).toInt()
                            .toFloat() else -1f

                        // Use launch (asynchronous) instead of withContext (synchronous)
                        // to ensure the download loop never waits for the UI thread.
                        viewModelScope.launch {
                            updateFileStatus(fileIndex, DownloadStatus.DOWNLOADING, progress, total)
                        }
                    }
                }
                // Ensure final update
                viewModelScope.launch {
                    val finalProgress = if (fileLength > 0) 100f else -1f
                    updateFileStatus(fileIndex, DownloadStatus.DOWNLOADING, finalProgress, total)
                }
                output.flush()
                Log.d(TAG, "Download complete: ${destination.name}, Total bytes: $total")
                true
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Download error: ${e.message}", e)
                viewModelScope.launch {
                    _uiState.update { state ->
                        val updatedFiles = state.files.toMutableList()
                        updatedFiles[fileIndex] =
                            updatedFiles[fileIndex].copy(error = e.localizedMessage)
                        state.copy(files = updatedFiles)
                    }
                }
                false
            } finally {
                output?.close()
                input?.close()
                connection?.disconnect()
            }
        }
    }
}
