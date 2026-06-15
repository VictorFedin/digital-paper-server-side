package ru.digitalpaper.server.dto.response.common

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Стандартный ответ с информацией об ошибке")
data class ErrorResponse(
    @field:Schema(description = "HTTP-код ошибки", example = "400")
    val code: Int,

    @field:Schema(description = "Общее описание ошибки", example = "Ошибка валидации")
    val message: String,

    @field:Schema(
        description = "Ошибки отдельных полей в формате поле-сообщение",
        example = """{"email":"Неверный формат email"}"""
    )
    val errors: Map<String, String>
)
