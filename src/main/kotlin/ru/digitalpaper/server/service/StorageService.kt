package ru.digitalpaper.server.service

import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.errors.MinioException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.digitalpaper.server.util.log.ServerLogUtil
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.UUID

@Service
class StorageService {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("grayLog")
        private const val SEPARATOR = "/"
    }

    @Value($$"${s3.config.endpoint}")
    private lateinit var endpoint: String

    @Value($$"${s3.config.accessKey}")
    private lateinit var accessKey: String

    @Value($$"${s3.config.secretKey}")
    private lateinit var secretKey: String

    @Value($$"${s3.config.bucketName}")
    private lateinit var bucketName: String


    private fun getMinioClient(): MinioClient {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build()
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
            getMinioClient().putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
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