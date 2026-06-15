package ru.digitalpaper.server.dto.internal

import io.swagger.v3.oas.annotations.media.Schema

@Schema(hidden = true)
data class StoredObjectInfo(
    val bucket: String,
    val objectKey: String,
    val originalFileName: String?,
    val contentType: String,
    val size: Long,
    val etag: String?,
    val versionId: String?
)
