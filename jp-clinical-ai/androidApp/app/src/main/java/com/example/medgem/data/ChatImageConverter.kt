package com.example.medgem.data

import android.util.Log
import org.json.JSONArray

object ChatImageConverter {

    fun pathsToJsonArray(paths: List<String>): String {
        val jsonArray = JSONArray()
        paths.forEach { path -> jsonArray.put(path) }
        return jsonArray.toString()
    }

    fun jsonArrayToPaths(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()

        return try {
            val jsonArray = JSONArray(json)
            val paths = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                paths.add(jsonArray.getString(i))
            }
            paths
        } catch (e: Exception) {
            Log.e("ChatImageConverter", "Error parsing image paths JSON", e)
            emptyList()
        }
    }
}