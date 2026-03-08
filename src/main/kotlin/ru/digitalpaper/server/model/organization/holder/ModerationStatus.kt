package ru.digitalpaper.server.model.organization.holder

enum class ModerationStatus {
    NEW,
    PENDING_REVIEW,
    REVISION_NEEDED,
    APPROVED,
    REJECTED
}