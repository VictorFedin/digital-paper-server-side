package ru.digitalpaper.server.controller.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.digitalpaper.server.config.decorator.Public
import ru.digitalpaper.server.dto.request.auth.LoginRequest
import ru.digitalpaper.server.dto.request.auth.LogoutRequest
import ru.digitalpaper.server.dto.request.auth.RefreshTokenRequest
import ru.digitalpaper.server.dto.response.auth.TokenResponse
import ru.digitalpaper.server.dto.response.common.ErrorResponse
import ru.digitalpaper.server.service.AuthService

@RestController
@RequestMapping(value = ["/api/v1/auth"])
@Tag(name = "Авторизация", description = "Управление пользовательской сессией через Keycloak")
@SecurityRequirements
class AuthController(private val authService: AuthService) {

    @Operation(
        summary = "Войти в систему",
        description = "Проверяет учётные данные в Keycloak и возвращает access/refresh токены"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Авторизация выполнена",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TokenResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Некорректные данные запроса",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Неверный логин или пароль",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Ошибка сервера",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @Public
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): TokenResponse {
        return authService.login(request)
    }

    @Operation(
        summary = "Обновить токены",
        description = "Получает новую пару токенов по действующему refresh token"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Токены обновлены",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = TokenResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Некорректный или просроченный refresh token",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Ошибка сервера",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @PostMapping("/refresh")
    fun refresh(@RequestBody request: RefreshTokenRequest): TokenResponse {
        return authService.refresh(request)
    }

    @Operation(
        summary = "Завершить сессию",
        description = "Отзывает refresh token в Keycloak"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Сессия завершена"),
            ApiResponse(
                responseCode = "400",
                description = "Некорректный refresh token",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Ошибка сервера",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@RequestBody request: LogoutRequest) {
        authService.logout(request)
    }
}
