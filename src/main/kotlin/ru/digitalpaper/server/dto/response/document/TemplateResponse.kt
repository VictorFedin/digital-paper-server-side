package ru.digitalpaper.server.dto.response.document

import java.time.ZonedDateTime
import java.util.*

data class TemplateResponse(
    val id: UUID,
    val name: String,
    val organizationId: UUID,
    val createdBy: UUID,
    val fields: List<TemplateFieldResponse>,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
)
