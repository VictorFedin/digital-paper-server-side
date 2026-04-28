package ru.digitalpaper.server.dto.response.organization

import ru.digitalpaper.server.dto.response.Response
import ru.digitalpaper.server.model.organization.holder.OrganizationType
import ru.digitalpaper.server.model.user.holder.Avatar
import java.time.ZonedDateTime
import java.util.UUID

data class OrganizationListItem(
    val id: UUID,
    val name: String,
    val avatar: Avatar?,
    val type: OrganizationType,
    val createdAt: ZonedDateTime
) : Response()
