package com.example.medgem

import android.net.Uri

sealed class Route(val route: String) {
    data object Home : Route("home")
    data object KnowledgeSearch : Route("knowledge_search")
    data object ChatConversationList : Route("chat_conversation_list")
    data object Settings : Route("settings")

    data object Chat :
        Route("chat?conversationId={conversationId}&readOnly={readOnly}&patientId={patientId}&visitIds={visitIds}") {
        const val ArgConversationId = "conversationId"
        const val ArgReadOnly = "readOnly"
        const val ArgPatientId = "patientId"
        const val ArgVisitIds = "visitIds"

        fun createRoute(
            conversationId: Long? = null,
            isReadOnly: Boolean = false,
            patientId: Long? = null,
            visitIds: String? = null
        ): String {
            val id = conversationId ?: -1L
            var route = "chat?conversationId=$id&readOnly=$isReadOnly"
            if (patientId != null) route += "&patientId=$patientId"
            if (visitIds != null) route += "&visitIds=$visitIds"
            return route
        }
    }

    data object RagChat : Route("rag_chat?conversationId={conversationId}&patientId={patientId}") {
        const val ArgConversationId = "conversationId"
        const val ArgPatientId = "patientId"

        fun createRoute(conversationId: Long? = null, patientId: Long? = null): String {
            val id = conversationId ?: -1L
            var route = "rag_chat?conversationId=$id"
            if (patientId != null) route += "&patientId=$patientId"
            return route
        }
    }

    data object PdfViewer : Route("pdf_viewer/{fileName}/{pageNumber}?query={query}") {
        const val ArgFileName = "fileName"
        const val ArgPageNumber = "pageNumber"
        const val ArgQuery = "query"

        fun createRoute(fileName: String, pageNumber: Int, query: String?): String {
            val encodedFile = Uri.encode(fileName)
            val base = "pdf_viewer/$encodedFile/$pageNumber"
            return if (query.isNullOrBlank()) {
                base
            } else {
                "$base?query=${Uri.encode(query)}"
            }
        }
    }

    data object Protocols : Route("protocols")
    data object Onboarding : Route("onboarding")
    data object Disclaimer : Route("disclaimer")
    data object ModelDownload : Route("model_download")

    data object PatientList : Route("patient_list")
    data object PatientDetail : Route("patient_detail/{patientId}") {
        const val ArgPatientId = "patientId"
        fun createRoute(patientId: Long): String = "patient_detail/$patientId"
    }

    data object VisitDetail : Route("visit_detail/{visitId}/{patientId}") {
        const val ArgVisitId = "visitId"
        const val ArgPatientId = "patientId"
        fun createRoute(visitId: Long, patientId: Long): String = "visit_detail/$visitId/$patientId"
    }
}
