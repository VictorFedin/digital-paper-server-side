package ru.digitalpaper.server.util.common


data class RequestSatellites(
    val traceId: String,
    val attributes: MutableMap<String, String> = mutableMapOf()
)