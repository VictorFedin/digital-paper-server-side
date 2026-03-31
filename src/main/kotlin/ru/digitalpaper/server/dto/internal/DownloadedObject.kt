package ru.digitalpaper.server.dto.internal

import java.io.InputStream

data class DownloadedObject(
    val inputStream: InputStream,
    val contentType: String,
    val size: Long,
    val originalFileName: String? = null
)