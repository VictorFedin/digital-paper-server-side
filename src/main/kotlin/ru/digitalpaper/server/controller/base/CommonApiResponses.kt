package ru.digitalpaper.server.controller.base

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import ru.digitalpaper.server.dto.response.common.ErrorResponse

@Target(allowedTargets = [AnnotationTarget.FUNCTION, AnnotationTarget.CLASS])
@Retention(AnnotationRetention.RUNTIME)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "400",
            description = "Некорректный запрос",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )
            ]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Пользователь не авторизован",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )
            ]
        ),
        ApiResponse(
            responseCode = "403",
            description = "Недостаточно прав",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )
            ]
        ),
        ApiResponse(
            responseCode = "404",
            description = "Запрашиваемый ресурс не найден",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )
            ]
        ),
        ApiResponse(
            responseCode = "500",
            description = "Ошибка сервера",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )
            ]
        )
    ]
)
annotation class CommonApiResponses
