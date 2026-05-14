package ru.digitalpaper.server.dto.response.document

import ru.digitalpaper.server.model.document.holder.DocumentStatus
import java.time.ZonedDateTime
import java.util.*

data class DocumentListItem(
    val id: UUID,
    val name: String,
    val responsible: String,
    val status: DocumentStatus,
    val contentType: String,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
)
