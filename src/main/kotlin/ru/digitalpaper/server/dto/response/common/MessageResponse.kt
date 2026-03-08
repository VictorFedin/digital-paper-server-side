package ru.digitalpaper.server.dto.response.common

import ru.digitalpaper.server.dto.response.Response

data class MessageResponse(
    val message: String
) : Response()
