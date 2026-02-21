package ru.digitalpaper.server.util.log

import ru.digitalpaper.server.util.Utils
import ru.digitalpaper.server.util.log.types.LogLevel

data class Log(
    val level: LogLevel,
    val method: String,
    val traceId: String,
    val message: String,
    val error: String = "",
    val attributes: Map<String, Any> = emptyMap()
) {

    constructor(
        level: LogLevel,
        method: String,
        traceId: String,
        message: String,
        error: String = "",
        vararg el: Pair<String, Any>
    ) : this(level, method, traceId, message, error, mapOf(*el))

    override fun toString(): String {
        return "server_log: " + Utils.logToJson(this)
    }
}
