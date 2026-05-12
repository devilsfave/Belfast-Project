package com.example.medgem.data

import io.objectbox.converter.PropertyConverter
import org.json.JSONArray

/**
 * Converts a List of Strings to a JSON String for ObjectBox storage.
 */
class StringListConverter : PropertyConverter<List<String>, String> {

    override fun convertToEntityProperty(databaseValue: String?): List<String> {
        if (databaseValue.isNullOrBlank()) {
            return emptyList()
        }
        return try {
            val jsonArray = JSONArray(databaseValue)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun convertToDatabaseValue(entityProperty: List<String>?): String {
        if (entityProperty == null) {
            return "[]"
        }
        val jsonArray = JSONArray()
        entityProperty.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }
}
