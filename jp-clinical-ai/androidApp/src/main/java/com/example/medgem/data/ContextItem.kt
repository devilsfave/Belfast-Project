package com.example.medgem.data

import android.util.Log
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
sealed class ContextItem {
    @Serializable
    @SerialName("text")
    data class Text(val content: String) : ContextItem()

    @Serializable
    @SerialName("image")
    data class Image(val path: String) : ContextItem()

    companion object {
        fun toJson(items: List<ContextItem>): String {
            return Json.encodeToString(items)
        }

        fun fromJson(json: String): List<ContextItem> {
            return try {
                Json.decodeFromString<List<ContextItem>>(json)
            } catch (e: Exception) {
                Log.e("ContextItem", "Failed to parse contextJson", e)
                emptyList()
            }
        }
    }
}
