package ru.digitalpaper.server.type

enum class StorageObjectType(
    val prefix: String,
    val allowedContentTypes: Set<String>,
    val maxSizeBytes: Long
) {

    USER_AVATAR(
        prefix = "user_avatars",
        allowedContentTypes = setOf("image/jpeg", "image/png", "image/webp"),
        maxSizeBytes = 5 * 1024 * 1024
    ),

    ORGANIZATION_IMAGE(
        prefix = "organization_images",
        allowedContentTypes = setOf("image/jpeg", "image/png", "image/webp"),
        maxSizeBytes = 5 * 1024 * 1024
    ),

    DOCUMENT(
        prefix = "documents",
        allowedContentTypes = setOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/png"
        ),
        maxSizeBytes = 50 * 1024 * 1024
    )

}