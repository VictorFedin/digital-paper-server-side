package ru.digitalpaper.server.dto.response.user

import ru.digitalpaper.server.dto.response.Response
import java.time.ZonedDateTime
import java.util.UUID

data class AvatarResponse(
    val id: UUID,
    val link: String,
    val fileName: String,
    val fileSize: Long,
    val contentType: String?,
    val createdAt: ZonedDateTime
) : Response()
