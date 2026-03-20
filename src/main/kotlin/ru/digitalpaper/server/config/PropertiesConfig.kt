package ru.digitalpaper.server.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import ru.digitalpaper.server.config.properties.MinioProperties

@Configuration
@EnableConfigurationProperties(MinioProperties::class)
class PropertiesConfig