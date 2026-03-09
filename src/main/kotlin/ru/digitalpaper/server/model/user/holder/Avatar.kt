package ru.digitalpaper.server.model.user.holder

import ru.digitalpaper.server.dto.response.user.AvatarResponse
import java.time.ZonedDateTime
import java.util.UUID

data class Avatar(
    val id: UUID,
    val link: String,
    val fileName: String,
    val fileSize: Long,
    val contentType: String?,
    val createdAt: ZonedDateTime
) {
    fun toResponse(): AvatarResponse =
        AvatarResponse(
            id = id,
            link = link,
            fileName = fileName,
            fileSize = fileSize,
            contentType = contentType,
            createdAt = createdAt
        )
}
