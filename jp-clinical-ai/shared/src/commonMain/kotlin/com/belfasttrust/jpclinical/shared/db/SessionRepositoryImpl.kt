package com.belfasttrust.jpclinical.shared.db

import com.belfasttrust.jpclinical.domain.SessionRepository
import com.belfasttrust.jpclinical.schema.MasterSchema
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

class SessionRepositoryImpl(
    private val database: ClinicalDatabase,
    private val coroutineContext: CoroutineContext = Dispatchers.Default
) : SessionRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val queries = database.clinicalDatabaseQueries

    override suspend fun saveSchema(sessionId: String, schema: MasterSchema) {
        val schemaJsonString = json.encodeToString(schema)
        queries.saveSession(
            sessionId = sessionId,
            schemaJson = schemaJsonString,
            isFinalised = schema.schemaFinalised,
            createdAt = getCurrentTimeMillis()
        )
    }

    override suspend fun loadSchema(sessionId: String): MasterSchema? {
        val session = queries.loadSession(sessionId).executeAsOneOrNull() ?: return null
        return try {
            json.decodeFromString<MasterSchema>(session.schemaJson)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun finaliseSession(sessionId: String) {
        queries.finaliseSession(sessionId)
    }

    override fun getAllSessions(): Flow<List<String>> {
        return queries.getAllSessions().asFlow().mapToList(coroutineContext)
    }
}
