package ru.digitalpaper.server.dto.request.document

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(description = "Запрос на назначение ответственного за документ")
data class AssignDocumentResponsibleRequest(
    @field:NotNull(message = "Необходимо указать пользователя")
    @field:Schema(
        description = "Идентификатор пользователя, который станет ответственным",
        format = "uuid",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    val userId: UUID?
)
