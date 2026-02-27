package ru.digitalpaper.server.model.user.holder

import java.time.ZonedDateTime
import java.util.UUID

data class Avatar(
    val id: UUID,
    val link: String,
    val fileName: String,
    val fileSize: Long,
    val contentType: String?,
    val createdAt: ZonedDateTime
)
