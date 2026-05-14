package ru.digitalpaper.server.controller.base

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class TraceIdFilter : OncePerRequestFilter() {

    companion object {
        const val TRACE_ID_HEADER = "X-Trace-Id"
        const val TRACE_ID_ATTRIBUTE = "traceId"
        const val MDC_TRACE_ID = "traceId"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val traceId = request.getHeader(TRACE_ID_HEADER)
            ?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()

        try {
            request.setAttribute(TRACE_ID_ATTRIBUTE, traceId)
            response.setHeader(TRACE_ID_HEADER, traceId)
            MDC.put(MDC_TRACE_ID, traceId)

            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(MDC_TRACE_ID)
        }
    }

}
