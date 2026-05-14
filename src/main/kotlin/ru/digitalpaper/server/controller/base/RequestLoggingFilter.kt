package ru.digitalpaper.server.controller.base

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RequestLoggingFilter : OncePerRequestFilter() {

  private val log = LoggerFactory.getLogger(RequestLoggingFilter::class.java)


  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain
  ) {
    val start = System.currentTimeMillis()

    try {
      filterChain.doFilter(request, response)
    } finally {
      val durationMs = System.currentTimeMillis() - start

      log.info(
        "HTTP {} {} completed with status {} in {} ms",
        request.method,
        request.requestURI,
        response.status,
        durationMs
      )
    }
  }

}
