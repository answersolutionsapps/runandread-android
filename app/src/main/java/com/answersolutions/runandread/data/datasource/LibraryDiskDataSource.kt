package com.answersolutions.runandread.data.datasource

import android.content.Context
import com.answersolutions.runandread.data.model.AudioBook
import com.answersolutions.runandread.data.model.Book
import com.answersolutions.runandread.data.model.BookPlayerType
import com.answersolutions.runandread.data.model.RunAndReadBook
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Singleton
class LibraryDiskDataSource @Inject constructor(@ApplicationContext private val context: Context) :
    LibraryDataSource {
    private val libraryDir: File
        get() = File(context.filesDir, "library").apply { if (!exists()) mkdirs() }

    private val audiobooksDir: File
        get() = File(context.filesDir, "audiobooks").apply { if (!exists()) mkdirs() }


    override fun loadBooks(): List<RunAndReadBook> {
        val books = mutableListOf<RunAndReadBook>()
        val json = Json { ignoreUnknownKeys = true }
        libraryDir.listFiles()?.forEach { file ->
            val jsonText = file.readText()
            val book = json.decodeFromString(Book.serializer(), jsonText)
            books.add(book)
        }
        audiobooksDir.listFiles()?.forEach { file ->
            val jsonText = file.readText()
            val book = json.decodeFromString(AudioBook.serializer(), jsonText)
            books.add(book)
        }
        return books
    }

    override suspend fun addBook(book: RunAndReadBook) {
        if (book.playerType() == BookPlayerType.AUDIO) {
            val bookFile = File(audiobooksDir, "${book.id}.json")
            bookFile.writeText(Json.encodeToString(book))
        } else {
            val bookFile = File(libraryDir, "${book.id}.json")
            bookFile.writeText(Json.encodeToString(book))
        }
    }

    override suspend fun updateBook(book: RunAndReadBook) {
        if (book.playerType() == BookPlayerType.AUDIO) {
            val bookFile = File(audiobooksDir, "${book.id}.json")
            if (bookFile.exists()) {
                bookFile.writeText(Json.encodeToString(book))
            }
        } else {
            val bookFile = File(libraryDir, "${book.id}.json")
            if (bookFile.exists()) {
                bookFile.writeText(Json.encodeToString(book))
            }
        }
    }

    //todo: pass whole book to determine what to delete
    override suspend fun deleteBook(bookId: String) {
//        if (book.playerType() == BookPlayerType.AUDIO) {
//            val bookFile = File(libraryDir, "$bookId.json")
//            bookFile.delete()
//        } else {
            val bookFile = File(libraryDir, "$bookId.json")
            bookFile.delete()
//        }
    }

    private val selectedDir: File
        get() = File(context.filesDir, "selected").apply { if (!exists()) mkdirs() }

    private val selectedFile: File
        get() = File(selectedDir, "selected_book.txt")

    override suspend fun selectBook(bookId: String) {
        selectedFile.writeText(bookId)
    }

    override suspend fun getSelectedBook(): RunAndReadBook? = withContext(Dispatchers.IO) {
        if (!selectedFile.exists()) return@withContext null

        val selectedId = async {
            selectedFile.inputStream().bufferedReader().use { it.readText().trim() }
        }.await()
        val selectedBookFile1 = File(libraryDir, "$selectedId.json")
        val selectedBookFile2 = File(audiobooksDir, "$selectedId.json")
        val json = Json { ignoreUnknownKeys = true }
        val book = if (selectedBookFile1.exists()) {
            val jsonText = async {
                selectedBookFile1.inputStream().bufferedReader().use { it.readText() }
            }.await()
            async { json.decodeFromString(Book.serializer(), jsonText) }.await()
        } else if (selectedBookFile2.exists()) {
            val jsonText = async {
                selectedBookFile2.inputStream().bufferedReader().use { it.readText() }
            }.await()
            async { json.decodeFromString(AudioBook.serializer(), jsonText) }.await()
        } else {
            null
        }
        return@withContext book
    }

    override suspend fun unselectBook() {
        if (selectedFile.exists()) {
            selectedFile.delete()
        }
    }
}
