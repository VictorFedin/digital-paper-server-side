package ru.digitalpaper.server.dto.request.user

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Изменяемые данные профиля текущего пользователя")
data class UpdateUserProfileRequest(
    @field:Schema(description = "Имя", example = "Иван", nullable = true)
    val firstName: String?,

    @field:Schema(description = "Фамилия", example = "Иванов", nullable = true)
    val lastName: String?,

    @field:Schema(description = "Отчество", example = "Иванович", nullable = true)
    val middleName: String?,

    @field:Schema(description = "Дата рождения", example = "1990-05-20", format = "date", nullable = true)
    val birthday: LocalDate?
)
