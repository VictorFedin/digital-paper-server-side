package ru.digitalpaper.server.dto.response.department

import java.time.ZonedDateTime
import java.util.UUID

data class DepartmentResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val phoneNumber: String?,
    val email: String?,
    val category: String,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime
)
