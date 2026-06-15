package ru.digitalpaper.server.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.controller.base.CommonApiResponses
import ru.digitalpaper.server.dto.request.user.UpdateUserProfileRequest
import ru.digitalpaper.server.dto.response.user.AvatarResponse
import ru.digitalpaper.server.dto.response.user.UserPayload
import ru.digitalpaper.server.dto.response.user.UserProfileResponse
import ru.digitalpaper.server.service.UserService

@CommonApiResponses
@RestController
@RequestMapping(value = ["/api/v1/user"])
@Tag(name = "Пользователь", description = "Профиль и аватар текущего пользователя")
class UserController(
    private val userService: UserService
) {

    @Operation(
        summary = "Получить профиль пользователя",
        description = "Возвращает профиль текущего пользователя"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Операция успешна",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = UserProfileResponse::class)
        )],
    )
    @GetMapping(value = ["/profile"])
    fun getUserProfile(
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload
    ): UserProfileResponse {
        return userService.getUserProfile(payload)
    }

    @Operation(
        summary = "Обновить профиль пользователя",
        description = "Обновляет данные профиля текущего пользователя"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Операция успешна",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = UserProfileResponse::class)
        )],
    )
    @PatchMapping(value = ["/profile"])
    fun updateUserProfile(
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @RequestBody request: UpdateUserProfileRequest
    ): UserProfileResponse {
        return userService.updateUserProfile(payload, request)
    }

    @Operation(
        summary = "Сохранить аватар пользователя",
        description = "Возвращает аватар текущего пользователя"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Операция успешна",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = AvatarResponse::class)
        )],
    )
    @PostMapping(value = ["/avatar"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun saveUserAvatar(
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload,
        @Parameter(
            description = "Изображение JPEG, PNG или WebP размером до 5 МБ",
            required = true,
            schema = Schema(type = "string", format = "binary")
        )
        @RequestPart("file") file: MultipartFile
    ): AvatarResponse {
        return userService.saveUserAvatar(file, payload)
    }

    @Operation(
        summary = "Удалить аватар пользователя",
        description = "Удаляет текущий аватар пользователя из профиля и файлового хранилища"
    )
    @ApiResponse(responseCode = "200", description = "Аватар удалён или отсутствовал")
    @DeleteMapping(value = ["/avatar"])
    fun deleteAvatar(
        @Parameter(hidden = true)
        @AuthenticationPrincipal payload: UserPayload
    ) {
        userService.deleteUserAvatar(payload)
    }
}
