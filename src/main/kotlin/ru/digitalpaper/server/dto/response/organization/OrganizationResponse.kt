package ru.digitalpaper.server.dto.response.organization

import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.model.organization.holder.Industry
import ru.digitalpaper.server.model.organization.holder.ModerationStatus
import java.time.ZonedDateTime
import java.util.UUID

data class OrganizationResponse(
    val id: UUID,
    val name: String,
    val description: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val industry: Industry,
    val status: ModerationStatus,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
) : Response()
