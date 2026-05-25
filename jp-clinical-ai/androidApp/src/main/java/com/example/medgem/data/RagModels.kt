package com.example.medgem.data

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

// RAG-specific message with references
data class RagSourceReference(
    val title: String,
    val pdfFile: String?,
    val page: Int = 0
)

fun referencesToJson(references: List<RagSourceReference>): String? {
    if (references.isEmpty()) return null
    val array = JSONArray()
    references.forEach { ref ->
        val obj = JSONObject()
        obj.put("title", ref.title)
        obj.put("pdfFile", ref.pdfFile)
        obj.put("page", ref.page)
        array.put(obj)
    }
    return array.toString()
}

fun jsonToReferences(json: String?): List<RagSourceReference> {
    if (json.isNullOrBlank()) return emptyList()
    val referencesList = mutableListOf<RagSourceReference>()
    try {
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val titleStr = jsonObject.optString("title", "")
            val pdfFileStr =
                if (jsonObject.isNull("pdfFile")) null else jsonObject.optString("pdfFile")
            val pageNum = jsonObject.optInt("page", 0)
            referencesList.add(RagSourceReference(titleStr, pdfFileStr, pageNum))
        }
    } catch (e: Exception) {
        Log.e("RagModels", "Error parsing references JSON", e)
    }
    return referencesList
}
