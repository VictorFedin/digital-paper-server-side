package ru.digitalpaper.server.model.document.holder

enum class DocumentStatus {
    DRAFT,
    CREATED,
    IN_PROGRESS,
    PENDING_REVIEW,
    CHANGES_REQUESTED,
    APPROVED,
    SIGNED,
    DONE,
    REJECTED,
    CANCELLED,
    EXPIRED,
    DELETED
}
