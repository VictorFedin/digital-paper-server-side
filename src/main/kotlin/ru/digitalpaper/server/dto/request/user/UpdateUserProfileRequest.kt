package ru.digitalpaper.server.dto.request.user

import java.time.LocalDate

data class UpdateUserProfileRequest(
    val firstName: String?,
    val lastName: String?,
    val middleName: String?,
    val birthday: LocalDate?
)
