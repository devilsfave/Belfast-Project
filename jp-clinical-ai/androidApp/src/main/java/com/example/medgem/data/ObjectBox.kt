package com.example.medgem.data

import android.content.Context
import android.util.Log
import io.objectbox.BoxStore
import java.io.File
import java.io.FileOutputStream

/**
 * ObjectBox database singleton for persisting chat conversations and messages.
 */
object ObjectBox {
    private const val TAG = "ObjectBox"
    private const val INITIAL_DB_NAME = "initial_data.mdb"

    lateinit var store: BoxStore
        private set

    fun init(context: Context) {
        if (::store.isInitialized) return

        try {
            // Ensure files directory exists (needed by ObjectBox)
            val filesDir = context.filesDir
            if (!filesDir.exists()) {
                filesDir.mkdirs()
            }

            // Check for initial database in assets
            val initialDbFile = copyAssetToTempFile(context, INITIAL_DB_NAME)

            val builder = MyObjectBox.builder()
                .androidContext(context.applicationContext)

            if (initialDbFile != null) {
                Log.d(
                    TAG,
                    "Found initial database file, setting as initialDbFile: ${initialDbFile.absolutePath}"
                )
                builder.initialDbFile(initialDbFile)
            } else {
                Log.d(TAG, "No initial database file found in assets.")
            }

            store = builder.build()

            // Clean up temp file if it was created
            initialDbFile?.delete()

            // Log data count for verification
            val count = store.boxFor(KnowledgeEntity::class.java).count()
            Log.d(TAG, "ObjectBox initialized. KnowledgeEntity count: $count")

        } catch (e: Exception) {
            // This commonly happens in secondary processes (like the PDF viewer service)
            // which don't have the same storage access or don't need the DB.
            Log.w(
                TAG,
                "Skipping ObjectBox initialization (likely in a secondary process): ${e.message}"
            )
        }
    }

    private fun copyAssetToTempFile(context: Context, assetName: String): File? {
        return try {
            // Check if asset exists
            val assets = context.assets.list("") ?: return null
            if (!assets.contains(assetName)) return null

            val tempFile = File(context.cacheDir, "temp_initial_db.mdb")

            // Ensure cache directory exists
            if (!context.cacheDir.exists()) {
                val created = context.cacheDir.mkdirs()
                if (!created) {
                    Log.e(TAG, "Failed to create cache directory: ${context.cacheDir.absolutePath}")
                    return null
                }
            }

            // Always overwrite temp file to ensure latest asset version
            context.assets.open(assetName).use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error copying initial database from assets", e)
            null
        }
    }
}
