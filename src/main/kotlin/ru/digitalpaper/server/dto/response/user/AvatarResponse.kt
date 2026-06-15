package ru.digitalpaper.server.dto.response.user

import io.swagger.v3.oas.annotations.media.Schema
import java.time.ZonedDateTime
import java.util.*

@Schema(description = "Данные пользовательского аватара")
data class AvatarResponse(
    @field:Schema(description = "Идентификатор аватара", format = "uuid", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Ссылка для получения изображения", example = "http://localhost:9000/public/user_avatars/user/avatar.webp")
    val link: String,

    @field:Schema(description = "Исходное имя файла", example = "avatar.webp")
    val fileName: String,

    @field:Schema(description = "Размер файла в байтах", example = "245760")
    val fileSize: Long,

    @field:Schema(description = "MIME-тип изображения", example = "image/webp", nullable = true)
    val contentType: String?,

    @field:Schema(description = "Дата и время загрузки", format = "date-time", example = "2026-06-15T12:00:00+03:00")
    val createdAt: ZonedDateTime
)
