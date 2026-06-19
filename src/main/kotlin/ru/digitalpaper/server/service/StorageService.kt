package ru.digitalpaper.server.service

import io.minio.*
import io.minio.http.Method
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.config.properties.MinioProperties
import ru.digitalpaper.server.dto.internal.DownloadedObject
import ru.digitalpaper.server.dto.internal.StoredObjectInfo
import ru.digitalpaper.server.exception.InternalErrorException
import ru.digitalpaper.server.type.StorageObjectType
import java.io.ByteArrayInputStream
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class StorageService(
    private val minioClient: MinioClient,
    private val minioProperties: MinioProperties
) {

    companion object {
        private const val SEPARATOR = "/"
    }

    fun getPublicOrResolvableUrl(
        bucket: String,
        objectKey: String,
    ): String {
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
    ): StoredObjectInfo {
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

    fun uploadBytes(
        bytes: ByteArray,
        filename: String,
        contentType: String,
        type: StorageObjectType,
        ownerId: String? = null,
    ): StoredObjectInfo {
        validateBytes(bytes, contentType, type)

        val bucket = resolveBucket(type)
        ensureBucketExists(bucket)

        val objectKey = buildObjectKey(type, filename, ownerId)

        ByteArrayInputStream(bytes).use { inputStream ->
            val response = minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(objectKey)
                    .stream(inputStream, bytes.size.toLong(), -1)
                    .contentType(contentType)
                    .userMetadata(
                        buildUserMetadata(
                            filename,
                            type,
                            ownerId
                        )
                    )
                    .build()
            )

            return StoredObjectInfo(
                bucket = bucket,
                objectKey = objectKey,
                originalFileName = filename,
                contentType = contentType,
                size = bytes.size.toLong(),
                etag = response.etag(),
                versionId = response.versionId()
            )
        }
    }

    fun download(
        objectKey: String,
        type: StorageObjectType,
    ): DownloadedObject {
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

    fun delete(
        objectKey: String,
        type: StorageObjectType
    ) {
        val bucket = resolveBucket(type)

        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(objectKey)
                    .build()
            )
        } catch (e: Exception) {
            throw InternalErrorException("Возникла ошибка при удалении файла")
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

    private fun validateBytes(
        bytes: ByteArray,
        contentType: String,
        type: StorageObjectType
    ) {
        require(bytes.isNotEmpty()) { "Файл пустой" }
        require(bytes.size <= type.maxSizeBytes) { "Размер файла превышает допустимый лимит" }
        require(contentType.lowercase() in type.allowedContentTypes) {
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
