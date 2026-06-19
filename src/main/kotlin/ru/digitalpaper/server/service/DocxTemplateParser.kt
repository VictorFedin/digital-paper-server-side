package ru.digitalpaper.server.service

import org.apache.poi.xwpf.usermodel.*
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import ru.digitalpaper.server.dto.internal.ParsedTemplateField
import ru.digitalpaper.server.model.document.holder.TemplateFieldType

@Component
class DocxTemplateParser {

    private val placeholderRegex = Regex("""\$\{([a-zA-Z0-9_]+)}""")

    fun parse(file: MultipartFile): List<ParsedTemplateField> {
        return file.inputStream.use { input ->
            XWPFDocument(input).use { document ->
                val contentControlFields = parseContentControls(document)
                val legacyFields = parseLegacyPlaceholders(document)

                (contentControlFields + legacyFields)
                    .distinctBy { it.key }
            }
        }
    }

    private fun parseContentControls(document: XWPFDocument): List<ParsedTemplateField> {
        val controls = buildList {
            addAll(findContentControls(document.bodyElements))
            document.headerList.forEach { addAll(findContentControls(it.bodyElements)) }
            document.footerList.forEach { addAll(findContentControls(it.bodyElements)) }
        }

        return controls.mapNotNull(::toParsedField)
    }

    private fun findContentControls(elements: List<IBodyElement>): List<XWPFAbstractSDT> =
        buildList {
            elements.forEach { element ->
                when (element) {
                    is XWPFSDT -> add(element)
                    is XWPFParagraph -> addAll(
                        element.getIRuns().filterIsInstance<XWPFSDT>()
                    )

                    is XWPFTable -> addAll(findContentControls(element))
                }
            }
        }

    private fun findContentControls(table: XWPFTable): List<XWPFAbstractSDT> =
        buildList {
            table.rows.forEach { row ->
                row.tableICells.forEach { cell ->
                    when (cell) {
                        is XWPFSDTCell -> add(cell)
                        is XWPFTableCell -> addAll(findContentControls(cell.bodyElements))
                    }
                }
            }
        }

    private fun toParsedField(control: XWPFAbstractSDT): ParsedTemplateField? {
        val title = control.title.orEmpty().trim()
        val tag = control.tag.orEmpty().trim()
        val content = control.content.text.orEmpty().trim()

        val key = sequenceOf(tag, title)
            .map(::normalizeKeyValue)
            .firstOrNull { it.isNotBlank() }
            ?: return null

        val label = title.takeIf { it.isNotBlank() }
            ?: content.takeIf { it.isNotBlank() }
            ?: normalizeKey(key)

        return ParsedTemplateField(
            key = key,
            label = label,
            type = inferFieldType(key, title, content),
            required = false
        )
    }

    private fun parseLegacyPlaceholders(document: XWPFDocument): List<ParsedTemplateField> {
        val text = buildString {
            appendBodyText(document.bodyElements)
            document.headerList.forEach { appendBodyText(it.bodyElements) }
            document.footerList.forEach { appendBodyText(it.bodyElements) }
        }

        return placeholderRegex
            .findAll(text)
            .map { match ->
                val key = match.groupValues[1]

                ParsedTemplateField(
                    key = key,
                    label = normalizeKey(key),
                    type = inferFieldType(key, key, ""),
                    required = false
                )
            }
            .distinctBy { it.key }
            .toList()
    }

    private fun StringBuilder.appendBodyText(elements: List<IBodyElement>) {
        elements.forEach { element ->
            when (element) {
                is XWPFParagraph -> appendLine(element.text)
                is XWPFTable -> element.rows.forEach { row ->
                    row.tableICells.forEach { cell ->
                        when (cell) {
                            is XWPFSDTCell -> appendLine(cell.content.text)
                            is XWPFTableCell -> appendBodyText(cell.bodyElements)
                        }
                    }
                }
            }
        }
    }

    private fun normalizeKeyValue(value: String): String =
        value
            .trim()
            .lowercase()
            .replace(Regex("""[^\p{L}\p{N}_]+"""), "_")
            .trim('_')

    fun normalizeKey(key: String): String {
        return key
            .trim()
            .split("_")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
    }

    private fun inferFieldType(
        key: String,
        title: String,
        content: String
    ): TemplateFieldType {
        val candidates = listOf(key, title, content)
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }

        if (candidates.any(::isDateField)) {
            return TemplateFieldType.DATE
        }

        return TemplateFieldType.TEXT
    }

    private fun isDateField(value: String): Boolean {
        val normalized = normalizeKeyValue(value)
        val words = normalized.split("_").filter { it.isNotBlank() }

        return normalized == "date" ||
            normalized.endsWith("_date") ||
            normalized.startsWith("date_") ||
            "дата" in words
    }
}
