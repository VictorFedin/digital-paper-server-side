package ru.digitalpaper.server.dto.response.document

import ru.digitalpaper.server.dto.response.Response
import java.time.ZonedDateTime
import java.util.UUID

data class TemplateResponse(
    val id: UUID,
    val name: String,
    val organizationId: UUID,
    val createdBy: UUID,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
) : Response()
