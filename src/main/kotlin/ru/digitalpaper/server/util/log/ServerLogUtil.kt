package ru.digitalpaper.server.util.log

import ru.digitalpaper.server.util.log.types.LogLevel
import java.io.PrintWriter
import java.io.StringWriter

object ServerLogUtil {

    /* INFO level */

    fun infoLog(
        method: String,
        traceId: String,
        message: String,
        attributes: Map<String, String> = emptyMap()
    ): Log = Log(
        level = LogLevel.INFO,
        method = method,
        traceId = traceId,
        message = message,
        attributes = attributes
    )

    fun infoLog(
        method: String,
        traceId: String,
        message: String,
        vararg el: Pair<String, Any>
    ): Log = Log(
        level = LogLevel.INFO,
        method = method,
        traceId = traceId,
        message = message,
        el = el
    )

    fun info(
        method: String,
        traceId: String,
        message: String,
        attributes: Map<String, String> = emptyMap()
    ): String = infoLog(
        method = method,
        traceId = traceId,
        message = message,
        attributes = attributes
    ).toString()

    fun info(
        method: String,
        traceId: String,
        message: String,
        vararg el: Pair<String, Any>
    ): String = infoLog(
        method = method,
        traceId = traceId,
        message = message,
        *el
    ).toString()

    /* ERROR level */

    fun errorLog(
        method: String,
        traceId: String,
        message: String,
        error: Throwable?,
        attributes: Map<String, String> = emptyMap()
    ): Log = Log(
        level = LogLevel.ERROR,
        method = method,
        traceId = traceId,
        message = message,
        error = printError(error),
        attributes = attributes
    )

    private fun printError(error: Throwable?): String {
        return if (error != null) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            error.printStackTrace(pw)
            val sStackTrace = sw.toString()
            "Error message: '${error.message}'      " +
                    "Error stackTrace: '$sStackTrace'"
        } else "No error message"
    }

    fun errorLog(
        method: String,
        traceId: String,
        message: String,
        error: Throwable?,
        vararg el: Pair<String, Any>
    ): Log = Log(
        level = LogLevel.ERROR,
        method = method,
        traceId = traceId,
        message = message,
        error = printError(error),
        *el
    )

    fun error(
        method: String,
        traceId: String,
        message: String,
        error: Throwable?,
        attributes: Map<String, String> = emptyMap()
    ): String = errorLog(
        method = method,
        traceId = traceId,
        message = message,
        error = error,
        attributes = attributes
    ).toString()

    fun error(
        method: String,
        traceId: String,
        message: String,
        error: Throwable?,
        vararg el: Pair<String, Any>
    ): String = errorLog(
        method = method,
        traceId = traceId,
        message = message,
        error = error,
        *el
    ).toString()

    fun error(
        method: String,
        traceId: String,
        message: String,
        error: String,
        vararg el: Pair<String, Any>
    ): String = Log(
        level = LogLevel.ERROR,
        method = method,
        traceId = traceId,
        message = message,
        error = error,
        *el
    ).toString()

    fun errorLog(
        method: String,
        traceId: String,
        message: String,
        attributes: Map<String, String> = emptyMap()
    ): Log = Log(
        level = LogLevel.ERROR,
        method = method,
        traceId = traceId,
        message = message,
        attributes = attributes
    )

    fun errorLog(
        method: String,
        traceId: String,
        message: String,
        vararg el: Pair<String, Any>
    ): Log = Log(
        level = LogLevel.ERROR, method = method,
        traceId = traceId,
        message = message,
        el = el
    )

    fun error(
        method: String,
        traceId: String,
        message: String,
        attributes: Map<String, String> = emptyMap()
    ): String = errorLog(
        method = method,
        traceId = traceId,
        message = message,
        attributes = attributes
    ).toString()

    fun error(
        method: String,
        traceId: String,
        message: String,
        vararg el: Pair<String, Any>
    ): String = errorLog(
        method = method,
        traceId = traceId,
        message = message,
        *el
    ).toString()

    /* DEBUG level */

    fun debugLog(
        method: String,
        traceId: String,
        message: String,
        attributes: Map<String, String> = emptyMap()
    ): Log = Log(
        level = LogLevel.DEBUG,
        method = method,
        traceId = traceId,
        message = message,
        attributes = attributes
    )

    fun debugLog(
        method: String,
        traceId: String,
        message: String,
        vararg el: Pair<String, Any>
    ): Log = Log(
        level = LogLevel.DEBUG,
        method = method,
        traceId = traceId,
        message = message,
        el = el
    )

    fun debug(
        method: String,
        traceId: String,
        message: String,
        attributes: Map<String, String> = emptyMap()
    ): String = debugLog(
        method = method,
        traceId = traceId,
        message = message,
        attributes = attributes
    ).toString()

    fun debug(
        method: String,
        traceId: String,
        message: String,
        vararg el: Pair<String, Any>
    ): String = debugLog(
        method = method,
        traceId = traceId,
        message = message,
        *el
    ).toString()

    /* WARN level */

    fun warnLog(
        method: String,
        traceId: String,
        message: String,
        attributes: Map<String, String> = emptyMap()
    ): Log = Log(
        level = LogLevel.WARN,
        method = method,
        traceId = traceId,
        message = message,
        attributes = attributes
    )

    fun warnLog(
        method: String,
        traceId: String,
        message: String,
        vararg el: Pair<String, Any>
    ): Log = Log(
        level = LogLevel.WARN,
        method = method,
        traceId = traceId,
        message = message,
        el = el
    )

    fun warn(
        method: String,
        traceId: String,
        message: String,
        attributes: Map<String, String> = emptyMap()
    ): String = warnLog(
        method = method,
        traceId = traceId,
        message = message,
        attributes = attributes
    ).toString()

    fun warn(
        method: String,
        traceId: String,
        message: String,
        vararg el: Pair<String, Any>
    ): String = warnLog(
        method = method,
        traceId = traceId,
        message = message,
        *el
    ).toString()
}