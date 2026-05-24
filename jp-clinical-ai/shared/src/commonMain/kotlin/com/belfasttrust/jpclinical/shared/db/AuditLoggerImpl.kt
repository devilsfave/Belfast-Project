package com.belfasttrust.jpclinical.shared.db

import com.belfasttrust.jpclinical.domain.AuditLogger
import com.belfasttrust.jpclinical.domain.AuditEventType

class AuditLoggerImpl(
    private val database: ClinicalDatabase
) : AuditLogger {

    private val queries = database.clinicalDatabaseQueries

    override suspend fun log(
        sessionId: String,
        eventType: AuditEventType,
        nurseId: String,
        details: String
    ) {
        queries.insertAudit(
            sessionId = sessionId,
            eventType = eventType.name,
            nurseId = nurseId,
            details = details,
            timestamp = getCurrentTimeMillis()
        )
    }
}
