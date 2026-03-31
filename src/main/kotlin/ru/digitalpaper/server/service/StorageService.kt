package ru.digitalpaper.server.service

import io.minio.*
import io.minio.http.Method
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.config.properties.MinioProperties
import ru.digitalpaper.server.dto.internal.DownloadedObject
import ru.digitalpaper.server.dto.internal.StoredObjectInfo
import ru.digitalpaper.server.type.StorageObjectType
import ru.digitalpaper.server.util.common.RequestSatellites
import ru.digitalpaper.server.util.log.ServerLogUtil
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class StorageService(
    private val minioClient: MinioClient,
    private val minioProperties: MinioProperties
) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("grayLog")
        private const val SEPARATOR = "/"
    }

    fun getPublicOrResolvableUrl(
        bucket: String,
        objectKey: String,
        rs: RequestSatellites
    ): String {
        logger.info(
            ServerLogUtil.info(
                "StorageService.getPublicOrResolvableUrl",
                rs.traceId,
                "Enter",
                mapOf(
                    "bucket" to bucket,
                    "objectKey" to objectKey,
                )
            )
        )

        return try {
            val url = if (bucket == minioProperties.publicBucket) {
                buildPublicUrl(bucket, objectKey)
            } else {
                minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .`object`(objectKey)
                        .expiry(minioProperties.presignedExpiryHours, TimeUnit.HOURS)
                        .build()
                )
            }

            url
        } catch (e: Exception) {
            throw e
        }
    }

    fun upload(
        file: MultipartFile,
        type: StorageObjectType,
        ownerId: String? = null,
        rs: RequestSatellites
    ): StoredObjectInfo {
        logger.info(
            ServerLogUtil.info(
                "StorageService.upload",
                rs.traceId,
                "Enter",
                mapOf("type" to type.toString())
            )
        )

        validateFile(file, type)

        val bucket = resolveBucket(type)
        ensureBucketExists(bucket)

        val objectKey = buildObjectKey(type, file.originalFilename, ownerId)

        file.inputStream.use { inputStream ->
            val response = minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(objectKey)
                    .stream(inputStream, file.size, -1)
                    .contentType(resolveContentType(file))
                    .userMetadata(
                        buildUserMetadata(
                            file.originalFilename,
                            type,
                            ownerId
                        )
                    )
                    .build()
            )

            logger.info(
                ServerLogUtil.info(
                    "StorageService.upload",
                    rs.traceId,
                    "File uploaded to Minio",
                    mapOf(
                        "bucket" to bucket,
                        "objectKey" to objectKey,
                        "etage" to response.etag(),
                        "versionId" to response.versionId()
                    )
                )
            )

            return StoredObjectInfo(
                bucket = bucket,
                objectKey = objectKey,
                originalFileName = file.originalFilename,
                contentType = resolveContentType(file),
                size = file.size,
                etag = response.etag(),
                versionId = response.versionId()
            )
        }
    }

    fun download(
        objectKey: String,
        type: StorageObjectType,
        rs: RequestSatellites
    ): DownloadedObject {
        logger.info(
            ServerLogUtil.info(
                "StorageService.download",
                rs.traceId,
                "Enter",
                mapOf(
                    "objectKey" to objectKey,
                    "type" to "$type"
                )
            )
        )

        val bucket = resolveBucket(type)

        try {
            val stat = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(objectKey)
                    .build()
            )

            val stream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(objectKey)
                    .build()
            )

            val originalFileName = stat.userMetadata()["original-filename"]

            return DownloadedObject(
                inputStream = stream,
                contentType = stat.contentType() ?: "application/octet-stream",
                size = stat.size(),
                originalFileName = originalFileName
            )
        } catch (e: Exception) {
            throw e
        }
    }

    private fun resolveBucket(
        type: StorageObjectType
    ): String {
        return when (type) {
            StorageObjectType.USER_AVATAR, StorageObjectType.ORGANIZATION_IMAGE -> minioProperties.publicBucket
            else -> minioProperties.secureBucket
        }
    }

    private fun validateFile(
        file: MultipartFile,
        type: StorageObjectType
    ) {
        val contentType = resolveContentType(file)

        require(!file.isEmpty) { "Файл пустой" }
        require(file.size <= type.maxSizeBytes) { "Размер файла превышает допустимый лимит" }
        require(contentType in type.allowedContentTypes) {
            "Недопустимый content type: $contentType"
        }
    }

    private fun resolveContentType(
        file: MultipartFile
    ): String {
        return file.contentType?.lowercase() ?: "application/octet-stream"
    }

    private fun ensureBucketExists(bucket: String) {
        val exists = minioClient.bucketExists(
            BucketExistsArgs.builder()
                .bucket(bucket)
                .build()
        )

        if (!exists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(bucket)
                    .build()
            )
        }
    }

    private fun buildObjectKey(
        type: StorageObjectType,
        originalFilename: String?,
        ownerId: String?
    ): String {
        val extension = extractExtension(originalFilename)
        val fileId = UUID.randomUUID().toString()

        return buildString {
            append(type.prefix)
            if (!ownerId.isNullOrBlank()) {
                append(SEPARATOR)
                append(ownerId)
            }
            append(SEPARATOR)
            append(fileId)
            if (extension != null) {
                append(".")
                append(extension)
            }
        }
    }

    private fun extractExtension(
        filename: String?
    ): String? {
        if (filename.isNullOrBlank()) return null
        val ext = filename.substringAfterLast('.', "")
            .lowercase()
            .trim()
        return ext.ifBlank { null }
    }

    private fun buildUserMetadata(
        originalFilename: String?,
        type: StorageObjectType,
        ownerId: String?
    ): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        originalFilename?.let { metadata["original-filename"] = it }
        metadata["object-type"] = type.name
        ownerId?.let { metadata["owner-id"] = it }
        return metadata
    }

    private fun buildPublicUrl(
        bucket: String,
        objectKey: String
    ): String {
        val baseUrl = minioProperties.endpoint.trimEnd('/')
        val normalizedObjectKey = objectKey.trimStart('/')

        return "$baseUrl/$bucket/$normalizedObjectKey"
    }
}