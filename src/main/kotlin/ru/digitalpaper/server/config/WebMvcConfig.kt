package ru.digitalpaper.server.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import ru.digitalpaper.server.config.resolver.OrganizationContextArgumentResolver

@Configuration
class WebMvcConfig(
    private val organizationContextArgumentResolver: OrganizationContextArgumentResolver
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(organizationContextArgumentResolver)
    }
}
