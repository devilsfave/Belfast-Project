package com.example.medgem.utils

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.get
import androidx.core.graphics.scale
import com.example.medgem.ModelConfig

object ImageUtils {
    /**
     * Prepares a bitmap for LLM inference by resizing and normalizing it.
     */
    fun prepareImage(bitmap: Bitmap): FloatArray {
        // Create a resized bitmap (direct resize) to target size
        val scaledBitmap = createResizedBitmap(bitmap)
        val width = scaledBitmap.width
        val height = scaledBitmap.height

        // 3 channels (RGB) * width * height
        val floatArray = FloatArray(width * height * ModelConfig.Llm.IMAGE_CHANNELS)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = scaledBitmap[x, y]

                // Extract RGB
                val red = Color.red(color)
                val green = Color.green(color)
                val blue = Color.blue(color)

                // Planar format (RRR...GGG...BBB...)
                val pixelIndex = y * width + x
                val channelStride = width * height

                // Normalize: ((val / 255.0) - 0.5) / 0.5
                floatArray[pixelIndex] = ((red / 255.0f) - 0.5f) / 0.5f
                floatArray[pixelIndex + channelStride] = ((green / 255.0f) - 0.5f) / 0.5f
                floatArray[pixelIndex + 2 * channelStride] = ((blue / 255.0f) - 0.5f) / 0.5f
            }
        }

        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }

        return floatArray
    }

    /**
     * Creates a resized square bitmap of the specified size.
     */
    fun createResizedBitmap(source: Bitmap): Bitmap {
        val targetSize = ModelConfig.Llm.IMAGE_SIZE

        if (source.width == targetSize && source.height == targetSize) {
            return source
        }

        return source.scale(targetSize, targetSize)
    }
}
