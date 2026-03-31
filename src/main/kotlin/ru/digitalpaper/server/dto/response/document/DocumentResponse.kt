package ru.digitalpaper.server.dto.response.document

import ru.digitalpaper.server.dto.response.Response
import java.time.ZonedDateTime
import java.util.*

data class DocumentResponse(
    val id: UUID,
    val name: String,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
) : Response()
