package com.answersolutions.runandread.data.datasource

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.answersolutions.runandread.data.model.EBookFile
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.InputStream
import javax.inject.Inject

class EBookDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun extractPdfText(input: InputStream?): EBookFile? = withContext(Dispatchers.IO) {
        input ?: return@withContext null
        return@withContext try {
            PDFBoxResourceLoader.init(context)
            PDDocument.load(input).use { document ->
                val title = document.documentInformation.title ?: "Unknown Title"
                val author = document.documentInformation.author ?: "Unknown Author"

                val text = PDFTextStripper().getText(document)
                Timber.d("PDF: $title ($author)")
                EBookFile(title, author, listOf(text))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting text from PDF")
            null
        }
    }

    suspend fun extractEpubText(input: InputStream?): EBookFile? = withContext(Dispatchers.IO) {
        input ?: return@withContext null
        return@withContext try {
            val book = EpubReader().readEpub(input)
            val title = book.metadata.titles.firstOrNull() ?: "Unknown Title"
            val author = book.metadata.authors.firstOrNull()?.toString() ?: "Unknown Author"

            val text = book.spine.spineReferences.mapNotNull { spineRef ->
                try {
                    val htmlContent = spineRef.resource.reader.readText()
                    Jsoup.parse(htmlContent).text() // Strip HTML tags
                } catch (e: Exception) {
                    Timber.e(e, "Error extracting text from EPUB spine reference")
                    null
                }
            }
            Timber.d("EPUB: $title ($author)")
            EBookFile(title, author, text)
        } catch (e: Exception) {
            Timber.e(e, "Error extracting text from EPUB")
            null
        }
    }

    suspend fun extractPlainText(input: InputStream?): EBookFile? = withContext(Dispatchers.IO) {
        input ?: return@withContext null
        return@withContext try {
            val text = input.bufferedReader().use { it.readText() }
            EBookFile("Unknown Title", "Unknown Author", listOf(text))
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
            EBookFile("Clipboard Content", "", listOf(clipData))
        } else null
    }
}

