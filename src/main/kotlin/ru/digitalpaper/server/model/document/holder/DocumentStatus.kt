package ru.digitalpaper.server.model.document.holder

enum class DocumentStatus {
    CREATED,
    IN_PROGRESS,
    PENDING_REVIEW,
    DONE,
    REJECTED
}