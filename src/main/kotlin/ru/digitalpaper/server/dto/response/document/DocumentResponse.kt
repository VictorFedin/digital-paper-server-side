package ru.digitalpaper.server.dto.response.document

import ru.digitalpaper.server.model.document.holder.DocumentStatus
import ru.digitalpaper.server.model.document.holder.DocumentType
import java.time.ZonedDateTime
import java.util.*

data class DocumentResponse(
    val id: UUID,
    val name: String,
    val type: DocumentType,
    val status: DocumentStatus,
    val contentType: String,
    val responsible: String,
    val createdBy: String,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
)
