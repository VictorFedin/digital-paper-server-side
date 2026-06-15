package ru.digitalpaper.server.dto.request.document

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import ru.digitalpaper.server.model.document.holder.DocumentStatus

@Schema(description = "Запрос на перевод документа в следующий статус")
data class ChangeDocumentStatusRequest(
    @field:NotNull(message = "Необходимо указать новый статус документа")
    @field:Schema(
        description = "Целевой статус документа. Переход должен быть разрешён текущим бизнес-процессом",
        example = "IN_PROGRESS",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val status: DocumentStatus?
)
