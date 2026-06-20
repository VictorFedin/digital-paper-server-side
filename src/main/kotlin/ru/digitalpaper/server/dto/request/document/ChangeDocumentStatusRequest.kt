package ru.digitalpaper.server.dto.request.document

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import ru.digitalpaper.server.model.document.holder.DocumentStatus

@Schema(description = "Запрос на перевод документа в следующий статус")
data class ChangeDocumentStatusRequest(
    @field:NotNull(message = "Необходимо указать новый статус документа")
    @field:Schema(
        description = "Целевой статус документа. Переход должен быть разрешён текущим бизнес-процессом",
        example = "APPROVED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val status: DocumentStatus?,

    @field:Schema(
        description = "Причина или комментарий к изменению статуса",
        example = "Документ готов к проверке",
        nullable = true
    )
    val reason: String? = null
)
