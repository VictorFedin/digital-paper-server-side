package ru.digitalpaper.server.service

import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.errors.MinioException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.digitalpaper.server.config.properties.MinioProperties
import ru.digitalpaper.server.util.log.ServerLogUtil
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*

@Service
class StorageService(
    private val minioClient: MinioClient,
    private val minioProperties: MinioProperties
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("grayLog")
        private const val SEPARATOR = "/"
    }

    fun saveFile(
        baseDir: String,
        subPath: String,
        documentId: UUID,
        objectExtension: String,
        content: InputStream,
        traceId: String
    ): String? {
        logger.info(
            ServerLogUtil.info(
                "StorageService.saveFile",
                traceId,
                "Enter",
                mapOf(
                    "documentId" to "$documentId",
                    "subPath" to subPath
                )
            )
        )

        var result: String? = "$baseDir$SEPARATOR$subPath$SEPARATOR$documentId.$objectExtension"

        var bias: ByteArrayInputStream? = null

        try {
            bias = ByteArrayInputStream(content.readAllBytes())
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(minioProperties.bucketName)
                    .`object`(result)
                    .stream(
                        bias,
                        bias.available().toLong(),
                        -1
                    )
                    .contentType("application/octet-stream")
                    .build()
            )
        } catch (e: MinioException) {
            logger.error(
                ServerLogUtil.error(
                    "StorageService.saveFile",
                    traceId,
                    "MinioException on attempt to save file with id = '$documentId'",
                    e
                )
            )
            result = null
        } catch (e: Exception) {
            logger.error(
                ServerLogUtil.error(
                    "StorageService.saveFile",
                    traceId,
                    "Error on attempt to save file with id = '$documentId'",
                    e
                )
            )
            result = null
        } finally {
            try {
                bias?.close()
            } catch (e: Exception) {
                logger.error(
                    ServerLogUtil.error(
                        "StorageService.saveFile",
                        traceId,
                        "Error on attempt to close bias for file with id = '$documentId'",
                        e
                    )
                )
            }
        }
        return result
    }
}