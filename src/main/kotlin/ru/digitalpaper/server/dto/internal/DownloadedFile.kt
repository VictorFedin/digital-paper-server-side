package ru.digitalpaper.server.dto.internal

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.core.io.InputStreamResource

@Schema(hidden = true)
data class DownloadedFile(
    val filename: String,
    val contentType: String,
    val resource: InputStreamResource,
    val contentLength: Long? = null,
)
