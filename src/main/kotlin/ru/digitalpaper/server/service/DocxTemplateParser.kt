package ru.digitalpaper.server.service

import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.dto.internal.ParsedTemplateField
import ru.digitalpaper.server.model.document.holder.TemplateFieldType

@Component
class DocxTemplateParser {

    private val placeholderRegex = Regex("""\$\{([a-zA-Z0-9_]+)}""")

    fun parse(file: MultipartFile): List<ParsedTemplateField> {
        val text = file.inputStream.use { input ->
            XWPFDocument(input).use { document ->
                buildString {
                    document.paragraphs.forEach { paragraph ->
                        appendLine(paragraph.text)
                    }

                    document.tables.forEach { table ->
                        table.rows.forEach { row ->
                            row.tableCells.forEach { cell ->
                                appendLine(cell.text)
                            }
                        }
                    }
                }
            }
        }

        return placeholderRegex
            .findAll(text)
            .map { match ->
                val key = match.groupValues[1]

                ParsedTemplateField(
                    key = key,
                    label = normalizeKey(key),
                    type = TemplateFieldType.TEXT,
                    required = false
                )
            }
            .distinctBy { it.key }
            .toList()
    }

    fun normalizeKey(key: String): String {
        return key
            .trim()
            .split("_")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
    }
}
