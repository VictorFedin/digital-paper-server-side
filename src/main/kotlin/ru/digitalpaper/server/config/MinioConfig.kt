package ru.digitalpaper.server.config

import io.minio.MinioClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.digitalpaper.server.config.properties.MinioProperties

@Configuration
class MinioConfig(
    private val minioProperties: MinioProperties
) {

    @Bean
    fun minioClient(): MinioClient {
        return MinioClient.builder()
            .endpoint(minioProperties.endpoint)
            .credentials(minioProperties.accessKey, minioProperties.secretKey)
            .build()
    }
}