package ru.digitalpaper.server.model.user.holder

import java.time.ZonedDateTime
import java.util.*

data class Avatar(
    val id: UUID,
    val bucket: String,
    val objectKey: String,
    val fileName: String,
    val fileSize: Long,
    val contentType: String?,
    val createdAt: ZonedDateTime
)
