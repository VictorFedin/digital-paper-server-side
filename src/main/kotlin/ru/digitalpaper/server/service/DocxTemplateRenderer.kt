package ru.digitalpaper.server.service

import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Component
class DocxTemplateRenderer {

    private val sdtRegex = Regex("""<w:sdt\b[\s\S]*?</w:sdt>""")
    private val sdtPrRegex = Regex("""<w:sdtPr\b[\s\S]*?</w:sdtPr>""")
    private val sdtContentRegex = Regex("""(<w:sdtContent\b[^>]*>)([\s\S]*?)(</w:sdtContent>)""")
    private val textNodeRegex = Regex("""<w:t([^>]*)>[\s\S]*?</w:t>""")
    private val tagRegex = Regex("""<w:tag\b[^>]*(?:w:)?val="([^"]*)"[^>]*/?>""")
    private val aliasRegex = Regex("""<w:alias\b[^>]*(?:w:)?val="([^"]*)"[^>]*/?>""")

    fun render(
        template: InputStream,
        values: Map<String, String>
    ): ByteArray {
        val normalizedValues = values.mapKeys { normalizeKeyValue(it.key) }

        return ByteArrayOutputStream().use { output ->
            ZipInputStream(template).use { zipInput ->
                ZipOutputStream(output).use { zipOutput ->
                    generateSequence { zipInput.nextEntry }
                        .forEach { entry ->
                            val bytes = zipInput.readBytes()
                            val renderedBytes = if (entry.name.shouldRenderXml()) {
                                renderXml(bytes.toString(Charsets.UTF_8), normalizedValues)
                                    .toByteArray(Charsets.UTF_8)
                            } else {
                                bytes
                            }

                            zipOutput.putNextEntry(ZipEntry(entry.name))
                            zipOutput.write(renderedBytes)
                            zipOutput.closeEntry()
                            zipInput.closeEntry()
                        }
                }
            }

            output.toByteArray()
        }
    }

    private fun renderXml(
        xml: String,
        values: Map<String, String>
    ): String {
        val withContentControls = sdtRegex.replace(xml) { match ->
            renderContentControl(match.value, values)
        }

        return values.entries.fold(withContentControls) { result, (key, value) ->
            result.replace("\${$key}", escapeXml(value))
        }
    }

    private fun renderContentControl(
        xml: String,
        values: Map<String, String>
    ): String {
        val properties = sdtPrRegex.find(xml)?.value ?: return xml
        val key = sequenceOf(
            tagRegex.find(properties)?.groupValues?.get(1),
            aliasRegex.find(properties)?.groupValues?.get(1)
        )
            .filterNotNull()
            .map(::decodeXml)
            .map(::normalizeKeyValue)
            .firstOrNull { it in values }
            ?: return xml

        return replaceContentText(xml, values.getValue(key))
    }

    private fun replaceContentText(
        xml: String,
        value: String
    ): String {
        val escapedValue = escapeXml(value)

        return sdtContentRegex.replace(xml) { contentMatch ->
            var replaced = false
            val content = contentMatch.groupValues[2]
            val renderedContent = textNodeRegex.replace(content) { textMatch ->
                if (replaced) {
                    "<w:t${textMatch.groupValues[1]}></w:t>"
                } else {
                    replaced = true
                    "<w:t${textMatch.groupValues[1]}>$escapedValue</w:t>"
                }
            }.takeIf { replaced } ?: "<w:r><w:t>$escapedValue</w:t></w:r>"

            "${contentMatch.groupValues[1]}$renderedContent${contentMatch.groupValues[3]}"
        }
    }

    private fun String.shouldRenderXml(): Boolean {
        return this == "word/document.xml" ||
            startsWith("word/header") && endsWith(".xml") ||
            startsWith("word/footer") && endsWith(".xml")
    }

    private fun normalizeKeyValue(value: String): String =
        value
            .trim()
            .lowercase()
            .replace(Regex("""[^\p{L}\p{N}_]+"""), "_")
            .trim('_')

    private fun escapeXml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private fun decodeXml(value: String): String =
        value
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
}
