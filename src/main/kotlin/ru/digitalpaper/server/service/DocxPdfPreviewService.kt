package ru.digitalpaper.server.service

import org.springframework.stereotype.Component
import ru.digitalpaper.server.config.properties.DocumentConverterProperties
import ru.digitalpaper.server.exception.InternalErrorException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator
import java.util.concurrent.TimeUnit

@Component
class DocxPdfPreviewService(
    private val properties: DocumentConverterProperties
) {

    fun render(docx: InputStream): ByteArray {
        val workDir = Files.createTempDirectory("digital-paper-docx-pdf-")

        try {
            val inputPath = workDir.resolve("document.docx")
            val outputPath = workDir.resolve("document.pdf")

            Files.copy(docx, inputPath)
            convert(inputPath, workDir)

            if (!Files.exists(outputPath)) {
                throw InternalErrorException("Конвертер не сформировал PDF-файл")
            }

            return Files.readAllBytes(outputPath)
        } finally {
            cleanup(workDir)
        }
    }

    private fun convert(
        inputPath: Path,
        outputDir: Path
    ) {
        val process = try {
            ProcessBuilder(
                properties.sofficePath,
                "--headless",
                "--nologo",
                "--nofirststartwizard",
                "--nodefault",
                "--nolockcheck",
                "--convert-to",
                "pdf",
                "--outdir",
                outputDir.toAbsolutePath().toString(),
                inputPath.toAbsolutePath().toString()
            )
                .redirectErrorStream(true)
                .start()
        } catch (e: Exception) {
            throw InternalErrorException(
                "Не удалось запустить LibreOffice/soffice. Проверьте настройку document.converter.soffice-path"
            )
        }

        val finished = process.waitFor(properties.timeoutSeconds, TimeUnit.SECONDS)
        val output = process.inputStream.bufferedReader().use { it.readText() }

        if (!finished) {
            process.destroyForcibly()
            throw InternalErrorException("Конвертация DOCX в PDF превысила лимит времени")
        }

        if (process.exitValue() != 0) {
            throw InternalErrorException("Ошибка конвертации DOCX в PDF: ${output.ifBlank { "unknown error" }}")
        }
    }

    private fun cleanup(path: Path) {
        runCatching {
            Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach { Files.deleteIfExists(it) }
        }
    }
}
