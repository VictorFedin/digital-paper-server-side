package ru.digitalpaper.server.exception

open class CustomException(
    val code: Int,
    message: String,
) : Exception(message)
