package com.answersolutions.runandread.data.datasource

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.answersolutions.runandread.data.model.EBookFile
import com.answersolutions.runandread.data.model.TextPart
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.siegmann.epublib.epub.EpubReader
import org.json.JSONObject
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject

class EBookDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private suspend fun extractAudioBookFromRANDR(context: Context, fileUri: File): EBookFile = withContext(Dispatchers.IO) {
        val fileName = fileUri.name
        if (fileName.contains(" ")) {
            throw IOException("Spaces in file name are not allowed")
        }

        val fileNameWithoutExtension = fileUri.nameWithoutExtension
        val rootDirectory = context.filesDir
        val extractionDirectory = File(rootDirectory, "temp_extracted_$fileNameWithoutExtension")

        // Ensure clean extraction directory
        if (extractionDirectory.exists()) {
            extractionDirectory.deleteRecursively()
        }

        if (!extractionDirectory.mkdirs()) {
            throw IOException("Failed to create extraction directory")
        }

        try {
            // Unzip .randr file
            unzipFile(fileUri, extractionDirectory)

            val extractedFiles = extractionDirectory.listFiles().first().listFiles() ?: throw IOException("Extraction failed")
            val bookJsonFile = extractedFiles.find { it.name == "book.json" }
            val audioFile = extractedFiles.find { it.name == "audio.mp3" }

            if (bookJsonFile == null || audioFile == null) {
                extractionDirectory.deleteRecursively()
                throw IOException("Missing required files in the extracted content")
            }

            val jsonData = bookJsonFile.readText()
            val parsedBook = JSONObject(jsonData)

            val title = parsedBook.optString("title", "Unknown Title")
            val author = parsedBook.optString("author", "Unknown Author")
            val language = parsedBook.optString("language", "Unknown Language")
            val rate = parsedBook.optDouble("rate", 1.0).toFloat()
            val voice = parsedBook.optString("voice", "Default")
            val model = parsedBook.optString("model", "Default")
            val bookSource = parsedBook.optString("book_source", "Unknown")

            val textPartsArray = parsedBook.optJSONArray("text")
            val textParts = mutableListOf<TextPart>()

            textPartsArray?.let {
                for (i in 0 until it.length()) {
                    val item = it.getJSONObject(i)
                    val startTimeMs = item.optInt("start_time_ms", 0)
                    val text = item.optString("text", "")
                    textParts.add(TextPart(startTimeMs, text))
                }
            }

            val audioDestination = File(rootDirectory, "audio/$fileNameWithoutExtension/")
            if (!audioDestination.exists()) {
                audioDestination.mkdirs()
            }

            val finalAudioPath = File(audioDestination, "audio.mp3")
            if (finalAudioPath.exists()) {
                finalAudioPath.delete()
            }

            audioFile.copyTo(finalAudioPath, overwrite = true)

            // Clean up extraction directory
            extractionDirectory.deleteRecursively()

            return@withContext EBookFile(
                title = title,
                author = author,
                content = emptyList(),
                audioPath = finalAudioPath.absolutePath,
                text = textParts,
                language = language,
                rate = rate,
                voice = voice,
                model = model,
                bookSource = bookSource
            )
        } catch (e: Exception) {
            extractionDirectory.deleteRecursively()
            throw IOException("Failed to process RANDR file: ${e.message}")
        }
    }

    @Throws(IOException::class)
    fun unzipFile(zipFile: File, outputDir: File) {
        ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
            var entry: ZipEntry? = zipInputStream.nextEntry
            while (entry != null) {
                val outputFile = File(outputDir, entry.name)

                if (entry.isDirectory) {
                    outputFile.mkdirs()
                } else {
                    // Ensure parent directories exist before writing the file
                    outputFile.parentFile?.mkdirs()

                    FileOutputStream(outputFile).use { outputStream ->
                        zipInputStream.copyTo(outputStream)
                    }
                }

                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
        }
    }

    private suspend fun extractPdfText(input: InputStream?): EBookFile? = withContext(Dispatchers.IO) {
        input ?: return@withContext null
        return@withContext try {
            PDFBoxResourceLoader.init(context)
            PDDocument.load(input).use { document ->
                val title = document.documentInformation.title ?: "Unknown Title"
                val author = document.documentInformation.author ?: "Unknown Author"

                val text = PDFTextStripper().getText(document).split("\n")
                Timber.d("PDF: $title ($author)")
                EBookFile(
                    title,
                    author,
                    text,
                    audioPath = "",
                    emptyList<TextPart>(),
                    language = "", rate = 1.0f,
                    voice = "",
                    model = "",
                    bookSource = ""
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting text from PDF")
            null
        }
    }

    private suspend fun extractEpubText(input: InputStream?): EBookFile? = withContext(Dispatchers.IO) {
        input ?: return@withContext null
        return@withContext try {
            val book = EpubReader().readEpub(input)
            val title = book.metadata.titles.firstOrNull() ?: "Unknown Title"
            val author = book.metadata.authors.firstOrNull()?.toString() ?: "Unknown Author"

            val text = book.spine.spineReferences.mapNotNull { spineRef ->
                try {
                    val htmlContent = spineRef.resource.reader.readText()
                    val document = Jsoup.parse(htmlContent)

                    document.select("title, section, cover, colophon, imprint, endnote, copyright")
                        .remove()
                    val cleanedText = document.text().trim()
//                    Jsoup.parse(htmlContent).text() // Strip HTML tags
                    cleanedText.takeIf { it.isNotBlank() }
                } catch (e: Exception) {
                    Timber.e(e, "Error extracting text from EPUB spine reference")
                    null
                }
            }

            Timber.d("EPUB: $title ($author)")
            EBookFile(
                title,
                author,
                text,
                audioPath = "",
                emptyList<TextPart>(),
                language = "", rate = 1.0f,
                voice = "",
                model = "",
                bookSource = ""
            )
        } catch (e: Exception) {
            Timber.e(e, "Error extracting text from EPUB")
            null
        }
    }

    private suspend fun extractPlainText(input: InputStream?): EBookFile? = withContext(Dispatchers.IO) {
        input ?: return@withContext null
        return@withContext try {
            val text = input.bufferedReader().use { it.readText() }
                .split("\n")//, listOf("This text has been narrated by the Run and Read app.")
            EBookFile(
                "Unknown Title",
                "Unknown Author",
                text,
                audioPath = "",
                emptyList<TextPart>(),
                language = "", rate = 1.0f,
                voice = "",
                model = "",
                bookSource = ""
            )
        } catch (e: Exception) {
            Timber.e(e, "Error extracting plain text")
            null
        }
    }

    suspend fun getEBookFileFromUri(uri: Uri): EBookFile? = withContext(Dispatchers.IO) {
        val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) cursor.getString(nameIndex) else "Unknown File"
            } else "Unknown File"
        } ?: "Unknown File"

        Timber.d("fileName: $fileName")

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            return@withContext when {
                fileName.lowercase().endsWith(".randr") -> {
                    val tempFile = File(context.cacheDir, fileName)
                    tempFile.outputStream().use { output -> inputStream.copyTo(output) }
                    extractAudioBookFromRANDR(context, tempFile)
                }
                fileName.lowercase().endsWith(".pdf") -> extractPdfText(inputStream)
                fileName.lowercase().endsWith(".epub") -> extractEpubText(inputStream)
                else -> extractPlainText(inputStream)
            }
        }
        return@withContext null
    }

    suspend fun getEbookFileFromClipboard(): EBookFile? = withContext(Dispatchers.IO) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip?.getItemAt(0)?.text?.toString()

        return@withContext if (!clipData.isNullOrBlank()) {
            Timber.d("Clipboard: $clipData")
            EBookFile(
                "Clipboard Content",
                "",
                clipData.split("\n"),
                audioPath = "",
                emptyList<TextPart>(),
                language = "", rate = 1.0f,
                voice = "",
                model = "",
                bookSource = ""
            )//, "This text has been narrated by the Run and Read app."))
        } else null
    }
}

