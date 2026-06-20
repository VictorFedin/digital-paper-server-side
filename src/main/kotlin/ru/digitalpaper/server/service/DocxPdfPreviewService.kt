package ru.digitalpaper.server.service

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.springframework.stereotype.Component
import ru.digitalpaper.server.exception.InternalErrorException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

@Component
class DocxPdfPreviewService {

    private val paragraphRegex = Regex("""<w:p\b[\s\S]*?</w:p>""")
    private val textRegex = Regex("""<w:t[^>]*>([\s\S]*?)</w:t>""")

    fun render(docx: InputStream): ByteArray {
        val text = extractText(docx).ifBlank { "Документ не содержит текстовых данных для предпросмотра." }
        return renderTextToPdf(text)
    }

    private fun extractText(docx: InputStream): String {
        ZipInputStream(docx).use { zip ->
            generateSequence { zip.nextEntry }
                .forEach { entry ->
                    if (entry.name == "word/document.xml") {
                        return extractTextFromDocumentXml(zip.readBytes().toString(Charsets.UTF_8))
                    }
                    zip.closeEntry()
                }
        }

        throw InternalErrorException("Не удалось прочитать DOCX-документ")
    }

    private fun extractTextFromDocumentXml(xml: String): String {
        return paragraphRegex.findAll(xml)
            .map { paragraph ->
                textRegex.findAll(paragraph.value)
                    .joinToString(separator = "") { decodeXml(it.groupValues[1]) }
                    .trim()
            }
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n")
    }

    private fun renderTextToPdf(text: String): ByteArray {
        PDDocument().use { document ->
            val font = loadFont(document)
            val output = ByteArrayOutputStream()
            val lines = text.lineSequence()
                .flatMap { wrapLine(it, MAX_CHARS_PER_LINE).asSequence() }
                .toList()

            var page = PDPage(PDRectangle.A4)
            document.addPage(page)
            var content = newContentStream(document, page, font)
            var y = START_Y

            lines.forEach { line ->
                if (y < BOTTOM_MARGIN) {
                    content.endText()
                    content.close()

                    page = PDPage(PDRectangle.A4)
                    document.addPage(page)
                    content = newContentStream(document, page, font)
                    y = START_Y
                }

                content.newLineAtOffset(0f, if (y == START_Y) 0f else -LINE_HEIGHT)
                content.showText(line)
                y -= LINE_HEIGHT
            }

            content.endText()
            content.close()
            document.save(output)

            return output.toByteArray()
        }
    }

    private fun newContentStream(
        document: PDDocument,
        page: PDPage,
        font: PDType0Font
    ): PDPageContentStream {
        return PDPageContentStream(document, page).apply {
            beginText()
            setFont(font, FONT_SIZE)
            newLineAtOffset(LEFT_MARGIN, START_Y)
        }
    }

    private fun wrapLine(
        value: String,
        maxChars: Int
    ): List<String> {
        if (value.length <= maxChars) return listOf(value)

        val result = mutableListOf<String>()
        var current = StringBuilder()

        value.split(Regex("""\s+""")).forEach { word ->
            if (current.isNotEmpty() && current.length + word.length + 1 > maxChars) {
                result.add(current.toString())
                current = StringBuilder()
            }

            if (current.isNotEmpty()) {
                current.append(' ')
            }
            current.append(word)
        }

        if (current.isNotEmpty()) {
            result.add(current.toString())
        }

        return result
    }

    private fun loadFont(document: PDDocument): PDType0Font {
        val fontFile = listOf(
            File("C:/Windows/Fonts/arial.ttf"),
            File("C:/Windows/Fonts/calibri.ttf"),
            File("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf")
        ).firstOrNull { it.exists() }
            ?: throw InternalErrorException("Не найден TTF-шрифт для генерации PDF")

        return PDType0Font.load(document, fontFile)
    }

    private fun decodeXml(value: String): String =
        value
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")

    private companion object {
        private const val FONT_SIZE = 11f
        private const val LINE_HEIGHT = 15f
        private const val LEFT_MARGIN = 50f
        private const val START_Y = 790f
        private const val BOTTOM_MARGIN = 50f
        private const val MAX_CHARS_PER_LINE = 95
    }
}
