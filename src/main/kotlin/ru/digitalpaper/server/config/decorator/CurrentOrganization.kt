package ru.digitalpaper.server.config.decorator

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    name = "X-Organization-Id",
    description = "Идентификатор организации, в контексте которой выполняется запрос",
    required = true,
    `in` = ParameterIn.HEADER,
    schema = Schema(type = "string", format = "uuid")
)
annotation class CurrentOrganization
