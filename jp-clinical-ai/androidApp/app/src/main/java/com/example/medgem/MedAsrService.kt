package com.example.medgem

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import com.k2fsa.sherpa.onnx.WaveReader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedAsrService @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val TAG = "MedAsrService"
    private var audioRecord: AudioRecord? = null

    @Volatile
    private var isRecording = false

    private val sampleRate = ModelConfig.MedAsr.SAMPLE_RATE
    private val audioSource = MediaRecorder.AudioSource.MIC
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    suspend fun getRecognizer() = MedAsrProvider.initialize().getOrNull()

    /**
     * Returns true if recording started successfully, false otherwise.
     */
    @SuppressLint("MissingPermission")
    suspend fun startRecording(): Boolean = withContext(Dispatchers.IO) {
        if (isRecording) {
            Log.w(TAG, "startRecording: already recording")
            return@withContext false
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "RECORD_AUDIO permission not granted")
            return@withContext false
        }

        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR) {
            Log.e(TAG, "Invalid buffer size")
            return@withContext false
        }

        val record = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val attributedContext = context.createAttributionContext("voice_input")
            AudioRecord.Builder()
                .setAudioSource(audioSource)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelConfig)
                        .setEncoding(audioFormat)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setContext(attributedContext)
                .build()
        } else {
            AudioRecord(
                audioSource,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
        }

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord initialization failed")
            record.release()
            return@withContext false
        }

        audioRecord = record
        recordedBytes.reset()
        record.startRecording()
        isRecording = true

        Log.d(TAG, "Started recording")

        // Launch reading loop in background so we return immediately
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            val buffer = ShortArray(bufferSize)
            val byteBuffer = ByteArray(bufferSize * 2) // 2 bytes per short
            while (isRecording) {
                val readCount = record.read(buffer, 0, buffer.size)
                if (readCount > 0) {
                    // Batch-convert shorts to bytes and write in one call
                    ByteBuffer.wrap(byteBuffer, 0, readCount * 2)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .asShortBuffer()
                        .put(buffer, 0, readCount)
                    recordedBytes.write(byteBuffer, 0, readCount * 2)
                }
            }
        }

        return@withContext true
    }

    /** Byte buffer for recorded PCM data — far cheaper than boxed Short list. */
    private val recordedBytes = ByteArrayOutputStream()

    suspend fun stopRecording(): String? = withContext(Dispatchers.IO) {
        if (!isRecording) return@withContext null

        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        Log.d(TAG, "Stopped recording. Bytes collected: ${recordedBytes.size()}")

        if (recordedBytes.size() == 0) return@withContext ""

        val recognizer = getRecognizer() ?: return@withContext "Error: ASR model not loaded"

        // Convert recorded bytes back to float samples
        val raw = recordedBytes.toByteArray()
        val shortBuffer = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        val sampleCount = raw.size / 2
        val samplesArray = FloatArray(sampleCount)
        for (i in 0 until sampleCount) {
            samplesArray[i] = shortBuffer.get(i).toFloat() / 32768.0f
        }

        val stream = recognizer.createStream()
        try {
            stream.acceptWaveform(samplesArray, sampleRate)
            recognizer.decode(stream)
            val result = recognizer.getResult(stream)
            Log.d(TAG, "Transcription result: ${result.text}")
            result.text
        } finally {
            stream.release()
        }
    }

    suspend fun transcribeFile(filePath: String): Result<String> = withContext(Dispatchers.IO) {
        val recognizer = getRecognizer()
            ?: return@withContext Result.failure(Exception("ASR model not loaded"))

        try {
            val waveData = WaveReader.readWave(filePath)
            val stream = recognizer.createStream()
            try {
                stream.acceptWaveform(waveData.samples, waveData.sampleRate)
                recognizer.decode(stream)
                val result = recognizer.getResult(stream)
                Log.d(TAG, "File transcription result: ${result.text}")
                Result.success(result.text)
            } finally {
                stream.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error transcribing file: $filePath", e)
            Result.failure(e)
        }
    }

    fun isRecording() = isRecording

    fun saveRecording(file: java.io.File) {
        if (recordedBytes.size() == 0) return
        try {
            val rawData = recordedBytes.toByteArray()
            val outputStream = java.io.FileOutputStream(file)
            val header = writeWavHeader(rawData.size, sampleRate, 1, 16)
            outputStream.write(header)
            outputStream.write(rawData)
            outputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving recording to file", e)
        }
    }

    private fun writeWavHeader(
        totalAudioLen: Int,
        longSampleRate: Int,
        channels: Int,
        byteRate: Int
    ): ByteArray {
        val totalDataLen = totalAudioLen + 36
        val byteRateValue = longSampleRate * channels * byteRate / 8
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte() // RIFF/WAVE header
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // 'fmt ' chunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRateValue and 0xff).toByte()
        header[29] = (byteRateValue shr 8 and 0xff).toByte()
        header[30] = (byteRateValue shr 16 and 0xff).toByte()
        header[31] = (byteRateValue shr 24 and 0xff).toByte()
        header[32] = (2 * 16 / 8).toByte() // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()

        return header
    }

    fun stopAndRelease() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
