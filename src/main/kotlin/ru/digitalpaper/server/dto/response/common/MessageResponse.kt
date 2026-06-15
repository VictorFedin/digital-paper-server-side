package ru.digitalpaper.server.dto.response.common

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Ответ с результатом выполненной операции")
data class MessageResponse(
    @field:Schema(description = "Сообщение о результате", example = "Операция выполнена успешно")
    val message: String
)
