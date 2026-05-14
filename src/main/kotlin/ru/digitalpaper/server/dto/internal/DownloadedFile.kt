package ru.digitalpaper.server.dto.internal

import org.springframework.core.io.InputStreamResource

data class DownloadedFile(
    val filename: String,
    val contentType: String,
    val resource: InputStreamResource,
    val contentLength: Long? = null,
)
