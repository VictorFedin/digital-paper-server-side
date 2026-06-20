package ru.digitalpaper.server.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "document.converter")
data class DocumentConverterProperties(
    val sofficePath: String = "soffice",
    val timeoutSeconds: Long = 60
)
