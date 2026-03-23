package ru.digitalpaper.server.dto.internal

data class StoredObjectInfo(
    val bucket: String,
    val objectKey: String,
    val originalFileName: String?,
    val contentType: String,
    val size: Long,
    val etag: String?,
    val versionId: String?
)
